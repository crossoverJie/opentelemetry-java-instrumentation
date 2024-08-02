/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.kafka.internal;

import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessagingAttributesGetter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.apache.kafka.clients.producer.RecordMetadata;

enum KafkaConsumerAttributesGetter
    implements MessagingAttributesGetter<KafkaProcessRequest, RecordMetadata> {
  INSTANCE;

  @Override
  public String getSystem(KafkaProcessRequest request) {
    return "kafka";
  }

  @Override
  public String getDestination(KafkaProcessRequest request) {
    return request.getRecord().topic();
  }

  @Nullable
  @Override
  public String getDestinationTemplate(KafkaProcessRequest request) {
    return null;
  }

  @Override
  public boolean isTemporaryDestination(KafkaProcessRequest request) {
    return false;
  }

  @Override
  public boolean isAnonymousDestination(KafkaProcessRequest request) {
    return false;
  }

  @Override
  @Nullable
  public String getConversationId(KafkaProcessRequest request) {
    return null;
  }

  @Nullable
  @Override
  public Long getMessageBodySize(KafkaProcessRequest request) {
    long size = request.getRecord().serializedValueSize();
    return size >= 0 ? size : null;
  }

  @Nullable
  @Override
  public Long getMessageEnvelopeSize(KafkaProcessRequest request) {
    return null;
  }

  @Override
  @Nullable
  public String getMessageId(KafkaProcessRequest request, @Nullable RecordMetadata unused) {
    return null;
  }

  @Nullable
  @Override
  public String getClientId(KafkaProcessRequest request) {
    return request.getClientId();
  }

  @Nullable
  @Override
  public Long getBatchMessageCount(KafkaProcessRequest request, @Nullable RecordMetadata unused) {
    return null;
  }

  @Override
  public String getDestinationPartitionId(
      KafkaProcessRequest kafkaProcessRequest, @Nullable RecordMetadata recordMetadata) {
    if (recordMetadata != null) {
      return String.valueOf(recordMetadata.partition());
    } else {
      return null;
    }
  }

  @Override
  public List<String> getMessageHeader(KafkaProcessRequest request, String name) {
    return StreamSupport.stream(request.getRecord().headers().headers(name).spliterator(), false)
        .map(header -> new String(header.value(), StandardCharsets.UTF_8))
        .collect(Collectors.toList());
  }
}
