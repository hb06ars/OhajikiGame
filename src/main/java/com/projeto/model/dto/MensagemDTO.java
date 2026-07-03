package com.projeto.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensagemDTO {

    private String tipo;
    private String sala;
    private String jogador;
    private String mensagem;
    private Object dados;

}