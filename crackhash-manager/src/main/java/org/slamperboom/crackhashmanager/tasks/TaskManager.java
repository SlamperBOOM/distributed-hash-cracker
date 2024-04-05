package org.slamperboom.crackhashmanager.tasks;

import org.slamperboom.crackhashmanager.DTOs.CrackClientResultDTO;
import org.slamperboom.crackhashmanager.rabbitmq.ManagerTaskQueueManager;
import org.slamperboom.crackhashmanager.tasks.mongodb.CrackTask;
import org.slamperboom.crackhashmanager.tasks.mongodb.TaskRepoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.LinkedList;
import java.util.List;

@Component
@ApplicationScope
public class TaskManager {
    private long nextTaskId;
    private static final int partCount = 10;
    private final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private final TaskRepoService taskRepoService;

    @Autowired
    public TaskManager(ManagerTaskQueueManager queueManager, TaskRepoService taskRepoService) {
        new TaskManagerThread(this, queueManager);
        this.taskRepoService = taskRepoService;
        nextTaskId = taskRepoService.findLastId() + 1;
    }

    public synchronized void addTask(String hash, int length, String requestId){
        for(int i=0; i<partCount; ++i){
            taskRepoService.saveTask(
                    new CrackTask(nextTaskId, "WAITING", hash, length,
                            requestId, i, partCount, null, false)
            );
            logger.info("TaskId: {}, status: WAITING", nextTaskId);
            nextTaskId++;
        }
    }

    public synchronized List<CrackTask> getNotFinishedTasks(){
        return taskRepoService.getUnfinishedTasks();
    }

    public synchronized CrackClientResultDTO getAccumulatedRequestResult(String requestId){
        var taskList = taskRepoService.getTasksForRequest(requestId);
        if(taskList.isEmpty()){
            return new CrackClientResultDTO("REQUEST NOT FOUND");
        }
        long finishedCount = taskList.stream().filter(task -> task.getStatus().equals(TaskStatus.READY.toString())).count();
        if(finishedCount < taskList.size()){
            return new CrackClientResultDTO("IN PROGRESS");
        }else{
            var task = taskList.stream().filter(t -> t.getData() != null && !t.getData().isEmpty()).findFirst();
            if(taskList.size() > 1) {
                logger.info("Compress request data. RequestId: {}", requestId);
            }
            if(task.isPresent()){
                List<CrackTask> partsToDelete = new LinkedList<>();
                taskList.stream()
                        .filter(t ->
                                t.getRequestId().equals(requestId) && t.getData().isEmpty()
                        ).forEach(partsToDelete::add);
                taskRepoService.removeTasks(partsToDelete);
                return new CrackClientResultDTO("READY", task.get().getData());
            }else{
                var oneTask = taskList.get(0);
                taskRepoService.removeTasks(taskList);
                taskRepoService.saveTask(oneTask);
                return new CrackClientResultDTO("READY, FAILED", "");
            }
        }
    }

    public synchronized void updateStatus(long taskId, TaskStatus status){
        if(taskRepoService.updateStatus(taskId, status)) {
            logger.info("TaskId: {}, status: {}", taskId, status);
        }
    }

    public synchronized void resetHeartBeat(long taskId){
        taskRepoService.heartBeatForTask(taskId, false);
    }

    public synchronized void processHealthCheck(long taskId){
        taskRepoService.heartBeatForTask(taskId, true);
        logger.info("TaskId: {}, received heartbeat", taskId);
        taskRepoService.updateStatus(taskId, TaskStatus.IN_PROGRESS);
    }

    public synchronized void updateTask(long taskId, String data){
        taskRepoService.setDataForTask(taskId, data);
        updateStatus(taskId, TaskStatus.READY);
    }
}
