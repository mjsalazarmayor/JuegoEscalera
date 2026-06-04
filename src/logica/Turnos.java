package logica;

import java.util.ArrayList;
import java.util.List;
import modelo.Jugador;

public class Turnos {

    private final List<Jugador> jugadores;
    private int indiceActual;

    public Turnos() {
        this.jugadores = new ArrayList<>();
        this.indiceActual = 0;
    }

    public void encolarJugador(Jugador jugador) {
        jugadores.add(jugador);
    }

    public Jugador siguienteTurno() {
        if (jugadores.isEmpty()) return null;
        indiceActual = (indiceActual + 1) % jugadores.size();
        return jugadores.get(indiceActual);
    }

    public Jugador jugadorActual() {
        if (jugadores.isEmpty()) return null;
        return jugadores.get(indiceActual);
    }

    public boolean hayJugadores() {
        return !jugadores.isEmpty();
    }

    public int cantidadJugadores() {
        return jugadores.size();
    }
    
    public List<Jugador> getTodosJugadores() {
        return new ArrayList<>(jugadores);
    }
}