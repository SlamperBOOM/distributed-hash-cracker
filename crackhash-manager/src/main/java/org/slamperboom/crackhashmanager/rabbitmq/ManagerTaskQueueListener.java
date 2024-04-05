package org.slamperboom.crackhashmanager.rabbitmq;

import org.slamperboom.crackhashmanager.DTOs.CrackTaskDTO;
import org.slamperboom.crackhashmanager.DTOs.CrackWorkerResultDTO;
import org.slamperboom.crackhashmanager.tasks.TaskManager;
import org.slamperboom.crackhashmanager.tasks.TaskStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.slamperboom.crackhashmanager.rabbitmq.ManagerToWorkerRabbitMQConfig.managerToWorkerDeadQueueName;
import static org.slamperboom.crackhashmanager.rabbitmq.WorkerToManagerRabbitMQConfig.workerToManagerQueueName;

@Component
public class ManagerTaskQueueListener {

    private final TaskManager taskManager;

    @Autowired
    public ManagerTaskQueueListener(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @RabbitListener(queues = workerToManagerQueueName)
    public void listenForFinishedTasks(CrackWorkerResultDTO crackWorkerResultDTO){
        if(crackWorkerResultDTO.isHealthCheck()){
            taskManager.processHealthCheck(crackWorkerResultDTO.taskId());
        }else {
            taskManager.updateTask(crackWorkerResultDTO.taskId(), crackWorkerResultDTO.data());
        }
    }

    @RabbitListener(queues = managerToWorkerDeadQueueName)
    public void listenForRejectedMessages(CrackTaskDTO crackTaskDTO){
        taskManager.updateStatus(crackTaskDTO.taskId(), TaskStatus.WAITING);
    }
}
