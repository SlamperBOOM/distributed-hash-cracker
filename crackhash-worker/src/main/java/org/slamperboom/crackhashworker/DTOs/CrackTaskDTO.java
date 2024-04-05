package org.slamperboom.crackhashworker.DTOs;

public record CrackTaskDTO(long taskId, String hash, int length, int partNum, int totalParts){
}
