package org.bitlap.zim.tapir

import akka.event.slf4j.Logger
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpHeader, HttpResponse }
import akka.http.scaladsl.server.Directives.{ complete, extractUri, getFromResource }
import akka.http.scaladsl.server._
import io.circe.syntax.EncoderOps
import org.bitlap.zim.domain.input.UserSecurity
import org.bitlap.zim.domain.{ ResultSet, SystemConstant }
import sttp.model.StatusCode
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{ EndpointOutput, _ }
import org.bitlap.zim.domain.ZimError._

import java.util.Base64
import scala.util.Try

/**
 * 错误处理
 *
 * @author 梦境迷离
 * @since 2021/12/25
 * @version 1.0
 */
trait ApiErrorMapping extends ApiJsonCodec {

  lazy val errorOut: EndpointIO.Body[String, BusinessException] = jsonBody[BusinessException].description("unknown")

  lazy val errorOutVar: Seq[EndpointOutput.OneOfVariant[BusinessException]] = Seq(
    oneOfVariant(StatusCode.Unauthorized, jsonBody[BusinessException].description("unauthorized")),
    oneOfVariant(StatusCode.NotFound, jsonBody[BusinessException].description("not found")),
    oneOfDefaultVariant(jsonBody[BusinessException].description("business exception"))
  )

  // 注意这里是PartialFunction，不能使用`_`匹配
  implicit def customExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: Unauthorized =>
      extractUri { uri =>
        Logger.root.error(s"Request to $uri could not be handled normally cause by ${e.getCause}")
        getFromResource("static/html/403.html")
      }
    case e: BusinessException =>
      extractUri { uri =>
        Logger.root.error(s"Request to $uri could not be handled normally cause by ${e.getCause}")
        val result = ResultSet(code = e.code, msg = if (e.msg != null) e.msg else SystemConstant.ERROR_MESSAGE)
        val resp = HttpEntity(ContentTypes.`application/json`, result.asJson.noSpaces)
        complete(HttpResponse(InternalServerError, entity = resp))
      }
    case e: Exception =>
      extractUri { uri =>
        Logger.root.error(s"Request to $uri could not be handled normally cause by ${e.getCause}")
        val resp = HttpEntity(
          ContentTypes.`application/json`,
          ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.ERROR_MESSAGE).asJson.noSpaces
        )
        complete(HttpResponse(InternalServerError, entity = resp))
      }
  }

  // 处理403 404 500
  implicit def customRejectionHandler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleNotFound {
        getFromResource("static/html/404.html")
      }
      .handle { case MissingCookieRejection(_) =>
        getFromResource("static/html/403.html")
      }
      .handle { case _ =>
        // 所有其他的先使用404，后续改成500
        getFromResource("static/html/404.html")
      }
      .result()

  def extractAuthorization: PartialFunction[HttpHeader, Option[UserSecurity]] = {
    case h: HttpHeader =>
      val secret: String = Try(new String(Base64.getDecoder.decode(h.value()))).getOrElse(null)
      Option(secret).map(f => UserSecurity(f))
    case _ => None
  }
}