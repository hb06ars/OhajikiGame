var vez = Math.random() < 0.5 ? "AZUL" : "VERMELHO";
var todasParadas = true;
var alteracaoAposPecasFicaremParadas = true;
var movendo = false;
var pontosTemporarioAzul = 0;
var pontosTemporarioVermelho = 0;
var pontosAzul = 0;
var pontosVermelho = 0;
var proximoId = 1;
var idParaRemover = null;
var bateuErrado = false;
var discosPontuados = new Set();
var acertouAlgumaPeca = false;
var discoLancado = null;
let discoLancadoId = null;
let jogoIniciado = false;
let estado = "AGUARDANDO";
let discs = [];
let sel = null;
let sx = 0;
let sy = 0;


const imgBlue = new Image();
const imgRed = new Image();

imgBlue.src = "img/disco_azul.png";
imgRed.src = "img/disco_vermelho.png";




// START GAME ------------------------------------------------
// Essa função faz toda a inicialização.
function startGame() {
    if (jogoIniciado) {
        return;
    }
    jogoIniciado = true;

    estado = Partida.getEstado();
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
    estado.discos.forEach(d => {
        discs.push({
            id: d.id,
            team: d.team,
            x: d.x,
            y: d.y,
            vx: d.vx,
            vy: d.vy,
            angle: d.angle,
            r: 20,
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

    let sel = null;
    let sx = 0;
    let sy = 0;
    let lastX = 0;
    let lastY = 0;

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
                discoLancado = d;

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
        estado = "MOVENDO";

        if (movendo) {
            return;
        }

        bateuErrado = false;
        discosPontuados.clear();

        if (!sel) return;
        movendo = true;
        alteracaoAposPecasFicaremParadas = false;
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

    // Essa é a função mais importante do jogo.
    // Atualiza a física.
    // Aplica atrito.
    function step() {
		const BORDA = 20;
		const MARGEM = 40;

		const CAMPO = {
            left: 45,
            right: 45,
            top: 65,
            bottom: 0
        };

		const esquerda = CAMPO.left;
		const direita = c.width - CAMPO.right;
		const topo = CAMPO.top;
		const baixo = c.height - CAMPO.bottom;

        // Física
        for (let d of discs) {

			if (d.x < esquerda + d.r) {
				d.x = esquerda + d.r;
				d.vx *= -0.9;
			}

			if (d.x > direita - d.r) {
				d.x = direita - d.r;
				d.vx *= -0.9;
			}

			if (d.y < topo + d.r) {
				d.y = topo + d.r;
				d.vy *= -0.9;
			}

			if (d.y > baixo - d.r) {
				d.y = baixo - d.r;
				d.vy *= -0.9;
			}

			let velocidade = Math.hypot(d.vx, d.vy);

			if (velocidade > 0.02) {
				let sentido = d.vx >= 0 ? 1 : -1;
				d.angle += sentido * velocidade * 0.03;
			}

			d.x += d.vx;
			d.y += d.vy;

			d.vx *= 0.98;
			d.vy *= 0.98;

			// ESQUERDA
			if (d.x < d.r) {
				d.x = d.r;
				d.vx *= -0.9;
			}

			// DIREITA
			if (d.x > c.width - d.r) {
				d.x = c.width - d.r;
				d.vx *= -0.9;
			}

			// TOPO
			if (d.y < d.r) {
				d.y = d.r;
				d.vy *= -0.9;
			}

			// BAIXO
			if (d.y > c.height - d.r) {
				d.y = c.height - d.r;
				d.vy *= -0.9;
			}
		}

        // Colisão entre discos
        // Ela é responsável pela colisão entre dois discos, até comparar todos.
        for (let i = 0; i < discs.length; i++) {
            for (let j = i + 1; j < discs.length; j++) {

                let a = discs[i];
                let b = discs[j];

                let dx = b.x - a.x;
                let dy = b.y - a.y;

                let dist = Math.hypot(dx, dy);
                let min = a.r + b.r;

                // Verificando colisão.
                if (dist < min && dist > 0) {
                    // Analisa se tem colisão repetida.
                    const chave =
                        a.id < b.id
                            ? `${a.id}-${b.id}`
                            : `${b.id}-${a.id}`;

                    if (!colisoes.has(chave)) {
                        colisoes.add(chave);
                        analisarColisao(a, b, discoLancado);
                    } else {

                         const chave =
                             a.id < b.id
                                 ? `${a.id}-${b.id}`
                                 : `${b.id}-${a.id}`;

                         colisoes.delete(chave);

                     }

                    // Analisa a colisão que teve, azul bateu em azul, ou azul bateu em vermelho, etc.
                    analisarColisao(a, b, discoLancado);

                    let nx = dx / dist;
                    let ny = dy / dist;

                    let overlap = min - dist;

                    a.x -= nx * overlap / 2;
                    b.x += nx * overlap / 2;

                    a.y -= ny * overlap / 2;
                    b.y += ny * overlap / 2;

                    let dvx = b.vx - a.vx;
                    let dvy = b.vy - a.vy;

                    let p = dvx * nx + dvy * ny;

                    if (p < 0) {
                        a.vx += p * nx;
                        a.vy += p * ny;
                        b.vx -= p * nx;
                        b.vy -= p * ny;
                    }
                }
            }
        }

        // Desenha
        x.clearRect(0, 0, c.width, c.height);

        for (let d of discs) {
            x.save();
            x.translate(d.x, d.y);
            x.rotate(d.angle);
            x.drawImage(
                d.img,
                -d.r,
                -d.r,
                d.r * 2,
                d.r * 2
            );

            x.restore();

            if (d.ax !== null) {
				x.save();
				x.beginPath();
				x.strokeStyle = "#FFF";
				x.lineWidth = 2;
				x.moveTo(d.x, d.y);
				x.lineTo(d.ax, d.ay);
				x.stroke();
				x.restore();
			}
        }

        requestAnimationFrame(step);

        // VALIDA SE TODAS AS PEÇAS ESTÃO PARADAS.
        const todasParadas = discs.every(
            d => Math.hypot(d.vx, d.vy) < 0.05
        );

        if (todasParadas && !alteracaoAposPecasFicaremParadas) {
            estado = "AGUARDANDO";
            alteracaoAposPecasFicaremParadas = true;
            movendo = false;

            if (!bateuErrado) {
                const pontos = discosPontuados.size;
                if (vez === "AZUL") {
                    pontosAzul += pontos;
                    document.getElementById("pontosAzul").innerHTML = pontosAzul;
                } else {
                    pontosVermelho += pontos;
                    document.getElementById("pontosVermelho").innerHTML = pontosVermelho;
                }
            }

            socket.send(JSON.stringify({
                tipo: "ATUALIZAR_PONTOS",
                sala: Partida.getSala(),
                pontosAzul: pontosAzul,
                pontosVermelho: pontosVermelho
            }));

            if (!bateuErrado) {
                const removidos = [...discosPontuados];
                discs = discs.filter(d => !discosPontuados.has(d.id));

                socket.send(JSON.stringify({
                    tipo: "REMOVER_DISCOS",
                    sala: Partida.getSala(),
                    discosRemovidos: removidos
                }));

                verificarVencedor();
                discoLancado = null;
                mudarVez();
            }

            mudarVez();
            if(bateuErrado){
                // console.log('VOCÊ BATEU ERRADO.')
            } else{
                pontosVermelho = pontosVermelho + pontosTemporarioVermelho;
                pontosAzul = pontosAzul + pontosTemporarioAzul;
            }
            if(vez == 'AZUL'){
                document.getElementById("pontosVermelho").innerHTML = pontosVermelho;
            } else {
                document.getElementById("pontosAzul").innerHTML = pontosAzul;
            }
            if(!bateuErrado && !acertouAlgumaPeca){
                mudarVez();
            }
            pontosTemporarioAzul = 0;
            pontosTemporarioVermelho = 0;
            bateuErrado = false;
            acertouAlgumaPeca = false;
        }
    }

    let colisoes = new Set();
    step();

}
// FIM START GAME ------------------------------------------------


// FORA DO START GAME ------------------------------------------------

    function analisarColisao(a, b, discoLancado){
        acertouAlgumaPeca = true;

        if (a.remover || b.remover) {
            return;
        }

        if (vez === "AZUL") {
            if (a.team === "red" || b.team === "red") {
                bateuErrado = true;
                return;
            }
        }

        if (vez === "VERMELHO") {
            if (a.team === "blue" || b.team === "blue") {
                bateuErrado = true;
                return;
            }
        }

        if (vez === "AZUL") {
            if (a.team === "blue")
                if (a.id !== discoLancado.id) {
                    discosPontuados.add(a.id);
                }
            if (b.team === "blue")
                if (b.id !== discoLancado.id) {
                    discosPontuados.add(b.id);
                }
        }

        if (vez === "VERMELHO") {
            if (a.team === "red")
                if (a.id !== discoLancado.id) {
                    discosPontuados.add(a.id);
                }
            if (b.team === "red")
                if (b.id !== discoLancado.id) {
                    discosPontuados.add(b.id);
                }
        }
    }

    function preenchendoVez(){
        if(vez == 'AZUL')
            document.getElementById("vezJogador").innerHTML = 'Vez: 🔵 Azul';
        else
            document.getElementById("vezJogador").innerHTML = 'Vez: 🔴 Vermelho';
    }

    function mudarVez() {
        if (vez == 'AZUL') {
            vez = 'VERMELHO';
        } else {
            vez = 'AZUL';
        }

        preenchendoVez();

        socket.send(JSON.stringify({
            tipo: "MUDAR_VEZ",
            sala: Partida.getSala(),
            vez: vez
        }));

    }

    function verificarVencedor() {
        const azuis = discs.filter(d => d.team === "blue").length;
        const vermelhos = discs.filter(d => d.team === "red").length;
        if (azuis === 1) {
            alert("🔵 Azul venceu!");
            location.reload();
            return true;
        }
        if (vermelhos === 1) {
            alert("🔴 Vermelho venceu!");
            location.reload();
            return true;
        }
    }

    window.aplicarJogada = function (jogada) {
        const disco = discs.find(d => d.id === jogada.discoId);
        if (!disco) {
            return;
        }
        discoLancado = disco;
        disco.vx = jogada.vx;
        disco.vy = jogada.vy;
    }


    // INICIAR JOGO ONLINE
    window.iniciarJogoOnline = function () {
        if (imgBlue.complete && imgRed.complete) {
            startGame();
        } else {
            Promise.all([
                new Promise(r => imgBlue.onload = r),
                new Promise(r => imgRed.onload = r)
            ]).then(startGame);
        }
    }

    window.removerDiscos = function (mensagem) {
        if (!mensagem.discosRemovidos) {
            return;
        }
        discs = discs.filter(d => !mensagem.discosRemovidos.includes(d.id));
        verificarVencedor();
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



// FIM FORA DO START GAME ------------------------------------------------
