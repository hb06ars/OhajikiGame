package com.projeto.service;

import com.projeto.model.dto.AlertaDTO;
import com.projeto.model.dto.Disco;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.projeto.utils.Constants.AZUL;
import static com.projeto.utils.Constants.VERMELHO;

@Component
@RequiredArgsConstructor
public class MovimentoDiscoService {

    public void movimentarDiscos(List<Disco> discos) {

        var largura = 1000;
        var altura = 1600;
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
