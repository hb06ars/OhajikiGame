package com.projeto.model.dto;

import com.projeto.model.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDTO {
    private StatusEnum tipo;
    private String mensagem;
}