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

package com.ealva.ealvalog.impl;

import com.ealva.ealvalog.FilterResult;
import com.ealva.ealvalog.Logger;
import com.ealva.ealvalog.NullMarker;
import com.ealva.ealvalog.core.ExtLogRecord;
import com.ealva.ealvalog.core.ExtRecordFormatter;
import com.ealva.ealvalog.filter.AlwaysAcceptFilter;
import com.ealva.ealvalog.util.NullThrowable;

import static com.ealva.ealvalog.LogLevel.CRITICAL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class AndroidLoggerHandlerTest {
  @SuppressWarnings("WeakerAccess") @Mock Logger logger;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testIsLoggableNoFilter() {
    when(logger.getName()).thenReturn(AndroidLoggerHandlerTest.class.getName());

    AndroidLoggerHandler handler = AndroidLoggerHandler.Companion.make();
    assertThat(handler.isLoggable(logger, CRITICAL, NullMarker.INSTANCE,
                                  NullThrowable.INSTANCE),
               is(FilterResult.NEUTRAL));
  }

  @Test
  public void testIsLoggableAlwaysAcceptFilter() {
    when(logger.getName()).thenReturn(AndroidLoggerHandlerTest.class.getName());

    AndroidLoggerHandler handler = AndroidLoggerHandler.Companion.make(
        new ExtRecordFormatter(ExtRecordFormatter.TYPICAL_ANDROID_FORMAT, true),
        AlwaysAcceptFilter.INSTANCE
    );
    assertThat(handler.isLoggable(logger, CRITICAL, NullMarker.INSTANCE,
                                  NullThrowable.INSTANCE),
               is(FilterResult.ACCEPT));
  }

  @Test
  public void testLogOutput() {
    AndroidLoggerHandler handler = AndroidLoggerHandler.Companion.make(
        new ExtRecordFormatter(ExtRecordFormatter.TYPICAL_ANDROID_FORMAT, true),
        AlwaysAcceptFilter.INSTANCE
    );
    handler.publish(ExtLogRecord.Companion.get(CRITICAL,
                                               "Message",
                                               getClass().getName(),
                                               null,
                                               null,
                                               null));
  }
}