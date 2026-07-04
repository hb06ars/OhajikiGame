window.Partida = {

    jogador: null,
    estado: null,
    sala: null,
    meuTime: null,

    setJogador(jogador){
        this.jogador = jogador;
    },

    getJogador(){
        return this.jogador;
    },

    setEstado(estado){
        this.estado = estado;
    },

    getEstado(){
        return this.estado;
    },

    setSala(codigo){
        this.sala = codigo;
    },

    getSala(){
        return this.sala;
    }
}