package com.projeto.service;

import com.projeto.model.dto.Disco;
import com.projeto.model.dto.EstadoPartida;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.projeto.utils.Constants.AZUL;
import static com.projeto.utils.Constants.RAIO_DISCO;

@Component
@RequiredArgsConstructor
public class ColisoesService {

    public void resolverColisoes(EstadoPartida estado) {

        var discos = estado.getDiscos();
        double r = RAIO_DISCO;

        for (int i = 0; i < discos.size(); i++) {
            for (int j = i + 1; j < discos.size(); j++) {

                var a = discos.get(i);
                var b = discos.get(j);

                var dx = b.getX() - a.getX();
                var dy = b.getY() - a.getY();

                var dist = Math.sqrt(dx * dx + dy * dy);
                var min = r * 2;

                if (dist > 0 && dist < min) {

                    var nx = dx / dist;
                    var ny = dy / dist;

                    var overlap = min - dist;

                    a.setX(a.getX() - nx * overlap / 2);
                    a.setY(a.getY() - ny * overlap / 2);

                    b.setX(b.getX() + nx * overlap / 2);
                    b.setY(b.getY() + ny * overlap / 2);

                    var dvx = b.getVx() - a.getVx();
                    var dvy = b.getVy() - a.getVy();

                    var p = dvx * nx + dvy * ny;

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

        var discoAehOrigem = discoEhOrigem(estado, a);
        var discoBehOrigem = discoEhOrigem(estado, b);

        if (discoAehOrigem) {
            discoLancado = a;
            outroDisco = b;
        } else if (discoBehOrigem) {
            discoLancado = b;
            outroDisco = a;
        } else {
            return;
        }

        estado.setAcertouAlgumaPeca(true);

        var timeDaVez = buscarTimeDaVez(estado);

        if (!outroDisco.getTeam().equals(timeDaVez)) {
            estado.setBateuErrado(true);
            estado.getDiscosPontuados().clear();
            estado.getDiscos().forEach(d -> d.setRemover(false));
            return;
        }

        estado.getDiscosPontuados().add(outroDisco.getId());

        outroDisco.setRemover(true);
    }

    private static String buscarTimeDaVez(EstadoPartida estado) {
        return estado.getVez().equals(AZUL) ? "blue" : "red";
    }

    private static boolean discoEhOrigem(EstadoPartida estado, Disco a) {
        return a.getId() == estado.getDiscoJogado()
                || estado.getDiscosPontuados().contains(a.getId());
    }

}
