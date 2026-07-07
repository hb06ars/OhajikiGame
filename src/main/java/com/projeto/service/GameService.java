package com.projeto.service;

import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.dto.Sala;
import com.projeto.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService {

    private final Map<String, Sala> salas = new ConcurrentHashMap<>();
    private final SalaService salaService;
    private final JogadaService jogadaService;
    private final StepService stepService;
    private final JsonUtils jsonUtils;

    public void stepAllSalas() throws IOException {
        for (Sala sala : salas.values()) {
            stepService.executar(sala);
            stepService.enviarEstado(sala);
        }
    }

    public void processarMensagem(WebSocketSession session, MensagemDTO dto) throws IOException {
        switch (dto.getTipo()) {
            case "CRIAR_SALA":
                salaService.criarSala(session, salas);
                salaService.listarSalasDisponiveis(salas);
                break;

            case "ENTRAR_SALA":
                salaService.entrarSala(session, dto.getSala(), salas);
                break;

            case "JOGADA":
                jogadaService.jogar(session, dto, salas);
                break;

            default:
                break;

        }
    }

    public void enviarListaSalas(WebSocketSession session) throws IOException {

        var dto = MensagemDTO.builder()
                .tipo("LISTA_SALAS")
                .salas(salaService.listarSalasDisponiveis(salas))
                .build();

        session.sendMessage(new TextMessage(jsonUtils.toJson(dto)));
    }
}