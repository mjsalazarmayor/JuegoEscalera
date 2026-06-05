package estructuras;
import modelo.Jugador;

/**
 * Tabla hash de direccionamiento abierto con sondeo lineal.
 * 
 * Función hash: suma de valores ASCII del nombre % longitud.
 * Colisiones: se resuelven avanzando al siguiente índice libre (sondeo lineal).
 * 
 * Tamaño fijo de 10 → funciona bien para los 5 jugadores del juego,
 * pero si se agregaran más jugadores habría que aumentarlo o implementar rehashing.
 */
public class TablaHash {

    private Jugador[] arreglo;
    private int longitud;

    public TablaHash(int longi) {
        longitud = longi;
        arreglo = new Jugador[longi];
    }

    /** Hash simple por suma de ASCII. Puede generar muchas colisiones con nombres cortos similares. */
    public int funcionHash(String nombre) {
        int suma = 0;
        for (int i = 0; i < nombre.length(); i++) {
            suma += nombre.charAt(i);
        }
        return suma % longitud;
    }

    /** Inserta usando sondeo lineal para resolver colisiones. */
    public void insertar(Jugador jugador) {
        int indice = funcionHash(jugador.getNombreUsuario());

        while (arreglo[indice] != null) {
            indice++;
            indice %= longitud;
        }
        arreglo[indice] = jugador;
    }

    public void mostrar() {
        System.out.println("=== TABLA HASH ===");
        for (int i = 0; i < longitud; i++) {
            if (arreglo[i] != null) {
                System.out.println("[" + i + "] " + arreglo[i]);
            } else {
                System.out.println("[" + i + "] vacío");
            }
        }
    }

    /**
     * Búsqueda con sondeo lineal: sigue avanzando mientras haya entradas,
     * hasta encontrar el nombre o recorrer toda la tabla (contador >= longitud).
     */
    public Jugador buscar(String nombre) {
        int indice   = funcionHash(nombre);
        int contador = 0;

        while (arreglo[indice] != null) {
            if (arreglo[indice].getNombreUsuario().equals(nombre)) {
                return arreglo[indice];
            }
            indice++;
            indice %= longitud;
            contador++;
            if (contador >= longitud) break;
        }
        return null;
    }
}