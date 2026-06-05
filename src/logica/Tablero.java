package logica;

import estructuras.Grafo;
import estructuras.ListaDoble;
import java.util.Random;
import modelo.Casilla;

/**
 * Representa el tablero de juego de 50 casillas.
 * 
 * Al construirse, genera aleatoriamente:
 *   - 5 escaleras (suben al jugador)
 *   - 4 serpientes (bajan al jugador)
 *   - 5 retos (preguntas de trivia)
 * 
 * Las conexiones escalera/serpiente se almacenan en un Grafo de adyacencia,
 * donde cada nodo origen apunta a su destino.
 */
public class Tablero {

    private static final int TOTAL_CASILLAS = 50;
    private static final int ESCALERAS = 5;
    private static final int SERPIENTES = 4;
    private static final int RETOS = 5;

    private final ListaDoble<Casilla> lista;
    private Grafo grafo;
    private final Random random;

    public Tablero() {
        this.lista = new ListaDoble<>();
        this.random = new Random();
        this.grafo = new Grafo(TOTAL_CASILLAS);
        crearTablero();
    }

    public void crearTablero() {
        // Primero crea todas las casillas como NORMAL
        for (int i = 1; i <= TOTAL_CASILLAS; i++) {
            lista.insertarAlFinal(new Casilla(i, "NORMAL", -1));
        }

        // usada[] evita colocar dos efectos en la misma casilla
        // La casilla 1 (inicio) y 50 (meta) quedan siempre libres
        boolean[] usada = new boolean[TOTAL_CASILLAS + 1];
        usada[1] = true;
        usada[TOTAL_CASILLAS] = true;

        colocarCasillasEspeciales("ESCALERA", ESCALERAS, true, usada);
        colocarCasillasEspeciales("SERPIENTE", SERPIENTES, false, usada);
        colocarRetos(usada);
    }

    /**
     * Coloca escaleras o serpientes en posiciones aleatorias no repetidas.
     * 
     * @param sube  true = escalera (destino mayor), false = serpiente (destino menor)
     * Tiene un límite de 1000 intentos para evitar loop infinito si el tablero está muy lleno.
     */
    private void colocarCasillasEspeciales(String tipo, int cantidad, boolean sube, boolean[] usada) {
        int contador = 0;
        int intentos = 0;
        int maxIntentos = 1000;

        while (contador < cantidad && intentos < maxIntentos) {
            intentos++;

            int origen, destino;

            if (sube) {
                // Escalera: origen en zona media-baja, destino siempre más arriba
                origen = aleatorio(2, TOTAL_CASILLAS - 4);
                destino = aleatorio(origen + 2, TOTAL_CASILLAS - 1);
            } else {
                // Serpiente: origen en zona alta, destino siempre más abajo
                origen = aleatorio(4, TOTAL_CASILLAS - 1);
                destino = aleatorio(2, origen - 2);
            }

            if (!usada[origen] && !usada[destino]) {
                Casilla c = buscarPorNumero(origen);
                if (c == null) {
                    continue;
                }
                c.setTipo(tipo);
                c.setDestino(destino);
                // Registra la conexión en el grafo para consultarla después con obtenerDestino()
                grafo.agregarArista(origen, destino);
                usada[origen] = true;
                usada[destino] = true;
                contador++;
            }
        }
    }

    private void colocarRetos(boolean[] usada) {
        int contador = 0;
        while (contador < RETOS) {
            int pos = aleatorio(2, TOTAL_CASILLAS - 1);
            if (!usada[pos]) {
                buscarPorNumero(pos).setTipo("RETO");
                usada[pos] = true;
                contador++;
            }
        }
    }

    /**
     * Búsqueda lineal O(n) en la ListaDoble.
     * Nota: con 50 casillas el impacto es mínimo, pero si el tablero creciera
     * sería mejor usar un arreglo o mapa indexado por número de casilla.
     */
    public Casilla buscarPorNumero(int numero) {
        for (int i = 0; i < lista.getTamanio(); i++) {
            Casilla c = lista.buscarPorIndice(i);
            if (c.getNumero() == numero) {
                return c;
            }
        }
        return null;
    }

    private int aleatorio(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public ListaDoble<Casilla> getLista() {
        return lista;
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public static int getTotalCasillas() {
        return TOTAL_CASILLAS;
    }

    // Imprime el tablero en forma de serpentín (zigzag) de 5 columnas
    public void mostrar() {
        System.out.println("========== TABLERO ==========");
        int columnas = 5;
        for (int fila = (TOTAL_CASILLAS / columnas) - 1; fila >= 0; fila--) {
            int inicio = fila * columnas + 1;
            int fin = inicio + columnas - 1;
            if (fila % 2 == 0) {
                for (int n = fin; n >= inicio; n--) {
                    System.out.print(formatearCasilla(n));
                }
            } else {
                for (int n = inicio; n <= fin; n++) {
                    System.out.print(formatearCasilla(n));
                }
            }
            System.out.println();
        }
        System.out.println("=============================");
        System.out.println("E=Escalera  S=Serpiente  R=Reto  .=Normal");
    }

    // Un solo método reutilizable para el símbolo de cada tipo
    private String getSimbolo(String tipo) {
        return switch (tipo) {
            case "ESCALERA" ->
                "E";
            case "SERPIENTE" ->
                "S";
            case "RETO" ->
                "R";
            default ->
                ".";
        };
    }

    private String formatearCasilla(int numero) {
        Casilla c = buscarPorNumero(numero);
        return String.format(" [%2d%s]", numero, getSimbolo(c.getTipo()));
    }

    private void mostrarConexiones() {
        System.out.println("Conexiones:");
        for (int i = 1; i <= TOTAL_CASILLAS; i++) {
            Casilla c = buscarPorNumero(i);
            switch (c.getTipo()) {
                case "ESCALERA" ->
                    System.out.println("  Escalera:  " + c.getNumero() + " -> " + c.getDestino() + " (sube)");
                case "SERPIENTE" ->
                    System.out.println("  Serpiente: " + c.getNumero() + " -> " + c.getDestino() + " (baja)");
            }
        }
    }
}