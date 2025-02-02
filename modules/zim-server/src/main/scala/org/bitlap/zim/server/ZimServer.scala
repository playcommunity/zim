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

package org.bitlap.zim.server

import org.bitlap.zim.server.configuration.{ AkkaHttpConfiguration, ApiConfiguration, ZimServiceConfiguration }
import org.bitlap.zim.server.util.LogUtil
import zio._

/**
 * main方法
 *
 * @author 梦境迷离
 * @version 1.0,2021/12/24
 */
object ZimServer extends ZimServiceConfiguration with zio.App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (for {
      routes <- ApiConfiguration.routes
      _ <- AkkaHttpConfiguration.httpServer(routes)
    } yield ())
      .provideLayer(ZimEnv)
      .foldM(
        e => LogUtil.error(s"error => $e").exitCode,
        _ => UIO.effectTotal(ExitCode.success)
      )

}
