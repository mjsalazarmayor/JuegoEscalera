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

/**
 * Servicio principal del juego (modo consola).
 * Coordina el tablero, los turnos, el historial y la lógica de retos.
 * 
 * Estructuras usadas:
 *   - TablaHash  → acceso rápido a jugadores por nombre
 *   - Pila       → historial de jugadas (LIFO: última jugada al tope)
 *   - ArbolBST   → preguntas organizadas por dificultad
 *   - Turnos     → cola circular de jugadores
 */
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
    }

    private void registrarJugadorReal() {
        String nombre = "";
        // Valida que el nombre no esté vacío ni tenga formato inválido
        while (!Validador.nombreValido(nombre)) {
            System.out.print("\nIngresa tu nombre: ");
            nombre = scanner.nextLine();
            if (!Validador.nombreValido(nombre)) {
                System.out.println(Validador.mensajeNombre(nombre));
            }
        }
        Jugador jugador = new Jugador(nombre);
        turnos.encolarJugador(jugador);
        tabla.insertar(jugador); // guarda referencia en la hash para búsquedas O(1)
    }

    private void generarBots(int cantidad) {
        String[] nombres = { "Bot-Julian", "Bot-Eddie", "Bot-Daniela", "Bot-Sebastian" };
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
            // Los bots se identifican por el prefijo "Bot-"
            boolean esBot = jugador.getNombreUsuario().startsWith("Bot-");

            System.out.println("\nTurno de: " + jugador.getNombreUsuario()
                    + " (casilla " + jugador.getPosicion() + ")");

            int dado = tirarDado(jugador, esBot);

            // Regla: necesitas número exacto para llegar a la última casilla
            if (!Reglas.puedeMover(jugador.getPosicion(), dado, tablero.getTotalCasillas())) {
                System.out.println("Necesitas exacto para llegar a "
                        + tablero.getTotalCasillas() + ". No te mueves.");
                turnos.siguienteTurno();
                continue;
            }

            int nuevaPos = Reglas.calcularNuevaPos(jugador.getPosicion(), dado);
            jugador.setPosicion(nuevaPos);
            // Registra la jugada en la pila (se muestra al final si se descomenta mostrarHistorial)
            historial.apilar(jugador.getNombreUsuario()
                    + " sacó " + dado + " → casilla " + nuevaPos);

            Casilla casilla = tablero.buscarPorNumero(nuevaPos);
            boolean turnoExtra = procesarCasilla(jugador, casilla, esBot);

            if (Reglas.esGanador(jugador.getPosicion(), tablero.getTotalCasillas())) {
                ganador = jugador.getNombreUsuario();
                break;
            }

            // Si el jugador acertó el reto, lanza de nuevo; si no, ya avanzó el turno dentro de evaluarRespuesta
            if (!turnoExtra) {
                turnos.siguienteTurno();
            } else {
                System.out.println("¡" + jugador.getNombreUsuario() + " tira de nuevo!");
            }
        }

        System.out.println("\n¡" + ganador + " ganó el juego!");
        mostrarRanking();
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

    /**
     * Despacha el efecto de la casilla según su tipo.
     * Retorna true si el jugador gana turno extra (acierto en reto).
     */
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
        // El destino está almacenado en el grafo de adyacencia del tablero
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
        // El nivel del reto se calcula según qué tan avanzado está el jugador en el tablero
        int nivel = Reglas.calcularNivel(jugador.getPosicion(), tablero.getTotalCasillas());
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
            System.out.println(" " + (i + 1) + ". " + CATEGORIAS[i]);
        }
        String entrada = "";
        while (!Validador.opcionValida(entrada, 1, CATEGORIAS.length)) {
            System.out.print("Opcion: ");
            entrada = scanner.nextLine();
            if (!Validador.opcionValida(entrada, 1, CATEGORIAS.length)) {
                System.out.println("Opcion invalida. Ingresa un numero entre 1 y " + CATEGORIAS.length + ".");
            }
        }
        return CATEGORIAS[Integer.parseInt(entrada.trim()) - 1];
    }

    /**
     * Evalúa la respuesta del jugador o del bot.
     * - Acierto → suma punto, devuelve true (turno extra)
     * - Fallo   → suma fallo, avanza el turno y devuelve false
     */
    private boolean evaluarRespuesta(Jugador jugador, Pregunta pregunta, boolean esBot) {
        System.out.println("\n[" + pregunta.getCategoria() + " - Nivel " + pregunta.getDificultad() + "]");
        System.out.println("Pregunta: " + pregunta.getEnunciado());

        String respuesta;
        if (esBot) {
            // El bot acierta con probabilidad 50%
            boolean acierta = random.nextBoolean();
            respuesta = acierta ? pregunta.getRespuesta() : "no se";
            System.out.println("El bot responde: " + respuesta);
        } else {
            respuesta = "";
            while (!Validador.respuestaValida(respuesta)) {
                System.out.print("Tu respuesta: ");
                respuesta = scanner.nextLine();
                if (!Validador.respuestaValida(respuesta)) {
                    System.out.println("La respuesta no puede estar vacia.");
                }
            }
        }

        if (arbol.validar(pregunta, respuesta)) {
            System.out.println("¡Correcto! Ganas un turno extra.");
            jugador.setAciertos(jugador.getAciertos() + 1);
            return true;
        } else {
            System.out.println("Incorrecto. La respuesta era: " + pregunta.getRespuesta());
            System.out.println("Pierdes tu turno.");
            jugador.setFallos(jugador.getFallos() + 1);
            turnos.siguienteTurno(); // penalización: pierde el turno actual
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

    /**
     * Desapila recursivamente para mostrar las últimas N jugadas.
     * Usa recursión en lugar de un loop para aprovechar la pila de llamadas.
     */
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
        MergeSort.mostrarRanking(jugadores); // ordena por aciertos desc, luego por posición
    }

    /**
     * Recolecta todos los jugadores de la cola de turnos de forma recursiva.
     * Importante: esto rota la cola, así que solo debe llamarse al final del juego.
     */
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