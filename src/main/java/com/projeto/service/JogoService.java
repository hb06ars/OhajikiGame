package com.projeto.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class JogoService {

    public void processarMensagem(WebSocketSession session, String mensagem) {

        System.out.println("--------------------------------");
        System.out.println("Sessão : " + session.getId());
        System.out.println("Mensagem: " + mensagem);
        System.out.println("--------------------------------");

        try {

            session.sendMessage(
                    new TextMessage("Servidor recebeu: " + mensagem)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}