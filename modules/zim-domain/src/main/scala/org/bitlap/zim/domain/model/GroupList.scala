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
import org.bitlap.zim.domain.Group
import scalikejdbc.{ WrappedResultSet, _ }

/**
 * 群组信息
 *
 * @see table:t_group
 * @param id        群组id
 * @param groupName 群组名称
 * @param avatar    头像
 * @param createId  创建人ID
 */
final case class GroupList(
  override val id: Int,
  override val groupName: String,
  avatar: String,
  createId: Int
) extends Group(id, groupName)

object GroupList extends BaseModel[GroupList] {

  // 能自动处理驼峰和非驼峰  不能处理 group_name => groupname
  override lazy val columns: collection.Seq[String] = autoColumns[GroupList]()

  override def tableName: String = "t_group"

  implicit val decoder: Decoder[GroupList] = deriveDecoder[GroupList]
  implicit val encoder: Encoder[GroupList] = (a: GroupList) => {
    if (a == null) Json.Null
    else
      Json.obj(
        ("id", Json.fromInt(a.id)),
        ("groupname", Json.fromString(a.groupName)),
        ("avatar", Json.fromString(a.avatar)),
        ("createId", Json.fromInt(a.createId))
      )
  }

  override def apply(rs: WrappedResultSet)(implicit sp: SyntaxProvider[GroupList]): GroupList = autoConstruct(rs, sp)

}
