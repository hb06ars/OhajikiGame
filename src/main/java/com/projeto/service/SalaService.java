package com.projeto.service;

import com.projeto.model.dto.AlertaDTO;
import com.projeto.model.dto.EstadoPartida;
import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.dto.Sala;
import com.projeto.model.enums.StatusEnum;
import com.projeto.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.projeto.utils.Constants.AZUL;
import static com.projeto.utils.Constants.VERMELHO;

@Component
@RequiredArgsConstructor
public class SalaService {

    private final JsonUtils jsonUtils;
    private final DiscoService discoService;

    public void criarSala(WebSocketSession session, Map<String, Sala> salas) throws IOException {
        var codigo = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        var sala = new Sala();
        sala.setCodigo(codigo);
        sala.setAzul(session);

        var estado = new EstadoPartida();
        estado.setVez(AZUL);
        estado.setPontosAzul(0);
        estado.setPontosVermelho(0);
        estado.setJogando(false);
        estado.setDiscos(discoService.criarDiscos());

        sala.setEstado(estado);
        salas.put(codigo, sala);


        var resposta = MensagemDTO.builder()
                .tipo(StatusEnum.SALA_CRIADA.name())
                .sala(codigo)
                .mensagem("Sala criada: " + codigo)
                .build();

        System.out.println("Sala criada: " + codigo);
        session.sendMessage(new TextMessage(jsonUtils.toJson(resposta)));
    }

    public void entrarSala(WebSocketSession session, String codigo, Map<String, Sala> salas) throws IOException {
        var sala = salas.get(codigo);
        if (sala == null || (sala.getVermelho() != null && !sala.getAzul().getId().equals(sala.getVermelho().getId()))) {
            if (sala == null) {
                enviarMensagemSalaInexistente(session);
            } else {
                enviarMensagemSalaOcupada(session);
            }
            return;
        }
        sala.setVermelho(session);

        var azul = montarSala(codigo, sala, AZUL);
        var vermelho = montarSala(codigo, sala, VERMELHO);

        sala.getAzul().sendMessage(new TextMessage(jsonUtils.toJson(azul)));
        sala.getVermelho().sendMessage(new TextMessage(jsonUtils.toJson(vermelho)));

    }

    private void enviarMensagemSalaOcupada(WebSocketSession session) throws IOException {
        session.sendMessage(new TextMessage(jsonUtils.toJson(AlertaDTO.builder()
                .tipo(StatusEnum.SALA_OCUPADA)
                .mensagem("A sala já está ocupada!").build())));
    }

    private void enviarMensagemSalaInexistente(WebSocketSession session) throws IOException {
        session.sendMessage(new TextMessage(jsonUtils.toJson(AlertaDTO.builder()
                .tipo(StatusEnum.SALA_INEXISTENTE)
                .mensagem("A sala não existe!").build())));
    }

    private static MensagemDTO montarSala(String codigo, Sala sala, String jogador) {
        return MensagemDTO.builder()
                .tipo(StatusEnum.PARTIDA_INICIADA.name())
                .sala(codigo)
                .estado(sala.getEstado())
                .jogador(jogador)
                .build();
    }

    public void enviarParaSala(Sala sala, MensagemDTO dto) throws IOException {
        if (sala == null) return;

        var json = jsonUtils.toJson(dto);

        if (sala.getAzul() != null && sala.getAzul().isOpen()) {
            sala.getAzul().sendMessage(new TextMessage(json));
        }

        if (sala.getVermelho() != null && sala.getVermelho().isOpen()) {
            sala.getVermelho().sendMessage(new TextMessage(json));
        }
    }
}
