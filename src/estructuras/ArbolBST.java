package estructuras;

import modelo.Pregunta;

public class ArbolBST {

    private Nodo<Pregunta> raiz;

    public ArbolBST() {
        this.raiz = null;
        generarPreguntas();
    }

    public void insertar(Pregunta p) {
        if (raiz == null) {
            raiz = new Nodo<>(p);
        } else {
            insertarEn(raiz, p);
        }
    }

    private void insertarEn(Nodo<Pregunta> nodo, Pregunta p) {
        if (p.getDificultad() <= nodo.getDato().getDificultad()) {
            if (nodo.getAnt() == null) {
                nodo.setAnt(new Nodo<>(p));
            } else {
                insertarEn(nodo.getAnt(), p); 
            }
        } else {
            if (nodo.getSig() == null) {
                nodo.setSig(new Nodo<>(p));
            } else {
                insertarEn(nodo.getSig(), p);
            }
        }
    }

    public Pregunta buscar(int dificultad, String categoria) {
        ListaDoble<Pregunta> candidatas = new ListaDoble<>();
        recolectar(raiz, dificultad, categoria.toLowerCase(), candidatas);
        if (candidatas.esVacia()) return null;
        int indice = (int) (Math.random() * candidatas.getTamanio());
        return candidatas.buscarPorIndice(indice);
    }

    private void recolectar(Nodo<Pregunta> nodo, int dif, String cat,
                             ListaDoble<Pregunta> lista) {
        if (nodo == null) return;
        Pregunta p = nodo.getDato();
        if (p.getDificultad() == dif && p.getCategoria().toLowerCase().equals(cat)) {
            lista.insertarAlFinal(p);
        }
        if (dif < p.getDificultad()) {
            recolectar(nodo.getAnt(), dif, cat, lista); 
        } else if (dif > p.getDificultad()) {
            recolectar(nodo.getSig(), dif, cat, lista); 
        } else {
            recolectar(nodo.getAnt(), dif, cat, lista); 
            recolectar(nodo.getSig(), dif, cat, lista); 
        }
    }

    public boolean validar(Pregunta pregunta, String respuestaJugador) {
        return pregunta.getRespuesta().trim().equalsIgnoreCase(respuestaJugador.trim());
    }

    public void inorden() {
        System.out.println("=== Arbol inorden ===");
        inordenNodo(raiz);
        System.out.println();
    }

    private void inordenNodo(Nodo<Pregunta> nodo) {
        if (nodo == null) return;
        inordenNodo(nodo.getAnt());           
        System.out.println(nodo.getDato());
        inordenNodo(nodo.getSig());           
    }

    private void generarPreguntas() {

        // nivel 1
        insertar(new Pregunta(1, "Matematicas",     "Si 3/4 de un numero es 48, ¿cual es el numero?",                    "64"));
        insertar(new Pregunta(1, "Geografia",        "Un mapa escala 1:50000 muestra 2 puntos a 3 cm. ¿Distancia real?",  "1.5"));
        insertar(new Pregunta(1, "Literatura",       "En Cien anios de soledad, ¿que tema representa la soledad?",         "soledad"));
        insertar(new Pregunta(1, "Deportes",         "¿Cuantos jugadores tiene un equipo de futbol en la cancha?",        "11"));
        insertar(new Pregunta(1, "Entretenimiento",  "¿Que actor interpreto a Iron Man en el MCU?",                       "robert downey jr"));
        insertar(new Pregunta(1, "Matematicas",      "¿Cual es el 35% del 40% de 2000?",                                 "280"));
        insertar(new Pregunta(1, "Geografia",        "¿Cual es el rio mas largo de Sudamerica?",                          "amazonas"));
        insertar(new Pregunta(1, "Literatura",       "¿Que recurso usa Garcia Marquez con 'lluvias de cuatro anos'?",     "hiperbole"));
        insertar(new Pregunta(1, "Deportes",         "¿Que pais gano la Copa Mundial de Futbol 2022?",                   "argentina"));
        insertar(new Pregunta(1, "Entretenimiento",  "¿Que pelicula Disney tiene a una sirena llamada Ariel?",            "la sirenita"));

        // nivel 2
        insertar(new Pregunta(2, "Matematicas",     "Si f(x) = 2x^2 - 3x + 1, ¿cual es f(-2)?",                         "15"));
        insertar(new Pregunta(2, "Geografia",        "¿Que capital africana esta en la orilla del rio Nilo?",             "el cairo"));
        insertar(new Pregunta(2, "Literatura",       "En Rayuela, ¿que estructura narrativa usa Cortazar?",               "no lineal"));
        insertar(new Pregunta(2, "Deportes",         "¿Que tenista tiene mas titulos de Grand Slam masculino?",           "novak djokovic"));
        insertar(new Pregunta(2, "Entretenimiento",  "¿Que director dirigio Titanic 1997?",                               "james cameron"));
        insertar(new Pregunta(2, "Matematicas",      "Resuelve: x/3 + x/4 = 14. ¿Cuanto es x?",                         "24"));
        insertar(new Pregunta(2, "Geografia",        "¿Que pais tiene la mayor poblacion del mundo en 2024?",             "india"));
        insertar(new Pregunta(2, "Literatura",       "¿Que figura retorica es 'sus cabellos eran de oro'?",               "metafora"));
        insertar(new Pregunta(2, "Deportes",         "¿En que anio se jugo la primera Copa Mundial de Futbol?",           "1930"));
        insertar(new Pregunta(2, "Entretenimiento",  "¿Que serie de Netflix trata sobre un juego coreano de muerte?",    "squid game"));

        // nivel 3
        insertar(new Pregunta(3, "Matematicas",     "Si log2(x) = 5, ¿cual es el valor de x?",                           "32"));
        insertar(new Pregunta(3, "Geografia",        "¿Que pais tiene la mayor linea costera del mundo?",                 "canada"));
        insertar(new Pregunta(3, "Literatura",       "En El amor en los tiempos del colera, ¿cuantos anios espera Florentino?", "51"));
        insertar(new Pregunta(3, "Deportes",         "¿Que atleta tiene el record mundial de 100m con 9.58 segundos?",   "usain bolt"));
        insertar(new Pregunta(3, "Entretenimiento",  "¿Que pelicula gano el Oscar a Mejor Pelicula en 2024?",             "oppenheimer"));
        insertar(new Pregunta(3, "Matematicas",      "Resuelve: 2x+3y=16 y x-y=2. ¿Cuanto es x?",                       "4"));
        insertar(new Pregunta(3, "Geografia",        "¿Que capital esta dividida por el rio Danubio?",                   "budapest"));
        insertar(new Pregunta(3, "Literatura",       "¿Que autor escribio El laberinto de la soledad?",                  "octavio paz"));
        insertar(new Pregunta(3, "Deportes",         "¿Que pais gano la Copa America 2024?",                             "argentina"));
        insertar(new Pregunta(3, "Entretenimiento",  "¿Que director dirigio Pulp Fiction?",                              "quentin tarantino"));
    }
}