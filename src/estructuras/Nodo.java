package estructuras;

public class Nodo <Tipo> {

    private Tipo dato;
    private Nodo <Tipo> sig;
    private Nodo <Tipo> ant;

    public Nodo(Tipo dato) {
        this.dato = dato;
        this.sig = null;
        this.ant = null;
    }

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