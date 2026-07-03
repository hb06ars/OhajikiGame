package com.projeto.service;

import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.dto.Sala;
import com.projeto.model.enums.StatusEnum;
import com.projeto.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JogoService {

    private final Map<String, Sala> salas = new ConcurrentHashMap<>();
    private final JsonUtils jsonUtils;

    public void criarSala(WebSocketSession session) throws IOException {
        String codigo = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        Sala sala = new Sala();
        sala.setCodigo(codigo);
        sala.setAzul(session);
        salas.put(codigo, sala);


        MensagemDTO resposta = MensagemDTO.builder()
                .tipo(StatusEnum.SALA_CRIADA.name())
                .sala(codigo)
                .mensagem("Sala criada: " + codigo)
                .build();


        session.sendMessage(new TextMessage(jsonUtils.toJson(resposta)));
    }

    public void entrarSala(WebSocketSession session, String codigo) throws IOException {
        Sala sala = salas.get(codigo);
        if (sala == null) {
            return;
        }
        sala.setVermelho(session);

        MensagemDTO resposta = MensagemDTO.builder()
                .tipo(StatusEnum.PARTIDA_INICIADA.name())
                .sala(codigo)
                .mensagem("Iniciando partida na sala: " + codigo)
                .build();

        session.sendMessage(new TextMessage(jsonUtils.toJson(resposta)));

    }

    public void processarMensagem(WebSocketSession session, MensagemDTO dto) throws IOException {
        switch (dto.getTipo()) {
            case "CRIAR_SALA":
                criarSala(session);
                break;

            case "ENTRAR_SALA":
                entrarSala(session, dto.getSala());
                break;
        }
    }

}