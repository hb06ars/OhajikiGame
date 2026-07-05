package com.projeto.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemDTO {
    private String tipo;

    private String sala;

    private EstadoPartida estado;

    private Integer discoId;

    private Double vx;

    private Double vy;

    private String jogador;

    private String vencedor;

    private String mensagem;

}