package org.bitlap.zim.util

import zio.{ UIO, ZIO }

import java.util.UUID

/**
 * UUID工具
 *
 * @since 2021年12月31日
 * @author 梦境迷离
 */
object UUIDUtil {

  /**
   * 64位随机UUID
   */
  def getUUID64String(): UIO[String] =
    ZIO.succeed((UUID.randomUUID.toString + UUID.randomUUID.toString).replace("-", ""))

  /**
   * 32位随机UUID
   */
  def getUUID32String(): UIO[String] =
    ZIO.succeed(UUID.randomUUID.toString.replace("-", ""))

}