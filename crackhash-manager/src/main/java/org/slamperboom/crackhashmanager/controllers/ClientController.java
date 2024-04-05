package org.slamperboom.crackhashmanager.controllers;

import org.slamperboom.crackhashmanager.DTOs.CrackClientRequestDTO;
import org.slamperboom.crackhashmanager.DTOs.CrackClientResultDTO;
import org.slamperboom.crackhashmanager.tasks.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("api/hash")
public class ClientController {

    private final TaskManager taskManager;

    @Autowired
    public ClientController(TaskManager taskManager){
        this.taskManager = taskManager;
    }

    @PostMapping("crack")
    public String crackHash(@RequestBody CrackClientRequestDTO crackRequest){
        String requestId = UUID.randomUUID().toString();
        taskManager.addTask(crackRequest.hash(), crackRequest.length(), requestId);
        return requestId;
    }

    @GetMapping("status")
    public CrackClientResultDTO checkTask(@RequestParam String requestId){
        return taskManager.getAccumulatedRequestResult(requestId);
    }

    @GetMapping("makehash")
    public String makeHash(@RequestParam String word){
        return DigestUtils.md5DigestAsHex(word.getBytes(StandardCharsets.UTF_8));
    }
}
