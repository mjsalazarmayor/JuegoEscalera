package logica;

import estructuras.ArbolBST;
import estructuras.ListaDoble;
import estructuras.Pila;
import estructuras.TablaHash;
import java.util.Random;
import java.util.Scanner;
import modelo.Casilla;
import modelo.Jugador;
import modelo.Pregunta;
import ordenamiento.MergeSort;

public class JuegoService {

    private final Tablero tablero;
    private final Turnos turnos;
    private final TablaHash tabla;
    private final Pila<String> historial;
    private final ArbolBST arbol;
    private final Random random;
    private final Scanner scanner;

    private static final String[] CATEGORIAS = {
        "Matematicas", "Geografia", "Literatura", "Deportes", "Entretenimiento"
    };

    public JuegoService() {
        this.tablero = new Tablero();
        this.turnos = new Turnos();
        this.tabla = new TablaHash(10);
        this.historial = new Pila<>();
        this.arbol = new ArbolBST();
        this.random = new Random();
        this.scanner = new Scanner(System.in);
    }

    // -------------------------------------------------------
    // Iniciar el juego
    // -------------------------------------------------------
    public void iniciar() {
        tablero.mostrar();
        registrarJugadorReal();
        generarBots(4);
        System.out.println("\n¡El juego comienza!");
        // tabla.mostrar();
    }

    private void registrarJugadorReal() {
        System.out.print("\nIngresa tu nombre: ");
        String nombre = scanner.nextLine().trim();
        Jugador jugador = new Jugador(nombre);
        turnos.encolarJugador(jugador);
        tabla.insertar(jugador);
    }

    private void generarBots(int cantidad) {
        String[] nombres = {"Bot-Julian", "Bot-Eddie", "Bot-Daniela", "Bot-Sebastian"};
        for (int i = 0; i < cantidad; i++) {
            Jugador bot = new Jugador(nombres[i]);
            turnos.encolarJugador(bot);
            tabla.insertar(bot);
        }
    }

    // -------------------------------------------------------
    // Bucle principal del juego
    // -------------------------------------------------------
    public void jugar() {
        System.out.println("\n=== INICIO DEL JUEGO ===");
        String ganador = null;

        while (ganador == null) {
            Jugador jugador = turnos.jugadorActual();
            boolean esBot = jugador.getNombreUsuario().startsWith("Bot-");

            System.out.println("\nTurno de: " + jugador.getNombreUsuario()
                    + " (casilla " + jugador.getPosicion() + ")");

            int dado = tirarDado(jugador, esBot);

            if (!Reglas.puedeMover(jugador.getPosicion(), dado, tablero.getTotalCasillas())) {
                System.out.println("Necesitas exacto para llegar a "
                        + tablero.getTotalCasillas() + ". No te mueves.");
                turnos.siguienteTurno();
                continue;
            }

            int nuevaPos = Reglas.calcularNuevaPos(jugador.getPosicion(), dado);
            jugador.setPosicion(nuevaPos);
            historial.apilar(jugador.getNombreUsuario()
                    + " sacó " + dado + " → casilla " + nuevaPos);

            Casilla casilla = tablero.buscarPorNumero(nuevaPos);
            boolean turnoExtra = procesarCasilla(jugador, casilla, esBot);

            if (Reglas.esGanador(jugador.getPosicion(),tablero.getTotalCasillas())) {
                ganador = jugador.getNombreUsuario();
                break;
            }

            if (!turnoExtra) {
                turnos.siguienteTurno();
            } else {
                System.out.println("¡" + jugador.getNombreUsuario() + " tira de nuevo!");
            }
        }

        System.out.println("\n¡" + ganador + " ganó el juego!");
        // mostrarHistorial();
        mostrarRanking();
        // mostrarEstado();
    }

    // -------------------------------------------------------
    // Dado
    // -------------------------------------------------------
    private int tirarDado(Jugador jugador, boolean esBot) {
        int dado = random.nextInt(6) + 1;
        if (esBot) {
            System.out.println("El bot tira el dado: " + dado);
        } else {
            System.out.print("Presiona ENTER para tirar el dado...");
            scanner.nextLine();
            System.out.println("Sacaste: " + dado);
        }
        return dado;
    }

