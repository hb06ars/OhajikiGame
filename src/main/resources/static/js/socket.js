const protocolo = location.protocol === "https:" ? "wss" : "ws";

const socket = new WebSocket(
    `${protocolo}://${location.host}/ws/jogo`
);

socket.onmessage = (e) => {
    const resposta = JSON.parse(e.data);

    switch (resposta.tipo) {
        case "CONECTADO":
            break;

        case "SALA_INEXISTENTE":
            document.getElementById("statusSala").innerHTML = "A sala não existe";
            break;

        case "SALA_OCUPADA":
            document.getElementById("statusSala").innerHTML = "A sala já está ocupada.";
            break;

        case "SALA_CRIADA":
            document.getElementById("codigoSala").value = resposta.sala;
            document.getElementById("statusSala").innerHTML = "Sala criada: " + resposta.sala + ", aguarde.";
            document.getElementById("codigoSala").style.display = "none";
            document.getElementById("btEntrarSala").style.display = "none";
            break;

        case "PARTIDA_INICIADA":
            Partida.setSala(resposta.sala);
            Partida.setEstado(resposta.estado);
            Partida.setJogador(resposta.jogador);
            vez = resposta.estado.vez;
            mostrarJogo();
            window.iniciarJogoOnline();
            break;

        case "FIM_JOGO":
            fimJogo(resposta);
            break;

        case "ESTADO":
            atualizarEstado(resposta.estado);
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
