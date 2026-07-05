package com.projeto.service;

import com.projeto.model.dto.Disco;
import com.projeto.model.dto.EstadoPartida;
import com.projeto.model.dto.MensagemDTO;
import com.projeto.model.dto.Sala;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StepService {

    private final SalaService salaService;
    private final MovimentoDiscoService movimentarDiscos;
    private final ColisoesService colisoesService;
    private final JogadaService jogadaService;

    public void executar(Sala sala) throws IOException {
        EstadoPartida estado = sala.getEstado();
        List<Disco> discos = estado.getDiscos();

        // movimento
        movimentarDiscos.movimentarDiscos(discos);

        // colisão
        colisoesService.resolverColisoes(estado);

        boolean existeMovimento = validarSeExisteMovimento(discos);

        if (!existeMovimento && estado.getDiscoJogado() != null) {
            jogadaService.finalizarJogada(sala);
        }

        boolean removeuAzul = validarSeRemoveuDisco(discos, "blue");
        boolean removeuVermelho = validarSeRemoveuDisco(discos, "red");

        if (!existeMovimento && !(removeuAzul && removeuVermelho)) {
            discos.removeIf(Disco::isRemover);
        }
    }

    private static boolean validarSeRemoveuDisco(List<Disco> discos, String blue) {
        return discos.stream()
                .anyMatch(d -> d.isRemover() && blue.equals(d.getTeam()));
    }

    private static boolean validarSeExisteMovimento(List<Disco> discos) {
        return discos.stream()
                .anyMatch(d -> Math.abs(d.getVx()) > 0.05 || Math.abs(d.getVy()) > 0.05);
    }


    public void enviarEstado(Sala sala) throws IOException {

        if (sala == null || sala.getEstado() == null) return;

        var mensagemDTO = MensagemDTO.builder()
                .tipo("ESTADO")
                .estado(sala.getEstado())
                .build();

        salaService.enviarParaSala(sala, mensagemDTO);
    }


}
