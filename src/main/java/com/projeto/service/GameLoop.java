package com.projeto.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GameLoop {

    private final GameService gameService;

    public GameLoop(GameService gameService) {
        this.gameService = gameService;
    }

    @Scheduled(fixedRate = 16)
    public void loop() throws IOException {
        gameService.stepAllSalas();
    }
}