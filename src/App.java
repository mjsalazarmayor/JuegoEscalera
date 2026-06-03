
import logica.JuegoService;

public class App {

    public static void main(String[] args) throws Exception {

        JuegoService juego = new JuegoService();
        juego.iniciar();
        juego.jugar();
    }
}
