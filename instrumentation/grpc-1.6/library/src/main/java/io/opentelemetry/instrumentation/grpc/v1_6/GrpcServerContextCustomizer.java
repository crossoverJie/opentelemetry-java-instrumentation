package io.opentelemetry.instrumentation.grpc.v1_6;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.ContextCustomizer;
import io.opentelemetry.semconv.incubating.RpcIncubatingAttributes;

public class GrpcServerContextCustomizer implements ContextCustomizer<GrpcRequest> {
  private final String currentServiceName;

  private static final String PARENT_RPC_KEY = "parent_rpc";
  private static final String CURRENT_RPC_KEY = "current_rpc";

  private static final String CURRENT_HTTP_URL_PATH = "current_http_url_path";

  public GrpcServerContextCustomizer(String serviceName) {
    this.currentServiceName = serviceName;
  }

  @Override
  public Context onStart(Context parentContext, GrpcRequest grpcRequest,
      Attributes startAttributeds) {
    BaggageBuilder builder = Baggage.fromContext(parentContext).toBuilder();

    String currentRpc = Baggage.fromContext(parentContext).getEntryValue(CURRENT_RPC_KEY);
    String fullMethodName = startAttributeds.get(AttributeKey.stringKey(RpcIncubatingAttributes.RPC_METHOD.getKey()));
    String rpcService = startAttributeds.get(AttributeKey.stringKey(RpcIncubatingAttributes.RPC_SERVICE.getKey()));
    // call from grpc
    String method = rpcService + "." + fullMethodName;
    String baggageInfo = getBaggageInfo(currentServiceName, method);

    String httpUrlPath = Baggage.fromContext(parentContext).getEntryValue(CURRENT_HTTP_URL_PATH);
    if (!StringUtils.isNullOrEmpty(httpUrlPath)) {
      // call from http
      // currentRpc = currentRpc;  currentRpc = create1|GET:/request
      // clear current_http_url_path
      builder.put(CURRENT_HTTP_URL_PATH, "");
    }

    Baggage baggage = builder
        .put(PARENT_RPC_KEY, currentRpc)
        .put(CURRENT_RPC_KEY, baggageInfo)
        .build();
    return parentContext.with(baggage);

  }

  private static String getBaggageInfo(String serviceName, String method) {
    if (StringUtils.isNullOrEmpty(serviceName)) {
      return "";
    }
    return serviceName + "|" + method;
  }

}
