package estructuras;

public class ListaDoble<Tipo> {

    private Nodo<Tipo> cabecera;  // Primer nodo
    private Nodo<Tipo> cola;      // Último nodo
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

    // ============ MÉTODOS DE MOSTRAR ============
    
    public void mostrarDesdePrimero() {
        if (esVacia()) return;
        Nodo<Tipo> aux = cabecera;
        while (aux != null) {
            System.out.println(aux.getDato());
            aux = aux.getSig();
        }
    }

    public void mostrarDesdeUltimo() {
        if (esVacia()) return;
        Nodo<Tipo> aux = cola;
        while (aux != null) {
            System.out.println(aux.getDato());
            aux = aux.getAnt();
        }
    }

    // ============ MÉTODOS INSERTAR ============
    
    public void insertarAlPrincipio(Tipo dato) {
        Nodo<Tipo> nuevo = new Nodo<>(dato);
        if (esVacia()) {
            cabecera = nuevo;
            cola = nuevo;
        } else {
            nuevo.setSig(cabecera);  // Nuevo apunta a la antigua cabecera
            cabecera.setAnt(nuevo);   // Antigua cabecera apunta atrás al nuevo
            cabecera = nuevo;         // Actualizar cabecera
        }
        tamaño++;
    }

    public void insertarAlFinal(Tipo dato) {
        Nodo<Tipo> nuevo = new Nodo<>(dato);
        if (esVacia()) {
            cabecera = nuevo;
            cola = nuevo;
        } else {
            nuevo.setAnt(cola);      // Nuevo apunta atrás a la antigua cola
            cola.setSig(nuevo);       // Antigua cola apunta adelante al nuevo
            cola = nuevo;             // Actualizar cola
        }
        tamaño++;
    }

    // ============ MÉTODOS ELIMINAR ============
    
    public Tipo eliminarPrimero(){
        if (esVacia()) return null;
        
        Tipo valor = cabecera.getDato();
        cabecera = cabecera.getSig();  // Movemos cabecera al siguiente
        
        if (cabecera != null) {
            cabecera.setAnt(null);     // Nueva cabecera no tiene anterior
        } else {
            cola = null;               // Lista quedó vacía
        }
        tamaño--;
        return valor;
    }

    public Tipo eliminarUltimo(){
        if (esVacia()) return null;
        
        Tipo valor = cola.getDato();
        cola = cola.getAnt();          // Movemos cola al anterior
        
        if (cola != null) {
            cola.setSig(null);         // Nueva cola no tiene siguiente
        } else {
            cabecera = null;           // Lista quedó vacía
        }
        tamaño--;
        return valor;
    }

    // ============ MÉTODO BUSCAR ============
    
    /**
     * Busca elemento por su posición (0 = primero, 1 = segundo...)
     */
    public Tipo buscarPorIndice(int indice) {
        if (indice < 0 || indice >= tamaño) return null;
        
        Nodo<Tipo> aux = cabecera;
        int contador = 0;
        while (aux != null) {
            if (contador == indice) return aux.getDato();
            aux = aux.getSig();
            contador++;
        }
        return null;
    }
}