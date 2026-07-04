package com.projeto.model.dto;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class Sala {
    private String codigo;
    private WebSocketSession azul;
    private WebSocketSession vermelho;
    private EstadoPartida estado;
}