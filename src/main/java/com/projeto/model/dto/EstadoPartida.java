package com.projeto.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EstadoPartida {
    private Integer discoJogado;
    private String vez;
    private List<Disco> discos = new ArrayList<>();
    private int pontosAzul;
    private int pontosVermelho;
    private boolean jogando;
    private String vencedor;

}