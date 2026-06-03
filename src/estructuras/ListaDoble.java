package estructuras;

public class ListaDoble<Tipo> {

    private Nodo<Tipo> cabecera;
    private Nodo<Tipo> cola;
    private int tamaño;

    public ListaDoble() {
        this.cabecera = null;
        this.cola = null;
        this.tamaño = 0;
    }

    public boolean esVacia() {
        return cabecera == null;
    }

    public int getTamanio() {
        return tamaño;
    }

    // metodos de mostrar
    public void mostrarDesdePrimero() {
        if (esVacia()) {
            return;
        }
        Nodo<Tipo> aux = cabecera;
        while (aux != null) {
            System.out.println(aux.getDato());
            aux = aux.getSig();
        }
    }

    public void mostrarDesdeUltimo() {
        if (esVacia()) {
            return;
        }
        Nodo<Tipo> aux = cola;
        while (aux != null) {
            System.out.println(aux.getDato());
            aux = aux.getAnt();
        }
    }

    // metodos insertar
    public void insertarAlPrincipio(Tipo dato) {
        Nodo<Tipo> nuevo = new Nodo<>(dato);
        if (esVacia()) {
            cabecera = nuevo;
            cola = nuevo;
        } else {
            nuevo.setSig(cabecera);
            cabecera.setAnt(nuevo);
            cabecera = nuevo;
        }
        tamaño++;

    }

    public void insertarAlFinal(Tipo dato) {
        Nodo<Tipo> nuevo = new Nodo<>(dato);
        if (esVacia()) {
            cabecera = nuevo;
            cola = nuevo;
        } else {
            nuevo.setAnt(cola);
            cola.setSig(nuevo);
            cola = nuevo;
        }
        tamaño++;
    }

    // metodos eliminar

    public Tipo eliminarPrimero(){
        if (esVacia()) {
            return null;
        }
        Tipo valor = cabecera.getDato();
        cabecera = cabecera.getSig();
        if (cabecera != null) {
            cabecera.setAnt(null);
        } else {
            cola = null;
        }
        tamaño--;
        return valor;
    }

    public Tipo eliminarUltimo(){
        if (esVacia()) {
            return null;
        }
        Tipo valor = cola.getDato();
        cola = cola.getAnt();
        if (cola != null) {
            cola.setSig(null);
        } else {
            cabecera = null;
        }
        tamaño--;
        return valor;
    }

    // metodo para buscar
    public Tipo buscarPorIndice(int indice) {
        if (indice < 0 || indice >= tamaño) return null;
        Nodo<Tipo> aux = cabecera;
        int contador   = 0;
        while (aux != null) {
            if (contador == indice) return aux.getDato();
            aux = aux.getSig();
            contador++;
        }
        return null;
    }
}
