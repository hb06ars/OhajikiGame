package com.projeto.service;

import com.projeto.model.dto.Disco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MovimentoDiscoService {

    public void movimentarDiscos(List<Disco> discos) {

        var largura = 1000;
        var altura = 1750;
        var r = 48;

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

}
