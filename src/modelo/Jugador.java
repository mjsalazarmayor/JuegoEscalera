package modelo;

public class Jugador {

    private String nombreUsuario;
    private int posicion;      // Casilla actual en el tablero
    private int aciertos;      // Preguntas respondidas correctamente
    private int fallos;        // Preguntas respondidas incorrectamente

    public Jugador(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        this.posicion = 1;     // Todos empiezan en la casilla 1
        this.aciertos = 0;
        this.fallos = 0;
    }

    // Getters y Setters
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public int getAciertos() {
        return aciertos;
    }

    public void setAciertos(int aciertos) {
        this.aciertos = aciertos;
    }

    public int getFallos() {
        return fallos;
    }

    public void setFallos(int fallos) {
        this.fallos = fallos;
    }

    @Override
    public String toString() {
        return "Jugador [nombreUsuario=" + nombreUsuario + ", posicion=" + posicion + "]";
    }

}