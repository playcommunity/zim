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

package org.bitlap.zim.server.util

import org.bitlap.zim.domain.input.UserSecurity.UserSecurityInfo
import org.bitlap.zim.tapir.ApiJsonCodec
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.schema.codec.ProtobufCodec

/**
 * @author 梦境迷离
 * @since 2022/3/5
 * @version 1.0
 */
class SchemaSpec extends AnyFlatSpec with Matchers with ApiJsonCodec {

  "Schema" should "ok" in {
    // TODO 直接使用存Redis会有错误 209 变成 3104751
    val userSecurityInfo =
      UserSecurityInfo(209, "dreamylost@qq.com", "jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=", "顶顶顶顶")
    val chunkByte = ProtobufCodec.encode(UserSecurityInfo.userSecuritySchema)(userSecurityInfo)
    val str = ProtobufCodec.decode(UserSecurityInfo.userSecuritySchema)(chunkByte)
    val id = str.map(_.id)
    id.getOrElse(0) shouldBe userSecurityInfo.id
  }
}
