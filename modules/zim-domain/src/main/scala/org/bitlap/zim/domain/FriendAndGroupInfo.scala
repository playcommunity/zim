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

package org.bitlap.zim.domain

import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }
import org.bitlap.zim.domain.model.{ GroupList, User }

/**
 * 好友和群组整个信息集
 *
 * @param mine   我的信息
 * @param friend 好友列表
 * @param group  群组信息列表
 */
final case class FriendAndGroupInfo(
  mine: User,
  friend: List[FriendList],
  group: List[GroupList]
)

object FriendAndGroupInfo {

  implicit val decoder: Decoder[FriendAndGroupInfo] = deriveDecoder[FriendAndGroupInfo]
  implicit val encoder: Encoder[FriendAndGroupInfo] = deriveEncoder[FriendAndGroupInfo]

}
