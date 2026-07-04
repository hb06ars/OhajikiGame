package com.projeto.service;

import com.projeto.model.dto.Disco;
import com.projeto.model.dto.EstadoPartida;
import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.dto.Ponto;
import com.projeto.model.dto.Sala;
import com.projeto.model.enums.StatusEnum;
import com.projeto.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JogoService {

    private final Map<String, Sala> salas = new ConcurrentHashMap<>();
    private final JsonUtils jsonUtils;

    private static final double CENTRO_X = 500;
    private static final double CENTRO_Y = 500;
    private static final double RAIO_MESA = 400;
    private static final double RAIO_DISCO = 20;

    public void processarMensagem(WebSocketSession session, MensagemDTO dto) throws IOException {
        switch (dto.getTipo()) {
            case "CRIAR_SALA":
                criarSala(session);
                break;

            case "ENTRAR_SALA":
                entrarSala(session, dto.getSala());
                break;

            case "JOGADA":
                jogar(session, dto);
                break;

            case "REMOVER_DISCOS":
                removerDiscos(session, dto);
                break;

            case "ATUALIZAR_PONTOS":
                atualizarPontos(session, dto);
                break;

            case "MUDAR_VEZ":
                mudarVez(session, dto);
                break;


        }
    }

    public void criarSala(WebSocketSession session) throws IOException {
        String codigo = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Sala sala = new Sala();
        sala.setCodigo(codigo);
        sala.setAzul(session);

        EstadoPartida estado = new EstadoPartida();
        estado.setVez("AZUL");
        estado.setPontosAzul(0);
        estado.setPontosVermelho(0);
        estado.setJogando(false);
        estado.setDiscos(criarDiscos());

        sala.setEstado(estado);
        salas.put(codigo, sala);


        MensagemDTO resposta = MensagemDTO.builder()
                .tipo(StatusEnum.SALA_CRIADA.name())
                .sala(codigo)
                .mensagem("Sala criada: " + codigo)
                .build();

        System.out.println("Quantidade de discos: " + sala.getEstado().getDiscos().size());

        for (Disco d : sala.getEstado().getDiscos()) {
            System.out.println(
                    d.getId() + " " +
                            d.getTeam() + " -> (" +
                            d.getX() + ", " +
                            d.getY() + ")"
            );
        }

        session.sendMessage(new TextMessage(jsonUtils.toJson(resposta)));
    }

    public void entrarSala(WebSocketSession session, String codigo) throws IOException {
        Sala sala = salas.get(codigo);
        if (sala == null) {
            return;
        }
        sala.setVermelho(session);

        MensagemDTO azul = MensagemDTO.builder()
                .tipo(StatusEnum.PARTIDA_INICIADA.name())
                .sala(codigo)
                .estado(sala.getEstado())
                .jogador("AZUL")
                .build();

        MensagemDTO vermelho = MensagemDTO.builder()
                .tipo(StatusEnum.PARTIDA_INICIADA.name())
                .sala(codigo)
                .estado(sala.getEstado())
                .jogador("VERMELHO")
                .build();

        sala.getAzul().sendMessage(new TextMessage(jsonUtils.toJson(azul)));
        sala.getVermelho().sendMessage(new TextMessage(jsonUtils.toJson(vermelho)));

    }

    private void enviarParaSala(Sala sala, MensagemDTO dto) throws IOException {

        String json = jsonUtils.toJson(dto);

        if (sala.getAzul() != null && sala.getAzul().isOpen()) {
            sala.getAzul().sendMessage(new TextMessage(json));
        }

        if (sala.getVermelho() != null && sala.getVermelho().isOpen()) {
            sala.getVermelho().sendMessage(new TextMessage(json));
        }

    }

    private void jogar(WebSocketSession session, MensagemDTO dto) throws IOException {

        Sala sala = salas.get(dto.getSala());

        if (sala == null) {
            return;
        }

        String json = jsonUtils.toJson(dto);

        if (session.getId().equals(sala.getAzul().getId())) {

            if (sala.getVermelho() != null && sala.getVermelho().isOpen()) {
                sala.getVermelho().sendMessage(new TextMessage(json));
            }

        } else {

            if (sala.getAzul() != null && sala.getAzul().isOpen()) {
                sala.getAzul().sendMessage(new TextMessage(json));
            }

        }
    }

    private List<Disco> criarDiscos() {
        List<Disco> discos = new ArrayList<>();
        int id = 1;
        id = adicionarDiscos(id, discos, 5, "blue");
        adicionarDiscos(id, discos, 5, "red");
        return discos;

    }

    private int adicionarDiscos(int discoId, List<Disco> discos, int quantidade, String team) {
        for (int i = 0; i < quantidade; i++) {
            Disco disco = new Disco();

            disco.setId(discoId++);
            disco.setTeam(team);

            Ponto ponto = gerarPosicao(discos);

            disco.setX(ponto.x());
            disco.setY(ponto.y());

            disco.setVx(0.0);
            disco.setVy(0.0);
            disco.setAngle(0.0);
            disco.setRemover(false);

            discos.add(disco);
        }
        return discoId;
    }

    private Ponto gerarPosicao(List<Disco> discos) {
        while (true) {
            double angulo = Math.random() * Math.PI * 2;
            double distancia = Math.random() * (RAIO_MESA - 50);

            double x = CENTRO_X + Math.cos(angulo) * distancia;
            double y = CENTRO_Y + Math.sin(angulo) * distancia;

            boolean valido = true;

            for (Disco d : discos) {
                double dist = Math.hypot(x - d.getX(), y - d.getY());
                if (dist < RAIO_DISCO * 2 + 30) {
                    valido = false;
                    break;
                }
            }
            if (valido) {
                return new Ponto(x, y);
            }
        }
    }

    private void removerDiscos(WebSocketSession session, MensagemDTO dto) throws IOException {
        Sala sala = salas.get(dto.getSala());
        if (sala == null) {
            return;
        }
        enviarParaSala(sala, dto);
    }

    private void atualizarPontos(WebSocketSession session, MensagemDTO dto) throws IOException {
        Sala sala = salas.get(dto.getSala());
        if (sala == null) {
            return;
        }
        enviarParaSala(sala, dto);
    }

    private void mudarVez(WebSocketSession session, MensagemDTO dto) throws IOException {
        Sala sala = salas.get(dto.getSala());
        if (sala == null) {
            return;
        }
        enviarParaSala(sala, dto);
    }

}