    // -------------------------------------------------------
    // Procesar casilla
    // -------------------------------------------------------
    private boolean procesarCasilla(Jugador jugador, Casilla casilla, boolean esBot) {
        return switch (casilla.getTipo()) {
            case "ESCALERA" ->
                procesarEscalera(jugador, casilla);
            case "SERPIENTE" ->
                procesarSerpiente(jugador, casilla);
            case "RETO" ->
                procesarReto(jugador, esBot);
            default -> {
                System.out.println("Casilla normal.");
                yield false;
            }
        };
    }

    private boolean procesarEscalera(Jugador jugador, Casilla casilla) {
        int destino = tablero.getGrafo().obtenerDestino(casilla.getNumero());
        System.out.println("¡Escalera! Subes a la casilla " + destino);
        jugador.setPosicion(destino);
        return false;
    }

    private boolean procesarSerpiente(Jugador jugador, Casilla casilla) {
        int destino = tablero.getGrafo().obtenerDestino(casilla.getNumero());
        System.out.println("¡Serpiente! Bajas a la casilla " + destino);
        jugador.setPosicion(destino);
        return false;
    }

    private boolean procesarReto(Jugador jugador, boolean esBot) {
        System.out.println("¡RETO!");
        String categoria = elegirCategoria(esBot);
        int nivel = Reglas.calcularNivel(jugador.getPosicion(),tablero.getTotalCasillas());
        Pregunta pregunta = arbol.buscar(nivel, categoria);

        if (pregunta == null) {
            System.out.println("No hay pregunta disponible. Turno normal.");
            return false;
        }

        return evaluarRespuesta(jugador, pregunta, esBot);
    }

    // -------------------------------------------------------
    // Reto
    // -------------------------------------------------------
    private String elegirCategoria(boolean esBot) {
        if (esBot) {
            String cat = CATEGORIAS[random.nextInt(CATEGORIAS.length)];
            System.out.println("El bot eligio: " + cat);
            return cat;
        }

        System.out.println("Elige una categoria:");
        for (int i = 0; i < CATEGORIAS.length; i++) {
            System.out.println("  " + (i + 1) + ". " + CATEGORIAS[i]);
        }
        System.out.print("Opcion: ");

        int opcion;
        try {
            opcion = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            opcion = 1;
        }

        if (opcion < 1 || opcion > CATEGORIAS.length) {
            opcion = 1;
        }
        return CATEGORIAS[opcion - 1];
    }

    private boolean evaluarRespuesta(Jugador jugador, Pregunta pregunta, boolean esBot) {
        System.out.println("\n[" + pregunta.getCategoria()
                + " - Nivel " + pregunta.getDificultad() + "]");
        System.out.println("Pregunta: " + pregunta.getEnunciado());

        String respuesta;
        if (esBot) {
            boolean acierta = random.nextBoolean();
            respuesta = acierta ? pregunta.getRespuesta() : "no se";
            System.out.println("El bot responde: " + respuesta);
        } else {
            System.out.print("Tu respuesta: ");
            respuesta = scanner.nextLine();
        }

        if (arbol.validar(pregunta, respuesta)) {
            System.out.println("¡Correcto! Ganas un turno extra.");
            jugador.setAciertos(jugador.getAciertos() + 1);
            return true;
        } else {
            System.out.println("Incorrecto. La respuesta era: "
                    + pregunta.getRespuesta());
            System.out.println("Pierdes tu turno.");
            jugador.setFallos(jugador.getFallos() + 1);
            turnos.siguienteTurno();
            return false;
        }
    }

    // -------------------------------------------------------
    // Mostrar resultados
    // -------------------------------------------------------
    public void mostrarHistorial() {
        System.out.println("\n=== ULTIMAS JUGADAS ===");
        mostrarHistorialRecursivo(5);
    }

    private void mostrarHistorialRecursivo(int cantidad) {
        if (cantidad == 0) {
            return;
        }
        String jugada = historial.desapilar();
        if (jugada != null) {
            System.out.println(jugada);
            mostrarHistorialRecursivo(cantidad - 1);
        }
    }

    public void mostrarRanking() {
        ListaDoble<Jugador> jugadores = new ListaDoble<>();
        recolectarRecursivo(jugadores, turnos.cantidadJugadores());
        MergeSort.mostrarRanking(jugadores);
    }

    private void recolectarRecursivo(ListaDoble<Jugador> lista, int restantes) {
        if (restantes == 0) {
            return;
        }
        Jugador j = turnos.siguienteTurno();
        lista.insertarAlFinal(j);
        recolectarRecursivo(lista, restantes - 1);
    }

    public void mostrarEstado() {
        tabla.mostrar();
    }
}
