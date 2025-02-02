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

package org.bitlap.zim.server.configuration

import org.bitlap.zim.domain.ws.protocol
import org.bitlap.zim.domain.ws.protocol.Constants
import org.bitlap.zim.server.actor.{ ScheduleStateful, UserStatusStateful }
import org.bitlap.zim.server.configuration.InfrastructureConfiguration.ZInfrastructureConfiguration
import zio.actors._
import zio.clock.Clock
import zio.{ UIO, _ }

/**
 * zio actor configuration
 *
 * @author 梦境迷离
 * @since 2021/12/25
 * @version 1.0
 */
object ZioActorSystemConfiguration {

  type ZZioActorSystemConfiguration = Has[ActorSystem]

  /**
   * create a zio actorSystem
   */
  private lazy val actorSystem: RIO[ZInfrastructureConfiguration, ActorSystem] = {
    for {
      actorSystem <- ActorSystem("zioActorSystem")
    } yield actorSystem
  }

  lazy val scheduleActor: ZIO[Any, Throwable, ActorRef[protocol.Command]] =
    actorSystem
      .flatMap(_.make(Constants.SCHEDULE_JOB_ACTOR, zio.actors.Supervisor.none, (), ScheduleStateful.stateful))
      .provideLayer(Clock.live ++ InfrastructureConfiguration.live)

  lazy val userStatusActor: ZIO[Any, Throwable, ActorRef[protocol.Command]] =
    actorSystem
      .flatMap(
        _.make(Constants.USER_STATUS_CHANGE_ACTOR, zio.actors.Supervisor.none, (), UserStatusStateful.stateful)
      )
      .provideLayer(Clock.live ++ InfrastructureConfiguration.live)

  val live: RLayer[ZInfrastructureConfiguration, ZZioActorSystemConfiguration] = ZLayer
    .fromAcquireRelease(actorSystem)(actorSystem => UIO.succeed(actorSystem.shutdown).ignore)

}
