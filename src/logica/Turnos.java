package logica;

import estructuras.Cola;
import modelo.Jugador;

public class Turnos {

    private final Cola<Jugador> cola;

    public Turnos() {
        this.cola = new Cola<>();
    }

    public void encolarJugador(Jugador jugador) {
        cola.encolar(jugador);
    }

    public Jugador siguienteTurno() {
        Jugador actual = cola.desencolar();
        cola.encolar(actual);
        return actual;
    }

    public Jugador jugadorActual() {
        return cola.verPrimero();
    }

    public boolean hayJugadores() {
        return !cola.esVacia();
    }

    public int cantidadJugadores() {
        return cola.getTamanio();
    }

}
