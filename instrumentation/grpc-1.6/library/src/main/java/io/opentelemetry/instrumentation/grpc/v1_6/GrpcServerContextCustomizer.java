package io.opentelemetry.instrumentation.grpc.v1_6;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.ContextCustomizer;

public class GrpcServerContextCustomizer implements ContextCustomizer<GrpcRequest> {
  private final String currentServiceName;

  private static final String PARENT_RPC_KEY = "parent_rpc";
  private static final String CURRENT_RPC_KEY = "current_rpc";

  public GrpcServerContextCustomizer(String serviceName) {
    this.currentServiceName = serviceName;
  }

  @Override
  public Context onStart(Context parentContext, GrpcRequest grpcRequest,
      Attributes startAttributeds) {

    String currentPpc = Baggage.fromContext(parentContext).getEntryValue(CURRENT_RPC_KEY);
    String baggageInfo = getBaggageInfo(currentPpc, grpcRequest.getMethod().getFullMethodName());
    Baggage baggage = Baggage.fromContext(parentContext).toBuilder()
        .put(PARENT_RPC_KEY, baggageInfo)
        .put(CURRENT_RPC_KEY, currentServiceName)
        .build();
    System.out.println("====agent set baggage " + baggageInfo);
    return parentContext.with(baggage);

  }

  private static String getBaggageInfo(String serviceName, String method) {
    if (StringUtils.isNullOrEmpty(serviceName)) {
      return "";
    }
    return serviceName + "." + method;
  }

}
