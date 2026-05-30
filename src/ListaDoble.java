
public class ListaDoble {

    private Nodo cabecera;

    public ListaDoble() {
        this.cabecera = null;
    }

    public boolean esVacia() {
        return cabecera == null;
    }

    public void retroceder() {
        if (!esVacia()) {
            while (cabecera.getAnt() != null) {
                cabecera = cabecera.getAnt();
            }
        }
    }

    public void adelantar() {
        if (!esVacia()) {
            while (cabecera.getSig() != null) {
                cabecera = cabecera.getSig();
            }
        }
    }

    public void mostrarDesdePrimero() {
        if (esVacia()) {
            return;
        }
        retroceder();
        Nodo aux = cabecera;
        while (aux != null) {
            System.out.println(aux.getDato());
            aux = aux.getSig();
        }
    }

    public void mostrarDesdeUltimo() {
        if (esVacia()) {
            return;
        }
        adelantar();
        Nodo aux = cabecera;
        while (aux != null) {
            System.out.println(aux.getDato());
            aux = aux.getAnt();
        }
    }

    public void insertarAlPrincipio(Casilla valor) {
        Nodo nuevo = new Nodo();
        nuevo.setDato(valor);
        if (esVacia()) {
            cabecera = nuevo;
        } else {
            retroceder();
            nuevo.setSig(cabecera);
            cabecera.setAnt(nuevo);
            cabecera = nuevo;
        }
    }

    public void insertarAlFinal(Casilla valor) {
        Nodo nuevo = new Nodo();
        nuevo.setDato(valor);
        if (esVacia()) {
            cabecera = nuevo;
        } else {
            adelantar();
            nuevo.setAnt(cabecera);
            cabecera.setSig(nuevo);
            cabecera = nuevo;
        }
    }
}
