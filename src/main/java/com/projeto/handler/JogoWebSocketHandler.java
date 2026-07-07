package com.projeto.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.enums.StatusEnum;
import com.projeto.service.GameService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JogoWebSocketHandler extends TextWebSocketHandler {

    private final GameService gameService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<WebSocketSession> lobbySessions = ConcurrentHashMap.newKeySet();

    public JogoWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // JOGADOR CONECTADO
        lobbySessions.add(session);
        gameService.adicionarLobby(session);

        var resposta = MensagemDTO.builder()
                .tipo(StatusEnum.CONECTADO.name())
                .sala(null)
                .mensagem("Conectado ao servidor")
                .build();

        session.sendMessage(new TextMessage(mapper.writeValueAsString(resposta)));
        gameService.enviarListaSalas(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var dto = mapper.readValue(message.getPayload(), MensagemDTO.class);
        gameService.processarMensagem(session, dto);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        // JOGADOR DESCONECTADO
        try{
            gameService.desconectar(session);
            gameService.removerLobby(session);
            gameService.atualizarLobby();
        } catch(Exception e){
            System.out.println("Sessão já foi fechada: " + e);
        }
    }

}