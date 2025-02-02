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

package org.bitlap.zim.domain.repository

import zio.stream

/**
 * 基础操作类
 *
 * @author 梦境迷离
 * @since 2021/12/25
 * @version 1.0
 */
trait BaseRepository[T] {

  /**
   * find by id
   */
  def findById(id: Long): stream.Stream[Throwable, T]

  /**
   * find by params
   */
//  def find(params: Condition*): stream.Stream[Throwable, T]
}
