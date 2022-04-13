package com.camunda.consulting.simulator.jobhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.jobQuery;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.junit.Assert.assertNull;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.camunda.consulting.simulator.PayloadGenerator;
import com.camunda.consulting.simulator.SimulationExecutor;
import com.camunda.consulting.simulator.SimulatorPlugin;
import com.camunda.consulting.simulator.TestHelper;

@Deployment(resources = "externalTaskCompleteModel.bpmn")
public class CompleteExternalTaskJobHandlerTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    if (BpmPlatform.getDefaultProcessEngine() == null) {
      RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(rule.getProcessEngine());
    }
    init(rule.getProcessEngine());
    TestHelper.removeCustomJobs(rule.getProcessEngine());
  }

  @Test
  public void shouldExecuteCompleteExternalTaskJob() {

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("externalTaskComplete");

    assertThat(processInstance).isStarted().isWaitingAt("externalTask");
    assertThat(jobQuery().count()).isEqualTo(1); // simulation job was created

    SimulationExecutor.execute(DateTime.now().minusMinutes(5).toDate(), DateTime.now().plusMinutes(5).toDate());

    assertThat(processInstance).isEnded();

  }

  @Test
  public void shouldDeleteAllExternalTaskJobs() {

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("externalTaskComplete");

    assertThat(processInstance).isStarted().isWaitingAt("externalTask");
    assertThat(jobQuery().count()).isEqualTo(1); // simulation job was created

    SimulatorPlugin.deleteAllExternalTasks();

    long numberOfExternalTasks = rule.getProcessEngine().getExternalTaskService().createExternalTaskQuery().count();

    assertThat(numberOfExternalTasks).isEqualTo(0);

  }

}