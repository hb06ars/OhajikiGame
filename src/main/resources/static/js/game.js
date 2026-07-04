var vez = "AZUL";
var pontosTemporarioAzul = 0;
var pontosTemporarioVermelho = 0;
var pontosAzul = 0;
var pontosVermelho = 0;
var proximoId = 1;
var idParaRemover = null;
var bateuErrado = false;
var discosPontuados = new Set();
var acertouAlgumaPeca = false;
let jogoIniciado = false;
let estado = "AGUARDANDO";
let discs = [];
let sel = null;
let sx = 0;
let sy = 0;
let lastX = 0;
let lastY = 0;
let movendo = false;
let estadoServidor = null;

const imgBlue = new Image();
const imgRed = new Image();

imgBlue.src = "img/disco_azul.png";
imgRed.src = "img/disco_vermelho.png";




// START GAME ------------------------------------------------
// Essa função faz toda a inicialização.
function startGame() {
    console.log("START GAME EXECUTADO");
    if (jogoIniciado) {
        return;
    }
    jogoIniciado = true;

    estado = Partida.getEstado();
    console.log("ESTADO RECEBIDO:", estado);
    console.log("DISCOS:", estado?.discos);

    if (!estado) {
        alert("Estado da partida não recebido.");
        return;
    }

    preenchendoVez();

    // Div do canvas no html com id = c, com um Contexto 2D.
    const c = document.getElementById("c");

    const x = c.getContext("2d");

    // Resize sempre que a janela muda de tamanho. Isso faz o canvas ocupar a tela inteira.
    function rs() {
        c.width = innerWidth;
        c.height = innerHeight;
    }



    onresize = rs;
    rs();

    estado = Partida.getEstado();
    discs = [];

    if (!estado || !estado.discos) {
        console.error("Estado inválido:", estado);
        return;
    }

    estado.discos.forEach(d => {
        discs.push({
            id: d.id,
            team: d.team,
            x: d.x,
            y: d.y,
            vx: d.vx,
            vy: d.vy,
            angle: d.angle,
            r: 48,
            remover: d.remover,
            img: d.team === "blue" ? imgBlue : imgRed,
            ax: null,
            ay: null
        });
    });

    // Responsável por criar os discos de forma aleatória na mesa, sem encostar um no outro.
    function add(n, team) {

        const img = team === "blue" ? imgBlue : imgRed;

        for (let i = 0; i < n; i++) {

            let x, y;
            let valido = false;

            while (!valido) {

                // posição aleatória dentro da mesa
                const MARGEM = 40;

				x = MARGEM + 20 + Math.random() * (c.width - (MARGEM + 20) * 2);
				y = MARGEM + 20 + Math.random() * (c.height - (MARGEM + 20) * 2);

                valido = true;

                // verifica colisão com todos os discos já criados
                for (const d of discs) {
                    const dist = Math.hypot(x - d.x, y - d.y);
                    // +5 deixa um pequeno espaço entre eles
                    if (dist < d.r + 14 + 30) {
                        valido = false;
                        break;
                    }
                }
            }

            discs.push({
                id: proximoId++,
                x,
                y,
                r: 48, // Tamanho do disco
                vx: 0,
                vy: 0,
                img,
                angle: 0,
                team,
                ax: null,
                ay: null
            });
        }
    }

    // Converte Mouse ou Touch do mobile para um formato único.
    function pos(e) {
        const t = e.touches ? e.touches[0] : e;
        return {
            x: t.clientX,
            y: t.clientY
        };
    }

        // Executa quando o jogador toca em algum lugar.
    function down(p) {

        if (movendo) {
            return;
        }

        if (Partida.getJogador() !== vez) {
            return;
        }

        sel = null;

        for (let d of discs) {

            // Só permite selecionar peças da vez
            if (vez === "AZUL" && d.team !== "blue")
                continue;

            if (vez === "VERMELHO" && d.team !== "red")
                continue;

            if (Math.hypot(p.x - d.x, p.y - d.y) < d.r) {

                sel = d;

                sx = p.x;
                sy = p.y;

                break;
            }
        }
    }

    // Executa enquanto o jogador arrasta.
    function move(p) {
        if (!sel) return;
        sel.ax = p.x;
        sel.ay = p.y;
    }

    // É chamada quando o jogador solta o dedo.
    function up(p) {

        if (movendo) {
            return;
        }

        bateuErrado = false;

        if (!sel) return;
        movendo = true;
        sel.vx = (sx - p.x) * 0.08;
        sel.vy = (sy - p.y) * 0.08;

        socket.send(JSON.stringify({
            tipo: "JOGADA",
            sala: Partida.sala,
            discoId: sel.id,
            vx: sel.vx,
            vy: sel.vy
        }));

        sel.ax = null;
        sel.ay = null;
        sel = null;
    }

    c.onmousedown = e => down(pos(e));
    c.onmousemove = e => move(pos(e));
    onmouseup = e => up(pos(e));

    c.addEventListener("touchstart", function(e){
        e.preventDefault();
        down({
            x: e.touches[0].clientX,
            y: e.touches[0].clientY
        });
    }, { passive: false });


    c.ontouchmove = e => {
        e.preventDefault();
        lastX = e.touches[0].clientX;
        lastY = e.touches[0].clientY;
        move({
            x: lastX,
            y: lastY
        });
    };


    c.ontouchend = e => {
        e.preventDefault();
        up({
            x: lastX,
            y: lastY
        });
    };

    let colisoes = new Set();

}
// FIM START GAME ------------------------------------------------


