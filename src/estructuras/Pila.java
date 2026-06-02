package estructuras;

public class Pila<Tipo> extends ListaDoble<Tipo> {
    public void apilar(Tipo dato){
        insertarAlPrincipio(dato);
    }

    public Tipo desapilar(){
        return eliminarPrimero();
    }

    public Tipo verTope(){
        return buscarPorIndice(0);
    }
}
