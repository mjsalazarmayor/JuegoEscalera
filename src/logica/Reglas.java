package logica;

public class Reglas {

    private Reglas() {
        // Constructor privado para evitar instanciación (clase de utilidad)
    }

    /**
     * Verifica si un movimiento es válido según las reglas básicas
     */
    public static boolean puedeMover(int posicion, int dado, int meta) {
        // El dado debe ser positivo y no superar la meta
        return dado > 0 && posicion + dado <= meta;
    }

    public static int calcularNuevaPos(int posActual, int dado) {
        return posActual + dado;
    }

    /**
     * Divide el tablero en 3 niveles según la posición actual
     */
    public static int calcularNivel(int posicion, int meta) {
        int tercio = meta / 3;
        
        if (posicion <= tercio) {
            return 1;  // Nivel inicial
        }
        if (posicion <= tercio * 2) {
            return 2;  // Nivel intermedio
        }
        return 3;  // Nivel final
    }

    /**
     * Determina si la posición actual es la meta (casilla ganadora)
     */
    public static boolean esGanador(int posicion, int meta) {
        return posicion == meta;
    }
}