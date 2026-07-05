package com.projeto.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.enums.StatusEnum;
import com.projeto.service.JogoService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class JogoWebSocketHandler extends TextWebSocketHandler {

    private final JogoService jogoService;
    private final ObjectMapper mapper = new ObjectMapper();

    public JogoWebSocketHandler(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        System.out.println("Jogador conectado: " + session.getId());

        var resposta = MensagemDTO.builder().tipo(StatusEnum.CONECTADO.name()).sala(null)
                .mensagem("Conectado ao servidor").build();

        session.sendMessage(new TextMessage(mapper.writeValueAsString(resposta)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var dto = mapper.readValue(message.getPayload(), MensagemDTO.class);
        jogoService.processarMensagem(session, dto);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("Jogador saiu: " + session.getId());
    }

}