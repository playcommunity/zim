/*
 * Copyright 2021 bitlap
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitlap.zim.server.application

import io.circe.syntax.EncoderOps
import org.bitlap.zim.cache.ZioRedisService
import org.bitlap.zim.domain.model.Receive
import org.bitlap.zim.domain.ws.protocol.{ protocol, AddRefuseMessage }
import org.bitlap.zim.domain.{ Message, SystemConstant }
import zio.actors.{ ActorRef => _ }
import zio.stream.ZStream
import zio.{ IO, ZIO }

import java.time.ZonedDateTime

/**
 * @author 梦境迷离
 * @since 2022/1/12
 * @version 1.0
 */
package object ws {

  /**
   * 封装返回消息格式
   */
  private[ws] def getReceive(message: Message): Receive = {
    val mine = message.mine
    val to = message.to
    Receive(
      mid = mine.id,
      username = mine.username,
      avatar = mine.avatar,
      `type` = to.`type`,
      content = mine.content,
      cid = 0,
      mine = false,
      fromid = mine.id,
      timestamp = ZonedDateTime.now().toInstant.toEpochMilli,
      status = 0,
      toid = to.id
    )
  }

  private[ws] def friendMessageHandler(userService: UserApplication)(message: Message): IO[Throwable, Unit] = {
    val uid = message.to.id
    val receive = getReceive(message)
    userService.findUserById(uid).runHead.flatMap { us =>
      {
        val msg = if (WsService.actorRefSessions.containsKey(uid)) {
          val actorRef = WsService.actorRefSessions.get(uid)
          val tmpReceiveArchive = receive.copy(status = 1)
          WsService.sendMessage(tmpReceiveArchive.asJson.noSpaces, actorRef)
          tmpReceiveArchive
        } else receive
        // 由于都返回了stream，使用时都转成非stream
        userService.saveMessage(msg).runHead.unit
      }.unless(us.isEmpty)
    }
  }

  private[ws] def groupMessageHandler(userService: UserApplication)(message: Message): IO[Throwable, Unit] = {
    val gid = message.to.id
    val receive = getReceive(message)
    var receiveArchive: Receive = receive.copy(mid = gid)
    val sending = userService.findGroupById(gid).runHead.flatMap { group =>
      userService
        .findUserByGroupId(gid)
        .filter(_.id != message.mine.id)
        .foreach { user =>
          {
            //是否在线
            val actorRef = WsService.actorRefSessions.get(user.id)
            receiveArchive = receiveArchive.copy(status = 1)
            WsService.sendMessage(receiveArchive.asJson.noSpaces, actorRef)
          }.when(WsService.actorRefSessions.containsKey(user.id))
        }
        .unless(group.isEmpty)
    }

    sending *> userService.saveMessage(receiveArchive).runHead.unit
  }

  private[ws] def agreeAddGroupHandler(
    userService: UserApplication
  )(agree: AddRefuseMessage): IO[Throwable, Unit] =
    userService.addGroupMember(agree.groupId, agree.toUid, agree.messageBoxId).runHead.map { f =>
      userService
        .findGroupById(agree.groupId)
        .runHead
        .flatMap { groupList =>
          // 通知加群成功
          val actor = WsService.actorRefSessions.get(agree.toUid);
          {
            val message = Message(
              `type` = protocol.agreeAddGroup.stringify,
              mine = agree.mine,
              to = null,
              msg = groupList.fold("")(g => g.asJson.noSpaces)
            )
            WsService.sendMessage(message.asJson.noSpaces, actor)
          }
            .when(groupList.isDefined && actor != null)
        }
        .unless(!f.getOrElse(false))
    }

  private[ws] def refuseAddFriendHandler(
    userService: UserApplication
  )(messageBoxId: Int, username: String, to: Int): IO[Throwable, Boolean] =
    userService.updateAgree(messageBoxId, 2).runHead.flatMap { r =>
      r.fold(ZIO.effect(false)) { ret =>
        val actor = WsService.actorRefSessions.get(to)
        if (actor != null) {
          val result = Map("type" -> "refuseAddFriend", "username" -> username)
          WsService.sendMessage(result.asJson.noSpaces, actor).as(ret)
        } else ZIO.effect(ret)
      }
    }

  private[ws] def readOfflineMessageHandler(
    userService: UserApplication
  )(message: Message): IO[Throwable, Unit] =
    userService
      .findOffLineMessage(message.mine.id, 0)
      .runCount
      .map { c =>
        {
          if (message.to.`type` == SystemConstant.GROUP_TYPE) {
            // 我所有的群中有未读的消息吗
            userService.readGroupMessage(message.mine.id, message.mine.id).runHead
          } else {
            userService.readFriendMessage(message.mine.id, message.to.id).runHead
          }
        } when (c > 0)
      }
      .unit

  private[ws] def changeOnlineHandler(
    userService: UserApplication
  )(uId: Int, status: String): IO[Throwable, Boolean] = {
    val isOnline = SystemConstant.status.ONLINE.equals(status)
    val beforeChange =
      if (isOnline) ZioRedisService.setSet(SystemConstant.ONLINE_USER, s"$uId")
      else ZioRedisService.removeSetValue(SystemConstant.ONLINE_USER, s"$uId")
    // 向我的所有在线好友发送广播消息，告知我的状态变更，否则只能再次打聊天开窗口时变更,todo 异步发送
    beforeChange *> {
      val ret = for {
        fs <- userService.findFriendGroupsById(uId)
        users <- ZStream.fromEffect(ZioRedisService.getSets(SystemConstant.ONLINE_USER))
        u <- ZStream.fromIterable(fs.list)
        notify <- {
          val fu = users.contains(u.id.toString)
          val actorRef = WsService.actorRefSessions.get(u.id);
          {
            val msg = Map(
              "id" -> s"$uId", //对好友而言，好友的好友就是我
              "type" -> protocol.checkOnline.stringify,
              "status" -> (if (isOnline) SystemConstant.status.ONLINE_DESC else SystemConstant.status.HIDE_DESC)
            )
            ZStream.fromEffect(WsService.sendMessage(msg.asJson.noSpaces, actorRef))
          }.when(fu && actorRef != null)
        }
      } yield notify

      ret.runCollect
    } *> userService.updateUserStatus(status, uId).runHead.map(_.getOrElse(false))
  }
}
