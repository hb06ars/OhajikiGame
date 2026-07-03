const socket = new WebSocket("ws://localhost:8080/ws/jogo");

socket.onopen = () => {
    console.log("Conectado");
    socket.send("Olá servidor!");

};

socket.onmessage = (e) => {
    console.log(e.data);
};

function criarSala(){
    var codigoSala = document.getElementById("codigoSala").value;
    if(codigoSala != null && codigoSala.length > 0){
        document.getElementById("statusSala").innerHTML = "Sala criada: " + codigoSala;
    } else {
            document.getElementById("statusSala").innerHTML = "O código da sala deve ser preenchida para a criação!";
    }
}

function entrarSala() {
    console.log("Entrar sala");
    document.getElementById("telaLobby").style.display = "none";
    document.getElementById("telaJogo").style.display = "block";
}

function mostrarLobby() {
    document.getElementById("telaLobby").style.display = "flex";
    document.getElementById("telaJogo").style.display = "none";
}

function mostrarJogo() {
    document.getElementById("telaLobby").style.display = "none";
    document.getElementById("telaJogo").style.display = "block";
}