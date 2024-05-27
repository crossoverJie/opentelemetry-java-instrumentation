/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.semconv.http;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.ContextCustomizer;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.internal.HttpConstants;
import io.opentelemetry.instrumentation.api.internal.HttpRouteState;
import java.util.HashSet;
import java.util.Set;

/**
 * A builder of {@link HttpServerRoute}.
 *
 * @since 2.0.0
 */
public final class HttpServerRouteBuilder<REQUEST> {

  final HttpServerAttributesGetter<REQUEST, ?> getter;
  Set<String> knownMethods = HttpConstants.KNOWN_METHODS;

  final String serviceName;
  private static final String PARENT_RPC_KEY = "parent_rpc";
  private static final String CURRENT_RPC_KEY = "current_rpc";
  private static final String CURRENT_HTTP_URL_PATH = "current_http_url_path";

  HttpServerRouteBuilder(HttpServerAttributesGetter<REQUEST, ?> getter) {
    this.getter = getter;
    this.serviceName = "";
  }
  HttpServerRouteBuilder(HttpServerAttributesGetter<REQUEST, ?> getter, String serviceName) {
    this.getter = getter;
    this.serviceName = serviceName;
  }

  /**
   * Configures the customizer to recognize an alternative set of HTTP request methods.
   *
   * <p>By default, this customizer defines "known" methods as the ones listed in <a
   * href="https://www.rfc-editor.org/rfc/rfc9110.html#name-methods">RFC9110</a> and the PATCH
   * method defined in <a href="https://www.rfc-editor.org/rfc/rfc5789.html">RFC5789</a>. If an
   * unknown method is encountered, the customizer will use the value {@value HttpConstants#_OTHER}
   * instead.
   *
   * <p>Note: calling this method <b>overrides</b> the default known method sets completely; it does
   * not supplement it.
   *
   * @param knownMethods A set of recognized HTTP request methods.
   */
  @CanIgnoreReturnValue
  public HttpServerRouteBuilder<REQUEST> setKnownMethods(Set<String> knownMethods) {
    this.knownMethods = new HashSet<>(knownMethods);
    return this;
  }

  /**
   * Returns a {@link ContextCustomizer} that initializes an {@link HttpServerRoute} in the {@link
   * Context} returned from {@link Instrumenter#start(Context, Object)}. The returned customizer is
   * configured with the settings of this {@link HttpServerRouteBuilder}.
   *
   * @see InstrumenterBuilder#addContextCustomizer(ContextCustomizer)
   */
  public ContextCustomizer<REQUEST> build() {
    Set<String> knownMethods = new HashSet<>(this.knownMethods);
    return (context, request, startAttributes) -> {
      if (HttpRouteState.fromContextOrNull(context) != null) {
        return context;
      }
      String method = getter.getHttpRequestMethod(request);
      if (method == null || !knownMethods.contains(method)) {
        method = "HTTP";
      }
      String urlPath = getter.getUrlPath(request);
      String methodPath = method + " " + urlPath;

      String currentRpc = Baggage.fromContext(context).getEntryValue(CURRENT_RPC_KEY);
      String baggageInfo = getBaggageInfo(serviceName, methodPath);
      Baggage baggage = Baggage.fromContext(context).toBuilder()
          .put(PARENT_RPC_KEY, currentRpc)
          .put(CURRENT_RPC_KEY, baggageInfo)
          .put(CURRENT_HTTP_URL_PATH, methodPath)
          .build();
      return context.with(HttpRouteState.create(method, null, 0))
          .with(baggage);
    };
  }

  private static String getBaggageInfo(String serviceName, String method) {
    if (StringUtils.isNullOrEmpty(serviceName)) {
      return "";
    }
    return serviceName + "|" + method;
  }

}
