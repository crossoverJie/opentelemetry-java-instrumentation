/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.powerjob.v4_0;

import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.BASIC_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.BROADCAST_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.DYNAMIC_DATASOURCE_SQL_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.FILE_CLEANUP_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.HTTP_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.MAP_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.MAP_REDUCE_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.PYTHON_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.SHELL_PROCESSOR;
import static io.opentelemetry.javaagent.instrumentation.powerjob.v4_0.PowerJobConstants.SPRING_DATASOURCE_SQL_PROCESSOR;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static java.util.Arrays.asList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariDataSource;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.testing.junit.AgentInstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tech.powerjob.official.processors.impl.FileCleanupProcessor;
import tech.powerjob.official.processors.impl.HttpProcessor;
import tech.powerjob.official.processors.impl.script.PythonProcessor;
import tech.powerjob.official.processors.impl.script.ShellProcessor;
import tech.powerjob.official.processors.impl.sql.DynamicDatasourceSqlProcessor;
import tech.powerjob.official.processors.impl.sql.SpringDatasourceSqlProcessor;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.WorkflowContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.impl.OmsLocalLogger;

class PowerJobBasicProcessorTest {
  @RegisterExtension
  private static final InstrumentationExtension testing = AgentInstrumentationExtension.create();

  @Test
  void testBasicProcessor() throws Exception {
    long jobId = 1;
    String jobParam = "abc";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    BasicProcessor testBasicProcessor = new TestBasicProcessor();
    testBasicProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(String.format("%s.process", TestBasicProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.unset());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        TestBasicProcessor.class.getName(), jobId, jobParam, BASIC_PROCESSOR));
              });
        });
  }

  @Test
  void testBasicFailProcessor() throws Exception {
    long jobId = 1;
    String jobParam = "abc";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    BasicProcessor testBasicFailProcessor = new TestBasicFailProcessor();
    testBasicFailProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(
                    String.format("%s.process", TestBasicFailProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.error());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        TestBasicFailProcessor.class.getName(), jobId, jobParam, BASIC_PROCESSOR));
              });
        });
  }

  @Test
  void testBroadcastProcessor() throws Exception {
    long jobId = 1;
    String jobParam = "abc";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    BasicProcessor testBroadcastProcessor = new TestBroadcastProcessor();
    testBroadcastProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(
                    String.format("%s.process", TestBroadcastProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.unset());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        TestBroadcastProcessor.class.getName(),
                        jobId,
                        jobParam,
                        BROADCAST_PROCESSOR));
              });
        });
  }

  @Test
  void testMapProcessor() throws Exception {
    long jobId = 1;
    String jobParam = "abc";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    BasicProcessor testMapProcessProcessor = new TestMapProcessProcessor();
    testMapProcessProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(
                    String.format("%s.process", TestMapProcessProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.unset());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        TestMapProcessProcessor.class.getName(), jobId, jobParam, MAP_PROCESSOR));
              });
        });
  }

  @Test
  void testMapReduceProcessor() throws Exception {
    long jobId = 1;
    String jobParam = "abc";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    BasicProcessor testMapReduceProcessProcessor = new TestMapReduceProcessProcessor();
    testMapReduceProcessProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(
                    String.format(
                        "%s.process", TestMapReduceProcessProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.unset());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        TestMapReduceProcessProcessor.class.getName(),
                        jobId,
                        jobParam,
                        MAP_REDUCE_PROCESSOR));
              });
        });
  }

  @Test
  void testShellProcessor() throws Exception {
    long jobId = 1;
    String jobParam = "ls";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    taskContext.setWorkflowContext(new WorkflowContext(jobId, ""));
    taskContext.setOmsLogger(new OmsLocalLogger());
    BasicProcessor shellProcessor = new ShellProcessor();
    shellProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(String.format("%s.process", ShellProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.unset());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        ShellProcessor.class.getName(), jobId, jobParam, SHELL_PROCESSOR));
              });
        });
  }

  @Test
  void testPythonProcessor() throws Exception {
    long jobId = 1;
    String jobParam = "1+1";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    taskContext.setWorkflowContext(new WorkflowContext(jobId, ""));
    taskContext.setOmsLogger(new OmsLocalLogger());
    BasicProcessor pythonProcessor = new PythonProcessor();
    pythonProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(String.format("%s.process", PythonProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.unset());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        PythonProcessor.class.getName(), jobId, jobParam, PYTHON_PROCESSOR));
              });
        });
  }

  @Test
  void testHttpProcessor() throws Exception {

    long jobId = 1;
    String jobParam = "{\"method\":\"GET\"}";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    taskContext.setWorkflowContext(new WorkflowContext(jobId, ""));
    taskContext.setOmsLogger(new OmsLocalLogger());
    BasicProcessor httpProcessor = new HttpProcessor();
    httpProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(String.format("%s.process", HttpProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.error());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        HttpProcessor.class.getName(), jobId, jobParam, HTTP_PROCESSOR));
              });
        });
  }

  @Test
  void testFileCleanerProcessor() throws Exception {

    long jobId = 1;
    JSONObject params = new JSONObject();
    params.put("dirPath", "/abc");
    params.put("filePattern", "[\\s\\S]*log");
    params.put("retentionTime", 0);
    JSONArray array = new JSONArray();
    array.add(params);
    String jobParam = array.toJSONString();
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    taskContext.setOmsLogger(new OmsLocalLogger());
    BasicProcessor fileCleanupProcessor = new FileCleanupProcessor();
    fileCleanupProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(
                    String.format("%s.process", FileCleanupProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.unset());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        FileCleanupProcessor.class.getName(),
                        jobId,
                        jobParam,
                        FILE_CLEANUP_PROCESSOR));
              });
        });
  }

  @Test
  void testSpringDataSourceProcessor() throws Exception {
    DataSource dataSource = new HikariDataSource();
    long jobId = 1;
    String jobParam = "{\"dirPath\":\"/abc\"}";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    taskContext.setWorkflowContext(new WorkflowContext(jobId, ""));
    taskContext.setOmsLogger(new OmsLocalLogger());
    BasicProcessor springDatasourceSqlProcessor = new SpringDatasourceSqlProcessor(dataSource);
    springDatasourceSqlProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(
                    String.format(
                        "%s.process", SpringDatasourceSqlProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.error());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        SpringDatasourceSqlProcessor.class.getName(),
                        jobId,
                        jobParam,
                        SPRING_DATASOURCE_SQL_PROCESSOR));
              });
        });
  }

  @Test
  void testDynamicDataSourceProcessor() throws Exception {

    long jobId = 1;
    String jobParam = "{\"dirPath\":\"/abc\"}";
    TaskContext taskContext = genTaskContext(jobId, jobParam);
    taskContext.setWorkflowContext(new WorkflowContext(jobId, ""));
    taskContext.setOmsLogger(new OmsLocalLogger());
    BasicProcessor dynamicDatasourceSqlProcessor = new DynamicDatasourceSqlProcessor();
    dynamicDatasourceSqlProcessor.process(taskContext);
    testing.waitAndAssertTraces(
        trace -> {
          trace.hasSpansSatisfyingExactly(
              span -> {
                span.hasName(
                    String.format(
                        "%s.process", DynamicDatasourceSqlProcessor.class.getSimpleName()));
                span.hasKind(SpanKind.INTERNAL);
                span.hasStatus(StatusData.error());
                span.hasAttributesSatisfying(
                    attributeAssertions(
                        DynamicDatasourceSqlProcessor.class.getName(),
                        jobId,
                        jobParam,
                        DYNAMIC_DATASOURCE_SQL_PROCESSOR));
              });
        });
  }

  private static TaskContext genTaskContext(long jobId, String jobParam) {
    TaskContext taskContext = new TaskContext();
    taskContext.setJobId(jobId);
    taskContext.setJobParams(jobParam);
    return taskContext;
  }

  private static List<AttributeAssertion> attributeAssertions(
      String codeNamespace, long jobId, String jobParam, String jobType) {
    List<AttributeAssertion> attributeAssertions =
        new ArrayList<>(
            asList(
                equalTo(AttributeKey.stringKey("code.namespace"), codeNamespace),
                equalTo(AttributeKey.stringKey("code.function"), "process"),
                equalTo(AttributeKey.stringKey("job.system"), "powerjob"),
                equalTo(AttributeKey.longKey("scheduling.powerjob.job.id"), jobId),
                equalTo(AttributeKey.stringKey("scheduling.powerjob.job.type"), jobType)));
    if (!StringUtils.isNullOrEmpty(jobParam)) {
      attributeAssertions.add(
          equalTo(AttributeKey.stringKey("scheduling.powerjob.job.param"), jobParam));
    }
    return attributeAssertions;
  }
}