package org.slamperboom.crackhashmanager.tasks;

import org.slamperboom.crackhashmanager.rabbitmq.ManagerTaskQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskManagerThread {
    private final TaskManager taskManager;
    private final ManagerTaskQueueManager queueManager;
    private final Logger logger = LoggerFactory.getLogger(TaskManagerThread.class);
    private static final int timeout = 10000;

    public TaskManagerThread(TaskManager taskManager, ManagerTaskQueueManager queueManager) {
        this.taskManager = taskManager;
        this.queueManager = queueManager;
        new Thread(this::checkerThreadFunc).start();
    }

    private void checkerThreadFunc(){
        while(Thread.currentThread().isAlive()){
            try{
                Thread.sleep(timeout);
            }catch (InterruptedException e){
                if(Thread.currentThread().isInterrupted()){
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            synchronized (taskManager) {
                var taskList = taskManager.getNotFinishedTasks();
                if (!taskList.isEmpty()) {
                    logger.info("------- Start checking tasks");
                }
                for (var task : taskList) {
                    Long taskId = task.getTaskId();
                    if (task.getStatus().equals(TaskStatus.IN_PROGRESS.toString()) && task.isReceivedHeartBeat()) {
                        taskManager.updateStatus(taskId, TaskStatus.IN_PROGRESS);
                        taskManager.resetHeartBeat(taskId);
                    } else {
                        if (!queueManager.uploadTask(task.getCrackTaskDTO())) {
                            taskManager.updateStatus(taskId, TaskStatus.WAITING);
                        } else {
                            logger.info("TaskId: {}, sent message in queue", taskId);
                        }
                    }
                }
                if (!taskList.isEmpty()) {
                    logger.info("------- End checking tasks");
                }
            }
        }
    }
}
