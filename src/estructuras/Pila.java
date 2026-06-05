package estructuras;

public class Pila<Tipo> extends ListaDoble<Tipo> {
    
    // LIFO (Last In, First Out)
    
    /**
     * Agrega elemento al tope de la pila
     */
    public void apilar(Tipo dato){
        insertarAlPrincipio(dato);
    }

    /**
     * Remueve y retorna el elemento del tope (el último ingresado)
     */
    public Tipo desapilar(){
        return eliminarPrimero();
    }

    /**
     * Consulta el tope sin removerlo
     */
    public Tipo verTope(){
        return buscarPorIndice(0);
    }
}