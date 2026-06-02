
import logica.Tablero;
import logica.Turnos;
import modelo.Jugador;

public class App {

    public static void main(String[] args) throws Exception {

        Tablero tablero = new Tablero();
        tablero.mostrar();

        Turnos turnos = new Turnos();
        turnos.agregarJugador(new Jugador("Julian"));
        turnos.agregarJugador(new Jugador("Maria Jose"));
        turnos.agregarJugador(new Jugador("Eddie"));

        for (int i = 0; i < 5; i++) {
            Jugador j = turnos.siguienteTurno();
            System.out.println("Turno " + (i + 1) + ": " + j.getNombreUsuario());
        }
    }
}
