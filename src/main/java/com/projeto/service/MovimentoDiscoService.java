package com.projeto.service;

import com.projeto.model.dto.Disco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.projeto.utils.Constants.BORDA_BAIXO;
import static com.projeto.utils.Constants.BORDA_DIREITA;
import static com.projeto.utils.Constants.BORDA_ESQUERDA;
import static com.projeto.utils.Constants.BORDA_TOPO;
import static com.projeto.utils.Constants.RAIO_DISCO;

@Component
@RequiredArgsConstructor
public class MovimentoDiscoService {

    public void movimentarDiscos(List<Disco> discos) {

        var r = RAIO_DISCO;

        for (Disco d : discos) {

            d.setX(d.getX() + d.getVx());
            d.setY(d.getY() + d.getVy());

            d.setVx(d.getVx() * 0.98);
            d.setVy(d.getVy() * 0.98);

            if (d.getX() < BORDA_ESQUERDA + r) {
                d.setX(BORDA_ESQUERDA + r);
                d.setVx(-d.getVx() * 0.9);
            }

            if (d.getX() > BORDA_DIREITA - r) {
                d.setX(BORDA_DIREITA - r);
                d.setVx(-d.getVx() * 0.9);
            }

            if (d.getY() < BORDA_TOPO + r) {
                d.setY(BORDA_TOPO + r);
                d.setVy(-d.getVy() * 0.9);
            }

            if (d.getY() > BORDA_BAIXO - r) {
                d.setY(BORDA_BAIXO - r);
                d.setVy(-d.getVy() * 0.9);
            }
        }
    }

}
