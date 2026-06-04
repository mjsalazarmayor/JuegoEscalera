package estructuras;

import modelo.Pregunta;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ArbolBST {

    private Nodo<Pregunta> raiz;

    public ArbolBST() {
        this.raiz = null;
        cargarDesdeArchivo("preguntas.txt");
    }

    // Constructor que acepta ruta personalizada
    public ArbolBST(String rutaArchivo) {
        this.raiz = null;
        cargarDesdeArchivo(rutaArchivo);
    }

    /**
     * Carga preguntas desde un archivo .txt con formato:
     * dificultad|categoria|enunciado|respuesta
     * Las líneas que empiecen con # son comentarios.
     */
    private void cargarDesdeArchivo(String ruta) {
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int cargadas = 0;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                String[] partes = linea.split("\\|", 4);
                if (partes.length != 4) continue;
                try {
                    int dif = Integer.parseInt(partes[0].trim());
                    String cat = partes[1].trim();
                    String enunciado = partes[2].trim();
                    String respuesta = partes[3].trim();
                    insertar(new Pregunta(dif, cat, enunciado, respuesta));
                    cargadas++;
                } catch (NumberFormatException e) {
                    // línea mal formada, se ignora
                }
            }
            System.out.println("✅ Preguntas cargadas desde '" + ruta + "': " + cargadas);
        } catch (IOException e) {
            System.out.println("⚠️  No se encontró '" + ruta + "'. Cargando preguntas por defecto.");
            generarPreguntas();
        }
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

    // Preguntas de respaldo si no existe el archivo
    private void generarPreguntas() {
        insertar(new Pregunta(1, "Matematicas",    "Si 3/4 de un numero es 48, ¿cual es el numero?",                   "64"));
        insertar(new Pregunta(1, "Geografia",       "¿Cual es el rio mas largo de Sudamerica?",                          "amazonas"));
        insertar(new Pregunta(1, "Literatura",      "En Cien anios de soledad, ¿que tema central representa la familia?", "soledad"));
        insertar(new Pregunta(1, "Deportes",        "¿Cuantos jugadores tiene un equipo de futbol en la cancha?",        "11"));
        insertar(new Pregunta(1, "Entretenimiento", "¿Que actor interpreto a Iron Man en el MCU?",                       "robert downey jr"));
        insertar(new Pregunta(2, "Matematicas",    "Si f(x) = 2x^2 - 3x + 1, ¿cual es f(-2)?",                         "15"));
        insertar(new Pregunta(2, "Geografia",       "¿Que capital africana esta en la orilla del rio Nilo?",             "el cairo"));
        insertar(new Pregunta(2, "Literatura",      "En Rayuela, ¿que estructura narrativa usa Cortazar?",               "no lineal"));
        insertar(new Pregunta(2, "Deportes",        "¿En que anio se jugo la primera Copa Mundial de Futbol?",           "1930"));
        insertar(new Pregunta(2, "Entretenimiento", "¿Que director dirigio Titanic 1997?",                               "james cameron"));
        insertar(new Pregunta(3, "Matematicas",    "Si log2(x) = 5, ¿cual es el valor de x?",                           "32"));
        insertar(new Pregunta(3, "Geografia",       "¿Que pais tiene la mayor linea costera del mundo?",                 "canada"));
        insertar(new Pregunta(3, "Literatura",      "¿Que autor escribio El laberinto de la soledad?",                  "octavio paz"));
        insertar(new Pregunta(3, "Deportes",        "¿Que atleta tiene el record mundial de 100m con 9.58 segundos?",   "usain bolt"));
        insertar(new Pregunta(3, "Entretenimiento", "¿Que director dirigio Pulp Fiction?",                              "quentin tarantino"));
    }
}