/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.incubator.semconv.messaging;

import java.util.Locale;

/**
 * Represents type of <a
 * href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/messaging/messaging-spans.md#operation-types">operations</a>
 * that may be used in a messaging system.
 */
public enum MessageOperation {
  CREATE,
  PUBLISH,
  RECEIVE,
  PROCESS,
  SETTLE;

  /**
   * Returns the operation name as defined in <a
   * href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/messaging/messaging-spans.md#operation-types">the
   * specification</a>.
   */
  String operationName() {
    return name().toLowerCase(Locale.ROOT);
  }
}
