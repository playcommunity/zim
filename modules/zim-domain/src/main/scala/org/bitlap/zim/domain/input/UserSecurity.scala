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
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder, HCursor }
import zio.schema.{ DeriveSchema, Schema }

import java.nio.charset.Charset
import java.util.Base64

/**
 * 用户登录 输入
 * cookie
 * @param cookie token 目前是邮箱+密码
 */
final case class UserSecurity(cookie: String)

object UserSecurity {

  implicit val decoder: Decoder[UserSecurity] = deriveDecoder[UserSecurity]

  case class UserSecurityInfo(id: Int, email: String, password: String, username: String) {
    def toCookieValue: String = {
      val base64 = Base64.getEncoder.encode(s"$email:$password".getBytes(Charset.forName("utf8")))
      new String(base64)
    }
  }

  object UserSecurityInfo {

    implicit val encoder: Encoder[UserSecurityInfo] = deriveEncoder[UserSecurityInfo]

    implicit val decoder: Decoder[UserSecurityInfo] = (c: HCursor) => {
      if (!c.succeeded) null
      else
        for {
          id <- c.getOrElse("id")(0)
          email <- c.downField("email").as[String]
          password <- c.downField("password").as[String]
          username <- c.getOrElse("username")("")
        } yield UserSecurityInfo(id, email, password, username)
    }

    implicit val userSecuritySchema: Schema[UserSecurityInfo] = DeriveSchema.gen[UserSecurityInfo]
  }

}
