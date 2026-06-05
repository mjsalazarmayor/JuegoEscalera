package ordenamiento;

import estructuras.ListaDoble;
import modelo.Jugador;

/**
 * Implementación de MergeSort sobre ListaDoble<Jugador>.
 * 
 * Criterio de orden (descendente):
 *   1. Mayor cantidad de aciertos primero.
 *   2. En empate de aciertos, mayor posición en el tablero.
 * 
 * Complejidad: O(n log n) en todos los casos.
 * Nota: buscarPorIndice() en ListaDoble es O(n), así que la división
 * tiene un costo extra. Si el rendimiento fuera crítico, convendría
 * convertir a arreglo antes de ordenar.
 */
public class MergeSort {

    public static ListaDoble<Jugador> ordenar(ListaDoble<Jugador> lista) {
        if (lista.getTamanio() <= 1) {
            return lista;
        }
        return dividir(lista);
    }

    /** Divide la lista por la mitad de forma recursiva hasta tener sublistas de 1 elemento. */
    private static ListaDoble<Jugador> dividir(ListaDoble<Jugador> lista) {
        if (lista.getTamanio() <= 1) {
            return lista;
        }

        int mitad = lista.getTamanio() / 2;

        ListaDoble<Jugador> izquierda = new ListaDoble<>();
        ListaDoble<Jugador> derecha = new ListaDoble<>();

        for (int i = 0; i < lista.getTamanio(); i++) {
            if (i < mitad) {
                izquierda.insertarAlFinal(lista.buscarPorIndice(i));
            } else {
                derecha.insertarAlFinal(lista.buscarPorIndice(i));
            }
        }

        izquierda = dividir(izquierda);
        derecha = dividir(derecha);

        return mezclar(izquierda, derecha);
    }

    /** Fusiona dos sublistas ya ordenadas comparando aciertos y posición. */
    private static ListaDoble<Jugador> mezclar(ListaDoble<Jugador> izq,
            ListaDoble<Jugador> der) {
        ListaDoble<Jugador> resultado = new ListaDoble<>();
        int i = 0, j = 0;

        while (i < izq.getTamanio() && j < der.getTamanio()) {
            Jugador jugadorIzq = izq.buscarPorIndice(i);
            Jugador jugadorDer = der.buscarPorIndice(j);

            if (jugadorIzq.getAciertos() > jugadorDer.getAciertos()) {
                resultado.insertarAlFinal(jugadorIzq);
                i++;
            } else if (jugadorIzq.getAciertos() < jugadorDer.getAciertos()) {
                resultado.insertarAlFinal(jugadorDer);
                j++;
            } else {
                // Desempate por posición en el tablero
                if (jugadorIzq.getPosicion() >= jugadorDer.getPosicion()) {
                    resultado.insertarAlFinal(jugadorIzq);
                    i++;
                } else {
                    resultado.insertarAlFinal(jugadorDer);
                    j++;
                }
            }
        }

        // Agrega los elementos restantes de la sublista que no se agotó
        while (i < izq.getTamanio()) {
            resultado.insertarAlFinal(izq.buscarPorIndice(i));
            i++;
        }
        while (j < der.getTamanio()) {
            resultado.insertarAlFinal(der.buscarPorIndice(j));
            j++;
        }

        return resultado;
    }

    public static void mostrarRanking(ListaDoble<Jugador> lista) {
        ListaDoble<Jugador> ordenada = ordenar(lista);
        System.out.println("\n=== RANKING FINAL ===");
        for (int i = 0; i < ordenada.getTamanio(); i++) {
            Jugador j = ordenada.buscarPorIndice(i);
            System.out.println((i + 1) + ". " + j.getNombreUsuario()
                    + " | aciertos=" + j.getAciertos()
                    + " | fallos=" + j.getFallos()
                    + " | posicion=" + j.getPosicion());
        }
    }
}