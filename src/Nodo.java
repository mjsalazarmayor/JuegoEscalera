
public class Nodo {

    private Casilla dato;
    private Nodo sig;
    private Nodo ant;

    public Nodo() {
        this.dato = null;
        this.sig = null;
        this.ant = null;
    }

    public Casilla getDato() {
        return dato;
    }

    public void setDato(Casilla dato) {
        this.dato = dato;
    }

    public Nodo getSig() {
        return sig;
    }

    public void setSig(Nodo sig) {
        this.sig = sig;
    }

    public Nodo getAnt() {
        return ant;
    }

    public void setAnt(Nodo ant) {
        this.ant = ant;
    }

}
