package com.app.fairfree.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Hashtable;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/public/welcome", produces = MediaType.APPLICATION_JSON_VALUE)
public class WelcomeController {

    @GetMapping("")
    public ResponseEntity<Map<String, String>> getGreetingMessage() {
        Map<String, String> messageResp = new Hashtable<>();
        messageResp.put("message", "Welcome to FairFree Application.");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(messageResp);
    }
}
