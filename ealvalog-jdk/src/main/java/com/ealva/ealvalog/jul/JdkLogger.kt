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

package com.ealva.ealvalog.jul

import com.ealva.ealvalog.LogEntry
import com.ealva.ealvalog.LogLevel
import com.ealva.ealvalog.Marker
import com.ealva.ealvalog.core.CoreLogger
import com.ealva.ealvalog.core.ExtLogRecord
import org.jetbrains.annotations.TestOnly
import java.util.logging.Handler

/**
 * Implementation that uses [java.util.logging.Logger]
 *
 * Created by Eric A. Snell on 2/28/17.
 */
class JdkLogger internal constructor(
  override val name: String,
  override var marker: Marker?,
  private var config: JdkLoggerConfiguration
) : CoreLogger<JdkBridge>(name, config) {

  internal fun update(configuration: JdkLoggerConfiguration) {
    this.config = configuration
    bridge = configuration.getBridge(name)
  }

  fun addHandler(handler: Handler) {
    config.addLoggerHandler(this, handler)
  }

  override fun getLogEntry(logLevel: LogLevel, marker: Marker?, throwable: Throwable?): LogEntry {
    return ExtLogRecord.get(logLevel, name, marker, throwable)
  }

  val bridgeForTest: JdkBridge
    @TestOnly
    get() = bridge
}
