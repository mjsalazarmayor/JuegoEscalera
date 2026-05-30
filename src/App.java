
public class App {

    public static void main(String[] args) throws Exception {

        ListaDoble tablero = new ListaDoble();

        tablero.insertarAlFinal(new Casilla(1, "NORMAL", 0));
        tablero.insertarAlFinal(new Casilla(2, "NORMAL", 0));
        tablero.insertarAlFinal(new Casilla(3, "ESCALERA", 10));
        tablero.insertarAlFinal(new Casilla(4, "NORMAL", 0));
        tablero.insertarAlFinal(new Casilla(5, "SERPIENTE", 2));

        System.out.println("=== Desde el primero ===");
        tablero.mostrarDesdePrimero();

        System.out.println("\n=== Desde el último ===");
        tablero.mostrarDesdeUltimo();
    }
}
