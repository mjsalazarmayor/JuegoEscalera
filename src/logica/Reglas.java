package logica;

public class Reglas {

    private Reglas() {
    }

    public static boolean puedeMover(int posicion, int dado, int meta) {
        return dado > 0 && posicion + dado <= meta;
    }

    public static int calcularNuevaPos(int posActual, int dado) {
        return posActual + dado;
    }

    public static int calcularNivel(int posicion, int meta) {
        int tercio = meta / 3;
        if (posicion <= tercio) {
            return 1;
        }
        if (posicion <= tercio * 2) {
            return 2;
        }
        return 3;
    }

    public static boolean esGanador(int posicion, int meta) {
        return posicion == meta;
    }
}
