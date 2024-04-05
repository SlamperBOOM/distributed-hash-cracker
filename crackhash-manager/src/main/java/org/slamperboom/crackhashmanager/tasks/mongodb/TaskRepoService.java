package org.slamperboom.crackhashmanager.tasks.mongodb;

import org.slamperboom.crackhashmanager.tasks.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Transactional
public class TaskRepoService {
    private final TaskRepo taskRepo;

    @Autowired
    public TaskRepoService(TaskRepo taskRepo) {
        this.taskRepo = taskRepo;
    }

    @Transactional(readOnly = true)
    public List<CrackTask> getUnfinishedTasks(){
        return taskRepo.findNotFinished();
    }

    public void saveTask(CrackTask crackTask){
        taskRepo.save(crackTask);
    }

    public boolean updateStatus(long taskId, TaskStatus newStatus){
        AtomicBoolean result = new AtomicBoolean(false);
        taskRepo.findById(taskId).ifPresentOrElse(crackTask -> {
            if(!crackTask.getStatus().equals(TaskStatus.READY.toString())){
                crackTask.setStatus(newStatus.toString());
                taskRepo.save(crackTask);
                result.set(true);
            }else{
                result.set(false);
            }
        }, () -> result.set(false));
        return result.get();
    }

    public void heartBeatForTask(long taskId, boolean state){
        taskRepo.findById(taskId).ifPresent(crackTask -> {
            crackTask.setReceivedHeartBeat(state);
            taskRepo.save(crackTask);
        });
    }

    public void setDataForTask(long taskId, String data){
        taskRepo.findById(taskId).ifPresent(crackTask -> {
            crackTask.setData(data);
            taskRepo.save(crackTask);
        });
    }

    @Transactional(readOnly = true)
    public List<CrackTask> getTasksForRequest(String requestId){
        return taskRepo.findByRequestId(requestId);
    }

    public void removeTasks(List<CrackTask> taskList){
        taskRepo.deleteAll(taskList);
    }

    public long findLastId(){
        return taskRepo.findAll().stream().map(CrackTask::getTaskId).max(Long::compareTo).orElse(0L);
    }
}
