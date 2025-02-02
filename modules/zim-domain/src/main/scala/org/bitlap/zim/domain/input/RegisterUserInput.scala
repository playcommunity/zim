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

package org.bitlap.zim.domain.input
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

/**
 * 注册用户信息提交 输入
 *
 * @param username 用户名
 * @param email 邮箱
 * @param password 密码
 */
final case class RegisterUserInput(
  username: String,
  password: String,
  email: String
)
object RegisterUserInput {

  implicit val decoder: Decoder[RegisterUserInput] = deriveDecoder[RegisterUserInput]

  // 测试用
  implicit val encoder: Encoder[RegisterUserInput] = deriveEncoder[RegisterUserInput]

}
