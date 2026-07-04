package com.projeto.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GameLoop {

    private final JogoService jogoService;

    public GameLoop(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    @Scheduled(fixedRate = 16)
    public void loop() throws IOException {
        jogoService.stepAllSalas();
    }
}