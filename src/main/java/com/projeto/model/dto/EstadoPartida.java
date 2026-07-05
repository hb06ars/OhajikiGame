package com.projeto.model.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class EstadoPartida {
    private String vez;
    private Integer pontosAzul;
    private Integer pontosVermelho;
    private List<Disco> discos;
    private Integer discoJogado;
    private boolean bateuErrado;
    private boolean jogando;
    private boolean acertouAlgumaPeca;
    private Set<Integer> discosPontuados = new HashSet<>();

    public void limparJogada() {
        bateuErrado = false;
        acertouAlgumaPeca = false;
        discosPontuados.clear();
        discoJogado = null;
    }

}