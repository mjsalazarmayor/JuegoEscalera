package estructuras;

import modelo.Jugador;

public class TablaHash {

    private Jugador[] arreglo;
    private int longitud;

    public TablaHash(int longi) {
        longitud = longi;
        arreglo = new Jugador[longi];
    }

    public int funcionHash(String nombre) {
        int suma = 0;
        for (int i = 0; i < nombre.length(); i++) {
            suma += nombre.charAt(i);
        }
        return suma % longitud;
    }

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
