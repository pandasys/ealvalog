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

package com.ealva.ealvalog

/**
 * It's expected all logging occurs through concrete implementations of this interface which are
 * obtained via logger factories and singletons
 *
 * Created by Eric A. Snell on 2/28/17.
 */
interface Logger {

  /**
   * The name of the logger. If the underlying logger is anonymous (name is null), then
   * [ANONYMOUS_NAME] is returned
   */
  val name: String

  /**
   * The [Marker] to use for all logging from this logger, superseded by any marker passed during
   * a logging call.
   */
  var marker: Marker?

  var filter: LoggerFilter

  /**
   * An optional LogLevel. If null and the Logger implementation is
   * hierarchical, the nearest ancestor LogLevel will be used as the level for this logger
   *
   * If null is passed to the root logger, it sets the level to [LogLevel.NONE]
   */
  var logLevel: LogLevel?

  /**
   * The level set for this logger or, if that it has not been set, get the nearest ancestor
   * log level if the Loggers are hierarchical
   */
  val effectiveLogLevel: LogLevel

  /**
   * Set if this logger should include call site location information in the log information. This
   * is a relatively expensive operation and defaults to false. Even if this is false a lower level
   * handler may request location be included. See [shouldIncludeLocation]
   *
   * The information passed to lower layers of the logging framework include: caller class,
   * caller method, and caller file line number. This is obtained from the call stack. If calling
   * code is obfuscated the results will also be obfuscated.
   *
   * Also note that the underlying log statement formatter must be configured to include this
   * information.
   */
  var includeLocation: Boolean

  /**
   * Determine if a log call will include location information. If [includeLocation] is true or a
   * lower level handler requests location information, it is obtained from the stack. Obtaining
   * the location is a relatively expensive operation.
   */
  fun shouldIncludeLocation(logLevel: LogLevel, marker: Marker?, throwable: Throwable?): Boolean

  /**
   * Determine if a log at this [LogLevel], with the given (optional) [Marker] and (optional)
   * [Throwable], result in an actual log statement
   */
  fun isLoggable(
    level: LogLevel,
    marker: Marker? = null,
    throwable: Throwable? = null
  ): Boolean

  /**
   * Obtain a [LogEntry] for use with this logger.
   */
  fun getLogEntry(
    logLevel: LogLevel,
    marker: Marker?,
    throwable: Throwable?,
    mdcContext: MdcContext?
  ): LogEntry

  /**
   * Log without checking the the level. This method's primary use is for this logging framework
   * and it's not expected client's would typically use this method. The Kotlin extension functions
   * and the JLogger subclass use this function internally.
   *
   * The [entry] is becomes "owned" by the Logger. Do not modify or reuse the [entry] after invoking
   * this function. This function will close the entry before returning.
   *
   * @param entry the full log information
   */
  fun logImmediate(entry: LogEntry)

  companion object {
    const val ANONYMOUS_NAME = "Anonymous Logger"
  }
}
