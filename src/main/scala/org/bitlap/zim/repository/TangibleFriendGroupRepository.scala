package org.bitlap.zim.repository

import org.bitlap.zim.domain.model.FriendGroup
import zio.{ stream, Has, ULayer, ZLayer }

/**
 * 好友分组操作实现
 *
 * @author LittleTear
 * @since 2021/12/31
 * @version 1.0
 */

private final class TangibleFriendGroupRepository(databaseName: String) extends FriendGroupRepository[FriendGroup] {

  private implicit lazy val dbName: String = databaseName

  override def createFriendGroup(receive: FriendGroup): stream.Stream[Throwable, Int] =
    _createFriendGroup(FriendGroup.table, receive).toUpdateOperation

  override def findFriendGroupsById(uid: Int): stream.Stream[Throwable, FriendGroup] =
    _findFriendGroupsById(FriendGroup.table, uid).toStreamOperation

  override def findById(id: Long): stream.Stream[Throwable, FriendGroup] = ???

  override def findAll(): stream.Stream[Throwable, FriendGroup] = ???
}

object TangibleFriendGroupRepository {

  def apply(databaseName: String): FriendGroupRepository[FriendGroup] =
    new TangibleFriendGroupRepository(databaseName)

  type ZFriendGroupRepository = Has[FriendGroupRepository[FriendGroup]]

  val live: ZLayer[Has[String], Nothing, ZFriendGroupRepository] =
    ZLayer.fromService[String, FriendGroupRepository[FriendGroup]](TangibleFriendGroupRepository(_))

  def make(databaseName: String): ULayer[ZFriendGroupRepository] =
    ZLayer.succeed(databaseName) >>> live

}
