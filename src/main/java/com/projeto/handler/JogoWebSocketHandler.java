package com.projeto.handler;

import com.projeto.service.JogoService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class JogoWebSocketHandler extends TextWebSocketHandler {

    private final JogoService jogoService;

    public JogoWebSocketHandler(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        System.out.println("Jogador conectado: " + session.getId());

        session.sendMessage(new TextMessage("Conectado ao servidor"));

    }

    @Override
    protected void handleTextMessage(WebSocketSession session,
                                     TextMessage message) {

        jogoService.processarMensagem(
                session,
                message.getPayload()
        );

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status) {

        System.out.println("Jogador saiu: " + session.getId());

    }

}