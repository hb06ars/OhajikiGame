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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService {

    private final Map<String, Sala> salas = new ConcurrentHashMap<>();
    private final SalaService salaService;
    private final JogadaService jogadaService;
    private final StepService stepService;
    private final JsonUtils jsonUtils;
    private final Set<WebSocketSession> lobbySessions = ConcurrentHashMap.newKeySet();

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
                salaService.atualizarLobby(lobbySessions, salas);
                break;

            case "ENTRAR_SALA":
                salaService.removerLobby(lobbySessions, session);
                salaService.entrarSala(session, dto.getSala(), salas);
                salaService.atualizarLobby(lobbySessions, salas);
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

    public void desconectar(WebSocketSession session) throws IOException {
        String salaRemover = null;

        for (Sala sala : salas.values()) {

            if (sala.getAzul() != null && sala.getAzul().getId().equals(session.getId())) {
                salaRemover = sala.getCodigo();
                break;
            }

            if (sala.getVermelho() != null && sala.getVermelho().getId().equals(session.getId())) {
                sala.setVermelho(null);
                salaService.atualizarLobby(lobbySessions, salas);
                return;
            }
        }

        if (salaRemover != null) {
            salas.remove(salaRemover);
            salaService.atualizarLobby(lobbySessions, salas);
        }
    }

    public void adicionarLobby(WebSocketSession session) {
        salaService.adicionarLobby(lobbySessions, session);
    }

    public void removerLobby(WebSocketSession session) {
        salaService.removerLobby(lobbySessions, session);
    }

    public void atualizarLobby() throws IOException {
        salaService.atualizarLobby(lobbySessions, salas);
    }
}