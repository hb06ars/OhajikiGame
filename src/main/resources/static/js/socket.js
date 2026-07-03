const socket = new WebSocket("ws://localhost:8080/ws/jogo");


socket.onmessage = (e) => {

    const resposta = JSON.parse(e.data);
    console.log(resposta);

    switch (resposta.tipo) {

        case "CONECTADO":
            console.log(resposta.mensagem);
            break;

        case "SALA_CRIADA":
            document.getElementById("codigoSala").value = resposta.sala;
            document.getElementById("statusSala").innerHTML =
                "Sala criada: " + resposta.sala;
            break;

        case "PARTIDA_INICIADA":
            mostrarJogo();
            break;

        case "ERRO":
            document.getElementById("statusSala").innerHTML =
                resposta.mensagem;
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
