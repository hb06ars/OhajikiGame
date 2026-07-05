package com.projeto.service;

import com.projeto.model.dto.Disco;
import com.projeto.model.dto.Ponto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.projeto.utils.Constants.CENTRO_X;
import static com.projeto.utils.Constants.CENTRO_Y;
import static com.projeto.utils.Constants.DISCOS_ADICIONAR;
import static com.projeto.utils.Constants.RAIO_DISCO;

@Service
@RequiredArgsConstructor
public class DiscoService {

    public List<Disco> criarDiscos() {
        List<Disco> discos = new ArrayList<>();
        int id = 1;
        id = adicionarDiscos(id, discos, DISCOS_ADICIONAR, "blue");
        adicionarDiscos(id, discos, DISCOS_ADICIONAR, "red");
        return discos;

    }

    private int adicionarDiscos(int discoId, List<Disco> discos, int quantidade, String team) {
        for (int i = 0; i < quantidade; i++) {
            var disco = new Disco();

            disco.setId(discoId++);
            disco.setTeam(team);

            var ponto = gerarPosicao(discos);

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
            var margemLateral = 30;
            var margemSuperior = 100;
            var margemInferior = 30;

            var larguraMesa = CENTRO_X * 2;
            var alturaMesa = CENTRO_Y * 2;

            var x = margemLateral + Math.random() * (larguraMesa - margemLateral * 2);
            var y = margemSuperior + Math.random() * (alturaMesa - margemSuperior - margemInferior);

            var valido = true;

            for (Disco d : discos) {
                var dist = Math.hypot(x - d.getX(), y - d.getY());
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

}