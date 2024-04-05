package org.slamperboom.crackhashmanager.DTOs;

public class CrackClientResultDTO {
    private final String status;
    private String data;

    public CrackClientResultDTO(String status) {
        this.status = status;
    }

    public CrackClientResultDTO(String status, String data) {
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public String getData() {
        return data;
    }
}
