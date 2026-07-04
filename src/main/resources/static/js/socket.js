const protocolo = location.protocol === "https:" ? "wss" : "ws";

const socket = new WebSocket(
    `${protocolo}://${location.host}/ws/jogo`
);


socket.onmessage = (e) => {
    const resposta = JSON.parse(e.data);

    switch (resposta.tipo) {

        case "CONECTADO":
            break;

        case "SALA_CRIADA":
            document.getElementById("codigoSala").value = resposta.sala;
            document.getElementById("statusSala").innerHTML =
                "Sala criada: " + resposta.sala;
            break;

        case "PARTIDA_INICIADA":
            Partida.setEstado(resposta.estado);
            Partida.setSala(resposta.sala);
            mostrarJogo();
            window.iniciarJogoOnline();
            break;

        case "ERRO":
            document.getElementById("statusSala").innerHTML =
                resposta.mensagem;
            break;

        case "JOGADA":
            console.log("JOGADA");
            window.aplicarJogada(resposta);
            break;

        case "REMOVER_DISCOS":
            removerDiscos(resposta);
            break;

        case "ATUALIZAR_PONTOS":
            atualizarPontos(resposta);
            break;

        case "MUDAR_VEZ":
            atualizarVez(resposta);
            break;
    }

};

function criarSala() {

    socket.send(JSON.stringify({
        tipo: "CRIAR_SALA"
    }));

}

function entrarSala() {
    const codigo = document.getElementById("codigoSala").value.trim();
    if (codigo.length == 0) {
        document.getElementById("statusSala").innerHTML =
            "Informe o código da sala.";
        return;
    }

    socket.send(JSON.stringify({
        tipo: "ENTRAR_SALA",
        sala: codigo
    }));

}

function mostrarLobby() {
    document.getElementById("telaLobby").style.display = "flex";
    document.getElementById("telaJogo").style.display = "none";
}

function mostrarJogo() {
    document.getElementById("telaLobby").style.display = "none";
    document.getElementById("telaJogo").style.display = "block";
}
