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

package com.ealva.ealvalog.impl

import com.ealva.ealvalog.BaseLoggerFactory
import com.ealva.ealvalog.LogLevel
import com.ealva.ealvalog.Logger
import com.ealva.ealvalog.LoggerFactory
import com.ealva.ealvalog.LoggerFilter
import com.ealva.ealvalog.Marker
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.LogManager

/**
 * Factory for [JdkLogger] instances
 *
 *
 * Created by Eric A. Snell on 3/4/17.
 */
object JdkLoggerFactory : BaseLoggerFactory(), JdkLoggerConfiguration {
  private val bridgeMap: ConcurrentHashMap<String, JdkBridge> = ConcurrentHashMap()
  private val loggerMap: ConcurrentMap<String, JdkLogger> = ConcurrentHashMap()
  private val jdkBridgeRoot = JdkBridge(LoggerFactory.ROOT_LOGGER_NAME)
  val root = JdkLogger(LoggerFactory.ROOT_LOGGER_NAME, null, this)
  private val bridgeTreeLock = ReentrantLock()

  @TestOnly
  fun getForTest(name: String): JdkLogger {
    return getLogger(name, null, false)
  }

  /**
   * Resets all loggers, removing filters and underlying handlers from the java.util.logging Loggers
   */
  fun reset() {
    LogManager.getLogManager().reset()
    val root = LogManager.getLogManager().getLogger("")
    val handlers = root.handlers
    for (handler in handlers) {
      root.removeHandler(handler)
      handler.close()
    }

    for (bridge in bridgeMap.values) {
      bridge.setToDefault()
    }

    bridgeMap.clear()
    setParents()
    updateLoggers()
  }

  override fun getLogger(name: String, marker: Marker?, incLocation: Boolean): JdkLogger {
    if (LoggerFactory.ROOT_LOGGER_NAME == name) {
      return root
    }
    bridgeTreeLock.lock()
    try {
      var created = false
      val logger = loggerMap.getOrPut(name) {
        created = true // if we create a new one we need to ensure the parent hierarchy is correct
        JdkLogger(name, marker, this)
      }
      if (created) {
        setParents()
        if (incLocation) {
          logger.includeLocation = true
        }
      }
      return logger
//      var jdkLogger: JdkLogger? = loggerMap[name]
//      if (jdkLogger == null) {
//        jdkLogger = JdkLogger(name, marker, this)
//        loggerMap[name] = jdkLogger
//        setParents()
//        if (includeLocation) {
//          jdkLogger.includeLocation = true
//        }
//      }
//      return jdkLogger
    } finally {
      bridgeTreeLock.unlock()
    }
  }

  override fun setLoggerFilter(logger: Logger, filter: LoggerFilter) {
    bridgeTreeLock.lock()
    try {
      val loggerName = logger.name
      val bridge = getBridge(loggerName)
      if (bridge.name == loggerName) {
        bridge.setFilter(filter)
      } else {
        makeNewBridge(bridge, loggerName, filter, null, null)
      }
    } finally {
      bridgeTreeLock.unlock()
    }
  }

  override fun addLoggerHandler(logger: Logger, loggerHandler: BaseLoggerHandler) {
    bridgeTreeLock.lock()
    try {
      val loggerName = logger.name
      val bridge = getBridge(loggerName)
      if (bridge.name == loggerName) {
        bridge.addLoggerHandler(loggerHandler)
      } else {
        makeNewBridge(bridge, loggerName, null, loggerHandler, null)
      }
    } finally {
      bridgeTreeLock.unlock()
    }
  }

  override fun setLogLevel(logger: Logger, logLevel: LogLevel) {
    bridgeTreeLock.lock()
    try {
      val loggerName = logger.name
      val bridge = getBridge(loggerName)
      if (bridge.name == loggerName) {
        bridge.logLevel = logLevel
      } else {
        makeNewBridge(bridge, loggerName, null, null, logLevel)
      }
    } finally {
      bridgeTreeLock.unlock()
    }
  }

  override fun setLogToParent(logger: Logger, logToParent: Boolean) {
    bridgeTreeLock.lock()
    try {
      val loggerName = logger.name
      val bridge = getBridge(loggerName)
      if (bridge.name == loggerName) {
        bridge.setLogToParent(logToParent)
      } else {
        makeNewBridge(bridge, loggerName, null, null, null).setLogToParent(logToParent)
      }
    } finally {
      bridgeTreeLock.unlock()
    }
  }

  override fun setIncludeLocation(logger: Logger, includeLocation: Boolean) {
    bridgeTreeLock.lock()
    try {
      val loggerName = logger.name
      val bridge = getBridge(loggerName)
      if (bridge.name == loggerName) {
        bridge.includeLocation = includeLocation
      } else {
        makeNewBridge(bridge, loggerName, null, null, null).includeLocation = includeLocation
      }
    } finally {
      bridgeTreeLock.unlock()
    }
  }

  private fun makeNewBridge(
    parent: JdkBridge,
    loggerName: String,
    filter: LoggerFilter?,
    handler: BaseLoggerHandler?,
    logLevel: LogLevel?
  ): JdkBridge {
    val newBridge = JdkBridge(loggerName, filter, handler, logLevel)
    newBridge.parent = parent
    bridgeMap.putIfAbsent(loggerName, newBridge)
    setParents()
    updateLoggers()
    return newBridge
  }

  private fun updateLoggers() {
    for (logger in loggerMap.values) {
      logger.update(this)
    }
  }

  override fun getBridge(loggerClassName: String): JdkBridge {
    var bridge: JdkBridge? = bridgeMap[loggerClassName]
    if (bridge != null) {
      return bridge
    }
    var className: String? = getParentName(loggerClassName)
    while (className != null) {
      bridge = bridgeMap[className]
      if (bridge != null) {
        return bridge
      }
      className = getParentName(className)
    }
    return jdkBridgeRoot
  }

  private fun setParents() {
    for (entry in bridgeMap.entries) {
      val bridge = entry.value
      var key = entry.key
      if (key.isNotEmpty()) {
        val i = key.lastIndexOf('.')
        if (i > 0) {
          key = key.substring(0, i)
          var parent: JdkBridge? = getBridge(key)
          if (parent == null) {
            parent = jdkBridgeRoot
          }
          bridge.parent = parent
        } else {
          bridge.parent = jdkBridgeRoot
        }
      }
    }
  }

  private fun getParentName(name: String): String? {
    if (name.isEmpty()) {
      return null
    }
    val i = name.lastIndexOf('.')
    return if (i > 0) name.substring(0, i) else ""
  }
}