package modelo;

public class Casilla {

    private int numero;
    private String tipo;
    private int destino;

    public Casilla(int numero, String tipo, int destino) {
        this.numero = numero;
        this.tipo = tipo;
        this.destino = destino;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }

    @Override
    public String toString() {
        return "Casilla [numero=" + numero + ", tipo=" + tipo + ", destino=" + destino + "]";
    }

}
