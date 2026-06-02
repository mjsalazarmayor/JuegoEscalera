package estructuras;

public class Cola<Tipo> extends ListaDoble<Tipo> {

    public void encolar(Tipo dato) {
        insertarAlFinal(dato);
    }

    public Tipo desencolar() {
        return eliminarPrimero();
    }

    public Tipo verPrimero(){
        return buscarPorIndice(0);
    }
}
