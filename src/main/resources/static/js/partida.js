window.Partida = {

    estado: null,
    sala: null,
    meuTime: null,

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