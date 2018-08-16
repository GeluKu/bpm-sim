package com.camunda.consulting.listener;

import com.camunda.consulting.jobhandler.CompleteUserTaskJobHandler;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

import java.util.Date;
import java.util.Optional;

public class UserTaskCompleteJobCreateListener extends AbstractJobCreateListener implements TaskListener{

  private static UserTaskCompleteJobCreateListener INSTANCE = null;
  
  public static TaskListener instance() {
    if (INSTANCE == null) {
      INSTANCE = new UserTaskCompleteJobCreateListener();
    }
    return INSTANCE;
  }

  @Override
    public void notify(DelegateTask task) {

      Optional<Expression> nextFireExpression = getCachedNextFireExpression(task.getExecution(), ((ExecutionEntity) task.getExecution()).getActivity());

      if(nextFireExpression.isPresent()) {
            Date dueDate = (Date) nextFireExpression.get().getValue(task.getExecution());
            createJobForUserTaskCompletion(task, dueDate);
        }
    }

    private void createJobForUserTaskCompletion(DelegateTask task, Date dueDate) {

      String jobHandlertype = CompleteUserTaskJobHandler.TYPE;
      CompleteUserTaskJobHandler.CompleteUserTaskJobHandlerConfiguration jobHandlerConfiguration = new CompleteUserTaskJobHandler.CompleteUserTaskJobHandlerConfiguration(task.getId());

      createTimerJob((ExecutionEntity)task.getExecution(), jobHandlertype, dueDate, jobHandlerConfiguration);

    }

}
