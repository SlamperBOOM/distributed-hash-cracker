package org.slamperboom.crackhashworker.worker;

import org.paukov.combinatorics.CombinatoricsFactory;
import org.paukov.combinatorics.ICombinatoricsVector;
import org.paukov.combinatorics.permutations.PermutationWithRepetitionGenerator;
import org.slamperboom.crackhashworker.DTOs.CrackTaskDTO;
import org.slamperboom.crackhashworker.DTOs.CrackWorkerResultDTO;
import org.slamperboom.crackhashworker.rabbitmq.WorkerTaskQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class Worker {
    private final WorkerTaskQueueManager queueManager;
    private final ICombinatoricsVector<Byte> alphabet;
    private final Logger logger = LoggerFactory.getLogger(Worker.class);

    private static final int healthCheckTimeout = 5000;
    private final AtomicBoolean isWorking = new AtomicBoolean(false);
    private final AtomicLong currentTaskId = new AtomicLong(-1);

    public Worker(WorkerTaskQueueManager queueManager) {
        this.queueManager = queueManager;
        alphabet = CombinatoricsFactory.createVector();
        for(int i=0; i<26; ++i){
            alphabet.addValue((byte)('a' + i));
            alphabet.addValue((byte)('A' + i));
        }
        new Thread(this::healthCheckThread).start();
    }

    public void workerThread(CrackTaskDTO task){
        logger.info("Working with task: {}", task);
        isWorking.set(true);
        currentTaskId.set(task.taskId());
        String data = "";
        StringBuilder taskHash = new StringBuilder(task.hash());
        for(int length = 1; length<=task.length(); ++length){
            if(!data.isEmpty()){
                break;
            }
            PermutationWithRepetitionGenerator<Byte> generator = new PermutationWithRepetitionGenerator<>(alphabet, length);
            long count = generator.getNumberOfGeneratedObjects();
            var iterator = generator.iterator();
            long index = 0;
            long start = (long)(count * (double)task.partNum() / task.totalParts());
            long end = (long)(count * ((double)task.partNum()+1) / task.totalParts());
            while(iterator.hasNext() && index < end && data.isEmpty()){
                if(index < start){
                    iterator.next();
                    index++;
                    continue;
                }
                var variant = iterator.next();
                byte[] byteArray = new byte[variant.getSize()];
                for(int i=0; i<variant.getSize(); ++i){
                    byteArray[i] = variant.getValue(i);
                }
                StringBuilder builder = new StringBuilder();
                builder = DigestUtils.appendMd5DigestAsHex(byteArray, builder);
                if(builder.compareTo(taskHash) == 0){
                    data = new String(byteArray);
                }
                index++;
            }
        }
        logger.info("Task finished. Found result: {}", data);
        queueManager.uploadFinishedTask(new CrackWorkerResultDTO(task.taskId(), data, false));
        isWorking.set(false);
        currentTaskId.set(-1);
    }

    public void healthCheckThread(){
        while(Thread.currentThread().isAlive()){
            try{
                Thread.sleep(healthCheckTimeout);
            }catch (InterruptedException e){
                if(Thread.currentThread().isInterrupted()){
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if(isWorking.get()){
                queueManager.sendHealthCheck(currentTaskId.get());
            }
        }
    }
}
