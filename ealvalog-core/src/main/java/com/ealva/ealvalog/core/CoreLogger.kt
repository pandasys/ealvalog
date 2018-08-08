/*
 * Copyright 2017 Eric A. Snell
 *
 * This file is part of eAlvaLog.
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

package com.ealva.ealvalog.core

import com.ealva.ealvalog.LogLevel
import com.ealva.ealvalog.LoggerFilter
import com.ealva.ealvalog.Marker

import java.util.logging.LogRecord

/**
 * This logger delegates to a [Bridge] for [.isLoggable] and
 * [.printLog]
 *
 *
 * Created by Eric A. Snell on 3/7/17.
 */
abstract class CoreLogger<T : Bridge> protected constructor(
  name: String,
  @field:Volatile protected open var bridge: T,
  marker: Marker?
) : BaseLogger(name, marker) {

  abstract var logToParent: Boolean

  abstract fun shouldLogToParent(): Boolean

  public override fun printLog(
    level: LogLevel,
    marker: Marker?,
    throwable: Throwable?,
    stackDepth: Int,
    msg: String,
    vararg formatArgs: Any
  ) {
    // isLoggable() should have already been called
    bridge.log(this, level, marker, throwable, stackDepth + 1, msg, *formatArgs)
  }

  override fun logImmediate(record: LogRecord) {
    bridge.log(record)
  }

  abstract fun setFilter(filter: LoggerFilter)
}