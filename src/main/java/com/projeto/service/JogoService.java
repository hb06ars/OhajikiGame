package com.projeto.service;

import com.projeto.model.dto.AlertaDTO;
import com.projeto.model.dto.Disco;
import com.projeto.model.dto.EstadoPartida;
import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.dto.Ponto;
import com.projeto.model.dto.Sala;
import com.projeto.model.enums.StatusEnum;
import com.projeto.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
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
    private static final double CENTRO_Y = 800;
    private static final double RAIO_DISCO = 48;

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

        System.out.println("Sala criada: " + codigo);

        session.sendMessage(new TextMessage(jsonUtils.toJson(resposta)));
    }

    public void entrarSala(WebSocketSession session, String codigo) throws IOException {
        Sala sala = salas.get(codigo);
        if (sala == null || (sala.getVermelho() != null && !sala.getAzul().getId().equals(sala.getVermelho().getId()))) {
            if (sala == null) {
                session.sendMessage(new TextMessage(jsonUtils.toJson(AlertaDTO.builder()
                        .tipo(StatusEnum.SALA_INEXISTENTE)
                        .mensagem("A sala não existe!").build())));
            } else {
                session.sendMessage(new TextMessage(jsonUtils.toJson(AlertaDTO.builder()
                        .tipo(StatusEnum.SALA_OCUPADA)
                        .mensagem("A sala já está ocupada!").build())));
            }
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

        if (sala == null) return;

        String json = jsonUtils.toJson(dto);

        if (sala.getAzul() != null && sala.getAzul().isOpen()) {
            sala.getAzul().sendMessage(new TextMessage(json));
        }

        if (sala.getVermelho() != null && sala.getVermelho().isOpen()) {
            sala.getVermelho().sendMessage(new TextMessage(json));
        }
    }

    private void jogar(WebSocketSession session, MensagemDTO dto) {

        Sala sala = salas.get(dto.getSala());

        if (sala == null) return;

        EstadoPartida estado = sala.getEstado();

        if (estado.isJogando()) {
            return;
        }

        String jogador = session.getId().equals(sala.getAzul().getId())
                ? "AZUL"
                : "VERMELHO";

        if (!estado.getVez().equals(jogador)) {
            return;
        }

        Disco disco = estado.getDiscos()
                .stream()
                .filter(d -> d.getId() == dto.getDiscoId())
                .findFirst()
                .orElse(null);

        if (disco == null) return;

        String meuTime = jogador.equals("AZUL") ? "blue" : "red";

        if (!disco.getTeam().equals(meuTime)) {
            return;
        }

        estado.limparJogada();
        estado.setJogando(true);
        estado.setDiscoJogado(disco.getId());

        disco.setVx(dto.getVx());
        disco.setVy(dto.getVy());
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
            double margemLateral = 30;
            double margemSuperior = 100;
            double margemInferior = 30;

            double larguraMesa = CENTRO_X * 2;
            double alturaMesa = CENTRO_Y * 2;

            double x = margemLateral + Math.random() * (larguraMesa - margemLateral * 2);
            double y = margemSuperior + Math.random() * (alturaMesa - margemSuperior - margemInferior);

            boolean valido = true;

            for (Disco d : discos) {
                double dist = Math.hypot(x - d.getX(), y - d.getY());
                if (dist < RAIO_DISCO * 2 + 70) {
                    valido = false;
                    break;
                }
            }
            if (valido) {
                return new Ponto(x, y);
            }
        }
    }

    private void verificarVencedor(Sala sala) throws IOException {
        long azuis = sala.getEstado().getDiscos().stream()
                .filter(d -> "blue".equals(d.getTeam()))
                .count();

        long vermelhos = sala.getEstado().getDiscos().stream()
                .filter(d -> "red".equals(d.getTeam()))
                .count();

        if (azuis <= 1) {

            MensagemDTO dto = MensagemDTO.builder()
                    .tipo(StatusEnum.FIM_JOGO.name())
                    .vencedor("AZUL")
                    .build();

            enviarParaSala(sala, dto);
            return;
        }

        if (vermelhos <= 1) {

            MensagemDTO dto = MensagemDTO.builder()
                    .tipo(StatusEnum.FIM_JOGO.name())
                    .vencedor("VERMELHO")
                    .build();

            enviarParaSala(sala, dto);
        }
    }

    private void trocarVez(EstadoPartida estado) {
        if ("AZUL".equals(estado.getVez())) {
            estado.setVez("VERMELHO");
        } else {
            estado.setVez("AZUL");
        }
    }

    private void atualizarPontuacao(EstadoPartida estado) {

        long pontos = estado.getDiscos().stream()
                .filter(Disco::isRemover)
                .count();

        if (pontos == 0) {
            return;
        }

        if ("AZUL".equals(estado.getVez())) {
            estado.setPontosAzul(
                    estado.getPontosAzul() + (int) pontos
            );
        } else {
            estado.setPontosVermelho(
                    estado.getPontosVermelho() + (int) pontos
            );
        }

    }

    @Scheduled(fixedRate = 16) // ~60fps
    public void loop() throws IOException {
        for (Sala sala : salas.values()) {
            step(sala);
            enviarEstado(sala);
        }
    }

    private void step(Sala sala) throws IOException {

        EstadoPartida estado = sala.getEstado();
        List<Disco> discos = estado.getDiscos();

        double largura = 1000;
        double altura = 1600;
        double r = 48;

        // movimento
        movimentarDiscos(discos);

        // colisão
        resolverColisoes(estado);

        String timeDaVez = estado.getVez().equals("AZUL") ? "blue" : "red";

        boolean existeMovimento = discos.stream()
                .anyMatch(d -> Math.abs(d.getVx()) > 0.05 || Math.abs(d.getVy()) > 0.05);

        if (!existeMovimento && estado.getDiscoJogado() != null) {
            finalizarJogada(sala);
        }

        boolean removeuAzul = discos.stream()
                .anyMatch(d -> d.isRemover() && "blue".equals(d.getTeam()));

        boolean removeuVermelho = discos.stream()
                .anyMatch(d -> d.isRemover() && "red".equals(d.getTeam()));

        if (!existeMovimento && !(removeuAzul && removeuVermelho)) {
            discos.removeIf(Disco::isRemover);
        }
    }



    private void broadcastEstado(Sala sala) throws IOException {
        MensagemDTO dto = MensagemDTO.builder()
                .tipo("ESTADO")
                .estado(sala.getEstado())
                .build();

        enviarParaSala(sala, dto);
    }

    private void enviarEstado(Sala sala) throws IOException {

        if (sala == null || sala.getEstado() == null) return;

        MensagemDTO dto = MensagemDTO.builder()
                .tipo("ESTADO")
                .estado(sala.getEstado())
                .build();

        enviarParaSala(sala, dto);
    }

    public void stepAllSalas() throws IOException {
        for (Sala sala : salas.values()) {
            step(sala);
            enviarEstado(sala);
        }
    }

    private void movimentarDiscos(List<Disco> discos) {

        double largura = 1000;
        double altura = 1600;
        double r = 48;

        for (Disco d : discos) {

            d.setX(d.getX() + d.getVx());
            d.setY(d.getY() + d.getVy());

            d.setVx(d.getVx() * 0.98);
            d.setVy(d.getVy() * 0.98);

            if (d.getX() < r) {
                d.setX(r);
                d.setVx(-d.getVx() * 0.9);
            }

            if (d.getX() > largura - r) {
                d.setX(largura - r);
                d.setVx(-d.getVx() * 0.9);
            }

            if (d.getY() < r) {
                d.setY(r);
                d.setVy(-d.getVy() * 0.9);
            }

            if (d.getY() > altura - r) {
                d.setY(altura - r);
                d.setVy(-d.getVy() * 0.9);
            }
        }
    }

    private void resolverColisoes(EstadoPartida estado) {

        List<Disco> discos = estado.getDiscos();
        double r = 48;

        for (int i = 0; i < discos.size(); i++) {
            for (int j = i + 1; j < discos.size(); j++) {

                Disco a = discos.get(i);
                Disco b = discos.get(j);

                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();

                double dist = Math.sqrt(dx * dx + dy * dy);
                double min = r * 2;

                if (dist > 0 && dist < min) {

                    double nx = dx / dist;
                    double ny = dy / dist;

                    double overlap = min - dist;

                    a.setX(a.getX() - nx * overlap / 2);
                    a.setY(a.getY() - ny * overlap / 2);

                    b.setX(b.getX() + nx * overlap / 2);
                    b.setY(b.getY() + ny * overlap / 2);

                    double dvx = b.getVx() - a.getVx();
                    double dvy = b.getVy() - a.getVy();

                    double p = dvx * nx + dvy * ny;

                    if (p < 0) {
                        a.setVx(a.getVx() + p * nx);
                        a.setVy(a.getVy() + p * ny);

                        b.setVx(b.getVx() - p * nx);
                        b.setVy(b.getVy() - p * ny);
                    }

                    processarColisao(estado, a, b);
                }
            }
        }
    }

    private void processarColisao(EstadoPartida estado, Disco a, Disco b) {

        if (estado.getDiscoJogado() == null) {
            return;
        }

        Disco discoLancado = null;
        Disco outroDisco = null;

        boolean aEhOrigem =
                a.getId() == estado.getDiscoJogado()
                        || estado.getDiscosPontuados().contains(a.getId());

        boolean bEhOrigem =
                b.getId() == estado.getDiscoJogado()
                        || estado.getDiscosPontuados().contains(b.getId());

        if (aEhOrigem) {
            discoLancado = a;
            outroDisco = b;
        } else if (bEhOrigem) {
            discoLancado = b;
            outroDisco = a;
        } else {
            return;
        }

        estado.setAcertouAlgumaPeca(true);

        String timeDaVez = estado.getVez().equals("AZUL") ? "blue" : "red";

        if (!outroDisco.getTeam().equals(timeDaVez)) {
            estado.setBateuErrado(true);
            estado.getDiscosPontuados().clear();
            estado.getDiscos().forEach(d -> d.setRemover(false));
            return;
        }

        estado.getDiscosPontuados().add(outroDisco.getId());

        outroDisco.setRemover(true);
    }

    private void finalizarJogada(Sala sala) throws IOException {
        EstadoPartida estado = sala.getEstado();
        List<Disco> discos = estado.getDiscos();

        if (estado.isBateuErrado()) {
            discos.forEach(d -> d.setRemover(false));

        } else {

            if (estado.getVez().equals("AZUL")) {
                estado.setPontosAzul(
                        estado.getPontosAzul()
                                + estado.getDiscosPontuados().size());
            } else {

                estado.setPontosVermelho(
                        estado.getPontosVermelho()
                                + estado.getDiscosPontuados().size());
            }

            discos.removeIf(Disco::isRemover);
        }

        verificarVencedor(sala);

        if (estado.isBateuErrado() || !estado.isAcertouAlgumaPeca()) {

            estado.setVez(
                    estado.getVez().equals("AZUL")
                            ? "VERMELHO"
                            : "AZUL");
        }

        estado.limparJogada();
        estado.setJogando(false);
    }



}