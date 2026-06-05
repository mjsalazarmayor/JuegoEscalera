package estructuras;

/**
 * Grafo dirigido implementado con lista de adyacencia.
 * 
 * Cada casilla del tablero es un nodo. Solo las casillas con escalera
 * o serpiente tienen aristas (una arista por casilla especial).
 * 
 * Estructura interna: ListaDoble de ListaDobles.
 *   índice i → lista de destinos de la casilla (i+1)
 * 
 * En este juego cada casilla tiene máximo 1 destino, pero la estructura
 * soporta múltiples aristas por si se quisieran agregar más conexiones.
 */
public class Grafo {

    private ListaDoble<ListaDoble<Integer>> adyacencia;
    private int totalCasillas;

    public Grafo(int totalCasillas) {
        this.totalCasillas = totalCasillas;
        this.adyacencia = new ListaDoble<>();

        // Inicializa una lista vacía de vecinos para cada casilla
        for (int i = 1; i <= totalCasillas; i++) {
            adyacencia.insertarAlFinal(new ListaDoble<>());
        }
    }

    private ListaDoble<Integer> getListaCasilla(int casilla) {
        if (casilla < 1 || casilla > totalCasillas) {
            return null;
        }
        return adyacencia.buscarPorIndice(casilla - 1);
    }

    public void agregarArista(int origen, int destino) {
        if (origen < 1 || origen > totalCasillas) {
            return;
        }

        if (destino < 1 || destino > totalCasillas) {
            return;
        }

        getListaCasilla(origen).insertarAlFinal(destino);
    }

    /** Retorna el primer (y único en este juego) destino de la casilla, o -1 si no tiene. */
    public int obtenerDestino(int casilla) {
        ListaDoble<Integer> lista = getListaCasilla(casilla);

        if (lista == null || lista.esVacia()) {
            return -1;
        }

        return lista.buscarPorIndice(0);
    }

    public boolean tieneConexion(int casilla) {
        ListaDoble<Integer> lista = getListaCasilla(casilla);
        return lista != null && !lista.esVacia();
    }

    /** Recorrido DFS desde una casilla, siguiendo sus conexiones. Usado para debug. */
    public void recorrerDesde(int casilla) {
        ListaDoble<Integer> visitados = new ListaDoble<>();
        recorrerRecursivo(casilla, visitados);
    }

    private void recorrerRecursivo(int casilla, ListaDoble<Integer> visitados) {
        if (casilla < 1 || casilla > totalCasillas) {
            return;
        }

        // Evita ciclos verificando si ya fue visitado
        for (int i = 0; i < visitados.getTamanio(); i++) {
            Integer visitado = visitados.buscarPorIndice(i);
            if (visitado != null && visitado == casilla) {
                return;
            }
        }

        visitados.insertarAlFinal(casilla);

        int destino = obtenerDestino(casilla);

        if (destino != -1) {
            System.out.println(casilla + " -> " + destino);
            recorrerRecursivo(destino, visitados);
        }
    }

    public void mostrar() {
        System.out.println("=== GRAFO (CONEXIONES) ===");
        for (int i = 1; i <= totalCasillas; i++) {
            if (tieneConexion(i)) {
                System.out.println("Casilla " + i + " -> " + obtenerDestino(i));
            }
        }
    }
}