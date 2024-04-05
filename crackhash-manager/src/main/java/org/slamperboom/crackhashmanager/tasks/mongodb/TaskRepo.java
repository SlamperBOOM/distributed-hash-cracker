package org.slamperboom.crackhashmanager.tasks.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TaskRepo extends MongoRepository<CrackTask, Long> {
    List<CrackTask> findByRequestId(String requestId);

    @Query("{'status':{$not: {$eq:\"READY\"}}}")
    List<CrackTask> findNotFinished();
}
