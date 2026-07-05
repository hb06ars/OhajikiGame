package com.projeto.service;

import com.projeto.model.dto.Disco;
import com.projeto.model.dto.EstadoPartida;
import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.dto.Sala;
import com.projeto.model.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

import static com.projeto.utils.Constants.AZUL;
import static com.projeto.utils.Constants.VELOCIDADE_MAXIMA;
import static com.projeto.utils.Constants.VERMELHO;

@Service
@RequiredArgsConstructor
public class JogadaService {

    private final SalaService salaService;

    public void jogar(WebSocketSession session, MensagemDTO dto, Map<String, Sala> salas) {
        var sala = salas.get(dto.getSala());

        if (sala == null) return;

        var estadoPartida = sala.getEstado();

        if (estadoPartida.isJogando()) {
            return;
        }

        var jogador = session.getId().equals(sala.getAzul().getId()) ? AZUL : VERMELHO;

        if (!estadoPartida.getVez().equals(jogador)) {
            return;
        }

        var disco = estadoPartida.getDiscos()
                .stream()
                .filter(d -> d.getId() == dto.getDiscoId())
                .findFirst()
                .orElse(null);

        if (disco == null) return;

        var meuTime = buscarMeuTime(jogador);

        if (!disco.getTeam().equals(meuTime)) {
            return;
        }

        var velocidade = Math.hypot(dto.getVx(), dto.getVy());

        if (velocidade > VELOCIDADE_MAXIMA) {
            return;
        }

        if (disco.isRemover()) {
            return;
        }

        estadoPartida.limparJogada();
        estadoPartida.setJogando(true);
        estadoPartida.setDiscoJogado(disco.getId());

        disco.setVx(dto.getVx());
        disco.setVy(dto.getVy());
    }

    private static String buscarMeuTime(String jogador) {
        return jogador.equals(AZUL) ? "blue" : "red";
    }

    public void finalizarJogada(Sala sala) throws IOException {
        var estado = sala.getEstado();
        var discos = estado.getDiscos();

        if (estado.isBateuErrado()) {
            discos.forEach(d -> d.setRemover(false));

        } else {

            if (estado.getVez().equals(AZUL)) {
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
            mudarVez(estado);
        }

        estado.limparJogada();
        estado.setJogando(false);
    }

    private static void mudarVez(EstadoPartida estado) {
        estado.setVez(estado.getVez().equals(AZUL) ? VERMELHO : AZUL);
    }

    private void verificarVencedor(Sala sala) throws IOException {
        var azuis = filtrarPecas(sala, "blue");
        var vermelhos = filtrarPecas(sala, "red");

        if (azuis <= 1) {
            var dto = montarMensagemFimDoJogo(AZUL);
            salaService.enviarParaSala(sala, dto);
            return;
        }

        if (vermelhos <= 1) {
            var dto = montarMensagemFimDoJogo(VERMELHO);
            salaService.enviarParaSala(sala, dto);
        }
    }

    private static long filtrarPecas(Sala sala, String corDaPeca) {
        return sala.getEstado().getDiscos().stream()
                .filter(d -> corDaPeca.equals(d.getTeam()))
                .count();
    }

    private static MensagemDTO montarMensagemFimDoJogo(String azul) {
        return MensagemDTO.builder()
                .tipo(StatusEnum.FIM_JOGO.name())
                .vencedor(azul)
                .build();
    }
}