// FORA DO START GAME ------------------------------------------------

    function preenchendoVez(){
        const info = document.getElementById("infoJogador");

        if (Partida.getJogador() === vez) {
            info.innerHTML =
                "🎯 Você é " +
                (Partida.getJogador() === "AZUL" ? "🔵" : "🔴") +
                " SUA VEZ!";
        } else {
            info.innerHTML =
                "⏳ Você é " +
                (Partida.getJogador() === "AZUL" ? "🔵" : "🔴") +
                " AGUARDE!";
        }

    }

    window.aplicarJogada = function () {
        // não faz nada (ou remover completamente depois)
    };

    window.atualizarEstado = function (estado) {
        estadoServidor = estado;
    };


    // INICIAR JOGO ONLINE
    window.iniciarJogoOnline = function () {

        if (imgBlue.complete && imgRed.complete) {
            startGame();
            render();
        } else {
            Promise.all([
                new Promise(r => imgBlue.onload = r),
                new Promise(r => imgRed.onload = r)
            ]).then(() => {
                startGame();
                render();
            });
        }
    }

    window.atualizarPontos = function (mensagem) {
        pontosAzul = mensagem.pontosAzul;
        pontosVermelho = mensagem.pontosVermelho;
        document.getElementById("pontosAzul").innerHTML = pontosAzul;
        document.getElementById("pontosVermelho").innerHTML = pontosVermelho;
    }

    window.atualizarVez = function (mensagem) {
        vez = mensagem.vez;
        preenchendoVez();
    }

    window.fimJogo = function (mensagem) {
        const meuTime = Partida.getJogador();

        if (mensagem.vencedor === meuTime) {
            alert("🎉 Parabéns! Você venceu!");
        } else {
            alert("😢 Você perdeu!");
        }
        location.reload();
    }

    function render() {

        const c = document.getElementById("c");
        const x = c.getContext("2d");

        x.clearRect(0, 0, c.width, c.height);

        if (estadoServidor?.discos) {

            for (let serverDisc of estadoServidor.discos) {

                let clientDisc = discs.find(d => d.id === serverDisc.id);

                if (!clientDisc) continue;

                clientDisc.x += (serverDisc.x - clientDisc.x) * 0.2;
                clientDisc.y += (serverDisc.y - clientDisc.y) * 0.2;

                clientDisc.vx = serverDisc.vx;
                clientDisc.vy = serverDisc.vy;
            }

            discs = discs.filter(d =>
                estadoServidor.discos.some(s => s.id === d.id)
            );
        }

        for (let d of discs) {

            if (!d.img) {
                d.img = d.team === "blue" ? imgBlue : imgRed;
                d.r = 48;
            }

            x.save();
            x.translate(d.x, d.y);
            x.drawImage(d.img, -d.r, -d.r, d.r * 2, d.r * 2);
            x.restore();
        }

        if (sel && sel.ax != null) {
            x.beginPath();
            x.strokeStyle = "white";
            x.moveTo(sel.x, sel.y);
            x.lineTo(sel.ax, sel.ay);
            x.stroke();
        }

        requestAnimationFrame(render);
    }


// FIM FORA DO START GAME ------------------------------------------------
