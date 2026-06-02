package logica;

import estructuras.ListaDoble;
import java.util.Random;
import modelo.Casilla;

public class Tablero {

    private static final int TOTAL_CASILLAS = 20;
    private static final int ESCALERAS = 3;
    private static final int SERPIENTES = 2;
    private static final int RETOS = 3;

    private final ListaDoble<Casilla> lista;
    private final Random random;

    public Tablero() {
        this.lista = new ListaDoble<>();
        this.random = new Random();
        crearTablero();
    }

    public void crearTablero() {
        for (int i = 1; i <= TOTAL_CASILLAS; i++) {
            lista.insertarAlFinal(new Casilla(i, "NORMAL", -1));

        }

        boolean[] usada = new boolean[TOTAL_CASILLAS + 1];
        usada[1] = true;
        usada[TOTAL_CASILLAS] = true;

        colocarCasillasEspeciales("ESCALERA", ESCALERAS, true, usada);
        colocarCasillasEspeciales("SERPIENTE", SERPIENTES, false, usada);
        colocarRetos(usada);
    }
    // coloca escaleras o serpientes de forma aleatoria

    private void colocarCasillasEspeciales(String tipo, int cantidad, boolean sube, boolean[] usada) {
        int contador = 0;
        int intentos = 0;
        int maxIntentos = 1000;

        while (contador < cantidad && intentos < maxIntentos) {
            intentos++;

            int origen, destino;

            if (sube) {
                origen = aleatorio(2, TOTAL_CASILLAS - 4);
                destino = aleatorio(origen + 2, TOTAL_CASILLAS - 1);
            } else {
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

    // buscar casilla
    public Casilla buscarPorNumero(int numero) {
        for (int i = 0; i < lista.getTamaño(); i++) {
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

    // mostrar tablero
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
        System.out.println();
        mostrarConexiones();
    }

    // un solo método reutilizable para el símbolo
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
