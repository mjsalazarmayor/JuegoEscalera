package estructuras;

public class Grafo {

    private ListaDoble<ListaDoble<Integer>> adyacencia;
    private int totalCasillas;

    public Grafo(int totalCasillas) {
        this.totalCasillas = totalCasillas;
        this.adyacencia = new ListaDoble<>();

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

    public void recorrerDesde(int casilla) {
        ListaDoble<Integer> visitados = new ListaDoble<>();
        recorrerRecursivo(casilla, visitados);
    }

    private void recorrerRecursivo(int casilla, ListaDoble<Integer> visitados) {

        if (casilla < 1 || casilla > totalCasillas) {
            return;
        }

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