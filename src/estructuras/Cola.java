package estructuras;

public class Cola<Tipo> extends ListaDoble<Tipo> {

    // FIFO (First In, First Out)
    
    /**
     * Agrega elemento al final de la cola
     */
    public void encolar(Tipo dato) {
        insertarAlFinal(dato);
    }

    /**
     * Remueve y retorna el primer elemento (el más antiguo)
     */
    public Tipo desencolar() {
        return eliminarPrimero();
    }

    /**
     * Consulta el primer elemento sin removerlo
     */
    public Tipo verPrimero(){
        return buscarPorIndice(0);
    }
}