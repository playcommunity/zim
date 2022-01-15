package org.bitlap.zim.server.configuration

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import org.bitlap.zim.server.api.{ ActuatorApi, OpenApi, ZimUserApi }
import org.bitlap.zim.server.configuration.ApplicationConfiguration.ZApplicationConfiguration
import zio._
import org.bitlap.zim.server.configuration.AkkaHttpConfiguration.ZMaterializer

/**
 * API配置
 *
 * @author 梦境迷离
 * @since 2021/12/25
 * @version 1.0
 */
final class ApiConfiguration(applicationConfiguration: ApplicationConfiguration)(implicit materializer: Materializer) {

  val zimUserApi: ZimUserApi = ZimUserApi(applicationConfiguration.apiApplication)

  val zimActuatorApi: ActuatorApi = ActuatorApi()

  val zimOpenApi: OpenApi = OpenApi()

}

/**
 * API依赖管理
 */
object ApiConfiguration {

  def apply(applicationConfiguration: ApplicationConfiguration, materializer: Materializer): ApiConfiguration =
    new ApiConfiguration(applicationConfiguration)(materializer)

  type ZApiConfiguration = Has[ApiConfiguration]

  val routes: URIO[ZApiConfiguration, Route] =
    for {
      userRoute <- ZIO.access[ZApiConfiguration](_.get.zimUserApi.route)
      actuatorRoute <- ZIO.access[ZApiConfiguration](_.get.zimActuatorApi.route)
      openRoute <- ZIO.access[ZApiConfiguration](_.get.zimOpenApi.route)
    } yield openRoute ~ actuatorRoute ~ userRoute

  val live: RLayer[ZApplicationConfiguration with ZMaterializer, ZApiConfiguration] =
    ZLayer.fromServices[ApplicationConfiguration, Materializer, ApiConfiguration](ApiConfiguration(_, _))

  def make(
    applicationConfiguration: ApplicationConfiguration,
    materializer: Materializer
  ): TaskLayer[ZApiConfiguration] =
    ZLayer.succeed(applicationConfiguration) ++ ZLayer.succeed(materializer) >>> live

}