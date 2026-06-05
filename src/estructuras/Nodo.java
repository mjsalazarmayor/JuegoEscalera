package estructuras;

public class Nodo <Tipo> {

    private Tipo dato;              // Valor almacenado en el nodo
    private Nodo <Tipo> sig;        // Referencia al siguiente nodo
    private Nodo <Tipo> ant;        // Referencia al nodo anterior

    public Nodo(Tipo dato) {
        this.dato = dato;
        this.sig = null;            // Inicialmente sin siguiente
        this.ant = null;            // Inicialmente sin anterior
    }

    // Getters y Setters
    public Tipo getDato() {
        return dato;
    }

    public void setDato(Tipo dato) {
        this.dato = dato;
    }

    public Nodo<Tipo> getSig() {
        return sig;
    }

    public void setSig(Nodo<Tipo> sig) {
        this.sig = sig;
    }

    public Nodo<Tipo> getAnt() {
        return ant;
    }

    public void setAnt(Nodo<Tipo> ant) {
        this.ant = ant;
    }

}