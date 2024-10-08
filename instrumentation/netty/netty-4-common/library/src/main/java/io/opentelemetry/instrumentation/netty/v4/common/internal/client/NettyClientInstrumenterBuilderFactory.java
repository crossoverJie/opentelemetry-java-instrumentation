/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.netty.v4.common.internal.client;

import io.netty.handler.codec.http.HttpResponse;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.incubator.builder.internal.DefaultHttpClientInstrumenterBuilder;
import io.opentelemetry.instrumentation.netty.v4.common.HttpRequestAndChannel;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class NettyClientInstrumenterBuilderFactory {
  private NettyClientInstrumenterBuilderFactory() {}

  public static DefaultHttpClientInstrumenterBuilder<HttpRequestAndChannel, HttpResponse> create(
      String instrumentationName, OpenTelemetry openTelemetry) {

    return new DefaultHttpClientInstrumenterBuilder<>(
            instrumentationName, openTelemetry, new NettyHttpClientAttributesGetter())
        .setHeaderSetter(HttpRequestHeadersSetter.INSTANCE);
  }
}
