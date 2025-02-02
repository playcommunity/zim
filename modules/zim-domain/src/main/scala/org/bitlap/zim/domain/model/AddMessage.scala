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

package org.bitlap.zim.domain.model

import io.circe._
import io.circe.generic.semiauto._
import scalikejdbc.{ WrappedResultSet, _ }

import java.time.ZonedDateTime

/**
 * 添加消息
 *
 * @see table:t_add_message
 * @param id 暂时都使用`Int`
 * @param fromUid 谁发起的请求
 * @param toUid   发送给谁的申请,可能是群，那么就是创建该群组的用户
 * @param groupId 如果是添加好友则为from_id的分组id，如果为群组则为群组id
 * @param remark  附言
 * @param agree   0未处理，1同意，2拒绝
 * @param `type`  类型，可能是添加好友或群组
 * @param time    申请时间
 */
final case class AddMessage(
  id: Int,
  fromUid: Int,
  toUid: Int,
  groupId: Int,
  remark: String,
  agree: Int,
  `type`: Int,
  time: ZonedDateTime
)

object AddMessage extends BaseModel[AddMessage] {

  // 日期格式化
  import org.bitlap.zim.domain._

  override lazy val columns: collection.Seq[String] = autoColumns[AddMessage]()

  override val tableName = "t_add_message"

  implicit val decoder: Decoder[AddMessage] = deriveDecoder[AddMessage]
  implicit val encoder: Encoder[AddMessage] = deriveEncoder[AddMessage]

  override def apply(rs: WrappedResultSet)(implicit sp: SyntaxProvider[AddMessage]): AddMessage = autoConstruct(rs, sp)

  def apply(id: Int, agree: Int): AddMessage =
    AddMessage(
      id = id,
      fromUid = 0,
      toUid = 0,
      groupId = 0,
      remark = null,
      agree = agree,
      `type` = 0,
      time = null
    )

  def apply(
    fromUid: Int,
    toUid: Int,
    groupId: Int,
    remark: String,
    `type`: Int,
    time: ZonedDateTime = ZonedDateTime.now()
  ): AddMessage =
    AddMessage(0, fromUid, toUid, groupId, remark, 0, `type`, time)
}
