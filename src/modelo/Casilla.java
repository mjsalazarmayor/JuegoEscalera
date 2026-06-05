package modelo;

public class Casilla {

    private int numero;      // Número de la casilla (posición en el tablero)
    private String tipo;     // Tipo: "normal", "escalera", "serpiente"
    private int destino;     // Si es escalera/serpiente, a qué casilla lleva

    public Casilla(int numero, String tipo, int destino) {
        this.numero = numero;
        this.tipo = tipo;
        this.destino = destino;
    }

    // Getters y Setters
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