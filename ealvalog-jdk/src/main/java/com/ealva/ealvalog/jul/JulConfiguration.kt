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

import com.ealva.ealvalog.Configuration
import com.ealva.ealvalog.LogLevel
import com.ealva.ealvalog.core.BasicMarkerFactory
import java.util.logging.Handler

/**
 * Created by Eric A. Snell on 9/6/18.
 */
class JulConfiguration(
  private var resetJulLoggers: Boolean,
  private var rootIncludeLocation: Boolean,
  private var rootLogLevel: LogLevel
) : Configuration(JdkLoggerFactory, BasicMarkerFactory()) {
  fun configure(rootHandlers: List<Handler>) {
    if (resetJulLoggers) JdkLoggerFactory.reset(true)
    val rootLogger = JdkLoggerFactory.root
    rootLogger.includeLocation = rootIncludeLocation
    rootLogger.logLevel = rootLogLevel
    rootHandlers.forEach { handler ->
      JdkLoggerFactory.root.addHandler(handler)
    }
  }
}