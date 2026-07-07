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
            document.getElementById("statusSala").innerHTML = "<span style='color:#e53935'>A sala não existe.</span>";
            break;

        case "SALA_OCUPADA":
            document.getElementById("statusSala").innerHTML = "<span style='color:#e53935'>A sala já está ocupada.</span>";
            break;

        case "SALA_CRIADA":
            document.getElementById("codigoSala").value = resposta.sala;
            document.getElementById("statusSala").innerHTML = "<span style='color:#abffab'>SALA CRIADA: " + resposta.sala + ", AGUARDE.</span>";
            document.getElementById("codigoSala").style.display = "none";
            document.getElementById("btEntrarSala").style.display = "none";
            document.getElementById("listaSalas").style.display = "none";
            document.getElementById("btcriar").style.display = "none";
            document.getElementById("btvoltar").style.display = "block";
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

        case "LISTA_SALAS":
            atualizarListaSalas(resposta.salas);
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
            "<span style='color:#e53935'>Informe o código da sala.</span>";
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

function entrarSalaCodigo(codigo){
    document.getElementById("codigoSala").value = codigo;
    entrarSala();
}
