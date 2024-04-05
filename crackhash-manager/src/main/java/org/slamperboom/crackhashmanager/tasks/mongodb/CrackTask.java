package org.slamperboom.crackhashmanager.tasks.mongodb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slamperboom.crackhashmanager.DTOs.CrackTaskDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("tasks")
@AllArgsConstructor
@Getter
@Setter
public class CrackTask {
    @Id
    private Long taskId;
    private String status;
    private String hash;
    private int length;
    private String requestId;
    private int partNum;
    private int totalParts;
    private String data;
    private boolean isReceivedHeartBeat;

    public CrackTaskDTO getCrackTaskDTO(){
        return new CrackTaskDTO(taskId, hash, length, partNum, totalParts);
    }
}
