package com.projeto.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemDTO {
    private String tipo;
    private String sala;
    private String mensagem;
    private Integer discoId;
    private EstadoPartida estado;

    private Integer vx;
    private Integer vy;

}