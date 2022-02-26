package org.bitlap.zim.domain

/**
 * 具有分页功能的结果集
 *
 * @param data 每页数据
 * @param pages  页数
 * @tparam T 分页内容
 * @since 2022年1月1日
 * @author 梦境迷离
 */
final case class ResultPageSet[T](override val data: List[T] = Nil, pages: Int) extends ResultSet(data)
