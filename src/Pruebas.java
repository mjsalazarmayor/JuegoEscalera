import estructuras.ArbolBST;
import estructuras.Cola;
import estructuras.Grafo;
import estructuras.ListaDoble;
import estructuras.Pila;
import estructuras.TablaHash;
import logica.Reglas;
import logica.Tablero;
import logica.Turnos;
import modelo.Casilla;
import modelo.Jugador;
import modelo.Pregunta;
import ordenamiento.MergeSort;

/**
 * Pruebas.java
 * ------------
 * Suite de pruebas unitarias para todas las clases del proyecto.
 * Cada prueba verifica el correcto funcionamiento de los componentes.
 * Imprime "OK" si pasa o "ERROR" si falla.
 * 
 * Uso: Ejecutar main() para correr todas las pruebas automáticamente.
 */
public class Pruebas {

    private static int total   = 0;    // Total de pruebas ejecutadas
    private static int pasadas = 0;    // Cantidad de pruebas exitosas

    public static void main(String[] args) {
        System.out.println("========== PRUEBAS DEL PROYECTO ==========\n");

        // Ejecutar todas las pruebas
        probarNodo();
        probarListaDoble();
        probarCola();
        probarPila();
        probarTablaHash();
        probarArbolBST();
        probarGrafo();
        probarReglas();
        probarTablero();
        probarTurnos();
        probarMergeSort();
        probarValidaciones();

        // Mostrar resumen
        System.out.println("\n==========================================");
        System.out.println("Resultado: " + pasadas + "/" + total + " pruebas pasadas");
        if (pasadas == total) {
            System.out.println("Todo OK!");
        } else {
            System.out.println("Hay " + (total - pasadas) + " prueba(s) fallando.");
        }
    }

    // ============================================================
    // PRUEBAS DE ESTRUCTURAS DE DATOS
    // ============================================================

    /**
     * Prueba la clase Nodo (estructura base de lista doble)
     * Verifica: constructor, getters, setters
     */
    private static void probarNodo() {
        System.out.println("--- Nodo ---");
        estructuras.Nodo<Integer> nodo = new estructuras.Nodo<>(10);
        verificar("Nodo guarda dato correctamente",    nodo.getDato() == 10);
        verificar("Nodo sig es null al crear",         nodo.getSig() == null);
        verificar("Nodo ant es null al crear",         nodo.getAnt() == null);

        nodo.setDato(20);
        verificar("Nodo actualiza dato con setDato",   nodo.getDato() == 20);
    }

    /**
     * Prueba la ListaDoble (estructura base para Cola y Pila)
     * Verifica: inserción al inicio/final, eliminación, búsqueda por índice
     */
    private static void probarListaDoble() {
        System.out.println("\n--- ListaDoble ---");
        ListaDoble<Integer> lista = new ListaDoble<>();

        verificar("Lista vacia al crear",              lista.esVacia());
        verificar("Tamanio 0 al crear",                lista.getTamanio() == 0);

        // Insertar al final
        lista.insertarAlFinal(1);
        lista.insertarAlFinal(2);
        lista.insertarAlFinal(3);
        verificar("Lista no vacia despues de insertar", !lista.esVacia());
        verificar("Tamanio correcto despues de insertar", lista.getTamanio() == 3);

        // Búsqueda por índice
        verificar("buscarPorIndice(0) devuelve 1",     lista.buscarPorIndice(0) == 1);
        verificar("buscarPorIndice(2) devuelve 3",     lista.buscarPorIndice(2) == 3);
        verificar("buscarPorIndice(-1) devuelve null", lista.buscarPorIndice(-1) == null);
        verificar("buscarPorIndice(99) devuelve null", lista.buscarPorIndice(99) == null);

        // Insertar al principio
        lista.insertarAlPrincipio(0);
        verificar("insertarAlPrincipio agrega al inicio", lista.buscarPorIndice(0) == 0);
        verificar("Tamanio aumenta con insertarAlPrincipio", lista.getTamanio() == 4);

        // Eliminar primero
        int primero = lista.eliminarPrimero();
        verificar("eliminarPrimero devuelve el primero", primero == 0);
        verificar("Tamanio baja despues de eliminar",   lista.getTamanio() == 3);

        // Eliminar último
        int ultimo = lista.eliminarUltimo();
        verificar("eliminarUltimo devuelve el ultimo",  ultimo == 3);

        // Casos borde: lista vacía
        ListaDoble<Integer> vacia = new ListaDoble<>();
        verificar("eliminarPrimero en vacia devuelve null", vacia.eliminarPrimero() == null);
        verificar("eliminarUltimo en vacia devuelve null",  vacia.eliminarUltimo() == null);
    }

    /**
     * Prueba la Cola (FIFO - First In First Out)
     * Verifica: encolar, desencolar, verPrimero
     */
    private static void probarCola() {
        System.out.println("\n--- Cola ---");
        Cola<String> cola = new Cola<>();

        verificar("Cola vacia al crear",               cola.esVacia());

        // Encolar elementos
        cola.encolar("A");
        cola.encolar("B");
        cola.encolar("C");

        verificar("verPrimero devuelve A (el primero en entrar)", cola.verPrimero().equals("A"));
        verificar("Tamanio cola es 3",                 cola.getTamanio() == 3);

        // Desencolar (FIFO)
        String primero = cola.desencolar();
        verificar("desencolar devuelve A (FIFO)",      primero.equals("A"));
        verificar("Siguiente es B despues de desencolar", cola.verPrimero().equals("B"));
        verificar("Tamanio baja a 2",                  cola.getTamanio() == 2);

        // Casos borde: cola vacía
        Cola<String> vacia = new Cola<>();
        verificar("desencolar en vacia devuelve null", vacia.desencolar() == null);
        verificar("verPrimero en vacia devuelve null", vacia.verPrimero() == null);
    }

    /**
     * Prueba la Pila (LIFO - Last In First Out)
     * Verifica: apilar, desapilar, verTope
     */
    private static void probarPila() {
        System.out.println("\n--- Pila ---");
        Pila<String> pila = new Pila<>();

        verificar("Pila vacia al crear",               pila.esVacia());

        // Apilar elementos
        pila.apilar("A");
        pila.apilar("B");
        pila.apilar("C");

        verificar("verTope devuelve C (el último en entrar)", pila.verTope().equals("C"));
        verificar("Tamanio pila es 3",                 pila.getTamanio() == 3);

        // Desapilar (LIFO)
        String tope = pila.desapilar();
        verificar("desapilar devuelve C (LIFO)",       tope.equals("C"));
        verificar("Nuevo tope es B",                   pila.verTope().equals("B"));

        // Casos borde: pila vacía
        Pila<String> vacia = new Pila<>();
        verificar("desapilar en vacia devuelve null",  vacia.desapilar() == null);
        verificar("verTope en vacia devuelve null",    vacia.verTope() == null);
    }

    /**
     * Prueba la TablaHash (para almacenar jugadores)
     * Verifica: inserción, búsqueda, referencia de objetos
     */
    private static void probarTablaHash() {
        System.out.println("\n--- TablaHash ---");
        TablaHash tabla = new TablaHash(10);

        Jugador juan  = new Jugador("Juan");
        Jugador maria = new Jugador("Maria");

        tabla.insertar(juan);
        tabla.insertar(maria);

        // Búsqueda por nombre
        verificar("buscar Juan lo encuentra",          tabla.buscar("Juan") != null);
        verificar("buscar Maria lo encuentra",         tabla.buscar("Maria") != null);
        verificar("buscar nombre inexistente null",    tabla.buscar("Pedro") == null);

        // Verificar que es el mismo objeto (referencia)
        juan.setPosicion(15);
        verificar("Cambio en objeto se refleja en tabla", tabla.buscar("Juan").getPosicion() == 15);
    }

    /**
     * Prueba el Árbol BST (para almacenar preguntas)
     * Verifica: búsqueda por nivel y categoría, validación de respuestas
     */
    private static void probarArbolBST() {
        System.out.println("\n--- ArbolBST ---");
        ArbolBST arbol = new ArbolBST();

        // El árbol ya tiene preguntas precargadas en el constructor
        Pregunta p1 = arbol.buscar(1, "matematicas");
        Pregunta p2 = arbol.buscar(2, "geografia");
        Pregunta p3 = arbol.buscar(3, "deportes");

        verificar("buscar nivel 1 matematicas no null",  p1 != null);
        verificar("buscar nivel 2 geografia no null",    p2 != null);
        verificar("buscar nivel 3 deportes no null",     p3 != null);
        verificar("buscar categoria inexistente null",   arbol.buscar(1, "musica") == null);

        // Validación de respuestas (insensible a mayúsculas/espacios)
        Pregunta pregunta = new Pregunta(1, "Test", "¿Prueba?", "respuesta");
        verificar("validar respuesta correcta",          arbol.validar(pregunta, "respuesta"));
        verificar("validar ignorando mayusculas",        arbol.validar(pregunta, "RESPUESTA"));
        verificar("validar ignorando espacios",          arbol.validar(pregunta, " respuesta "));
        verificar("validar respuesta incorrecta false",  !arbol.validar(pregunta, "mal"));
    }

    /**
     * Prueba el Grafo (para conexiones escalera/serpiente)
     * Verifica: agregar aristas, obtener destino, casos borde
     */
    private static void probarGrafo() {
        System.out.println("\n--- Grafo ---");
        Grafo grafo = new Grafo(20);  // 20 casillas

        // Agregar conexiones
        grafo.agregarArista(5, 15);   // Escalera: 5 -> 15
        grafo.agregarArista(12, 3);   // Serpiente: 12 -> 3

        verificar("casilla 5 tiene conexion",           grafo.tieneConexion(5));
        verificar("casilla 12 tiene conexion",          grafo.tieneConexion(12));
        verificar("casilla 1 no tiene conexion",        !grafo.tieneConexion(1));
        verificar("obtenerDestino(5) es 15",            grafo.obtenerDestino(5) == 15);
        verificar("obtenerDestino(12) es 3",            grafo.obtenerDestino(12) == 3);
        verificar("obtenerDestino sin conexion es -1",  grafo.obtenerDestino(1) == -1);

        // Validaciones de borde (no deben lanzar excepciones)
        grafo.agregarArista(0, 5);   // origen inválido (casilla 0)
        grafo.agregarArista(5, 99);  // destino inválido
        verificar("Casilla 0 no tiene conexion",        !grafo.tieneConexion(0));
    }

    // ============================================================
    // PRUEBAS DE LÓGICA DEL JUEGO
    // ============================================================

    /**
     * Prueba la clase Reglas (lógica básica del juego)
     * Verifica: movimiento, límites, niveles, condición de victoria
     */
    private static void probarReglas() {
        System.out.println("\n--- Reglas ---");

        // Movimiento válido
        verificar("puedeMover(45,5,50) true",           Reglas.puedeMover(45, 5, 50));
        verificar("puedeMover(46,5,50) false (necesita exacto)", !Reglas.puedeMover(46, 5, 50));
        verificar("puedeMover(50,1,50) false (ya ganó)", !Reglas.puedeMover(50, 1, 50));
        
        // Cálculo de nueva posición
        verificar("calcularNuevaPos(10,4) es 14",       Reglas.calcularNuevaPos(10, 4) == 14);
        
        // Condición de victoria
        verificar("esGanador(50,50) true",              Reglas.esGanador(50, 50));
        verificar("esGanador(49,50) false",             !Reglas.esGanador(49, 50));
        
        // Niveles (divide tablero en 3 tercios)
        verificar("calcularNivel posicion 1 es 1",      Reglas.calcularNivel(1, 50) == 1);
        verificar("calcularNivel posicion 25 es 2",     Reglas.calcularNivel(25, 50) == 2);
        verificar("calcularNivel posicion 40 es 3",     Reglas.calcularNivel(40, 50) == 3);
    }

    /**
     * Prueba la clase Tablero (configuración del juego)
     * Verifica: cantidad de casillas, tipos, distribución especial
     */
    private static void probarTablero() {
        System.out.println("\n--- Tablero ---");
        Tablero tablero = new Tablero();

        // Dimensiones básicas
        verificar("Tablero tiene 50 casillas",          tablero.getTotalCasillas() == 50);
        verificar("Casilla 1 existe",                   tablero.buscarPorNumero(1) != null);
        verificar("Casilla 50 existe",                  tablero.buscarPorNumero(50) != null);
        verificar("Casilla 0 no existe",                tablero.buscarPorNumero(0) == null);
        verificar("Casilla 99 no existe",               tablero.buscarPorNumero(99) == null);
        
        // Tipos de casillas
        verificar("Casilla 1 es NORMAL",                tablero.buscarPorNumero(1).getTipo().equals("NORMAL"));
        verificar("Casilla 50 es NORMAL",               tablero.buscarPorNumero(50).getTipo().equals("NORMAL"));
        verificar("Grafo no es null",                   tablero.getGrafo() != null);

        // Contar cantidad de cada tipo
        int escaleras = 0, serpientes = 0, retos = 0;
        for (int i = 1; i <= 50; i++) {
            String tipo = tablero.buscarPorNumero(i).getTipo();
            switch (tipo) {
                case "ESCALERA"  -> escaleras++;
                case "SERPIENTE" -> serpientes++;
                case "RETO"      -> retos++;
            }
        }
        verificar("Hay 5 escaleras",   escaleras == 5);
        verificar("Hay 4 serpientes",  serpientes == 4);
        verificar("Hay 5 retos",       retos == 5);
    }

    /**
     * Prueba la clase Turnos (gestión circular de jugadores)
     * Verifica: encolar, obtener actual, avanzar, rotación
     */
    private static void probarTurnos() {
        System.out.println("\n--- Turnos ---");
        Turnos turnos = new Turnos();

        verificar("Sin jugadores hayJugadores false",   !turnos.hayJugadores());

        // Agregar jugadores
        Jugador j1 = new Jugador("Ana");
        Jugador j2 = new Jugador("Luis");
        Jugador j3 = new Jugador("Sara");

        turnos.encolarJugador(j1);
        turnos.encolarJugador(j2);
        turnos.encolarJugador(j3);

        verificar("hayJugadores true despues de agregar", turnos.hayJugadores());
        verificar("cantidadJugadores es 3",             turnos.cantidadJugadores() == 3);
        verificar("jugadorActual es Ana (primero en entrar)", turnos.jugadorActual().getNombreUsuario().equals("Ana"));

        // Avanzar turno (circular)
        Jugador siguiente = turnos.siguienteTurno();
        verificar("siguienteTurno devuelve Luis (siguiente)", siguiente.getNombreUsuario().equals("Luis"));
        verificar("jugadorActual ahora es Luis",        turnos.jugadorActual().getNombreUsuario().equals("Luis"));

        // Rotación completa
        turnos.siguienteTurno(); // Luis -> Sara
        turnos.siguienteTurno(); // Sara -> Ana
        verificar("rota de vuelta a Ana (circular)",    turnos.jugadorActual().getNombreUsuario().equals("Ana"));
    }

    // ============================================================
    // PRUEBAS DE ORDENAMIENTO
    // ============================================================

    /**
     * Prueba el MergeSort (ordenamiento de jugadores)
     * Verifica: orden por aciertos y posición
     */
    private static void probarMergeSort() {
        System.out.println("\n--- MergeSort ---");
        ListaDoble<Jugador> lista = new ListaDoble<>();

        // Crear jugadores con diferentes aciertos y posiciones
        Jugador j1 = new Jugador("A"); j1.setAciertos(1); j1.setPosicion(30);
        Jugador j2 = new Jugador("B"); j2.setAciertos(3); j2.setPosicion(45);
        Jugador j3 = new Jugador("C"); j3.setAciertos(2); j3.setPosicion(40);
        Jugador j4 = new Jugador("D"); j4.setAciertos(3); j4.setPosicion(50);

        lista.insertarAlFinal(j1);
        lista.insertarAlFinal(j2);
        lista.insertarAlFinal(j3);
        lista.insertarAlFinal(j4);

        // Ordenar (por aciertos, desempata por posición)
        ListaDoble<Jugador> ordenada = MergeSort.ordenar(lista);

        verificar("Primero tiene mas aciertos",         ordenada.buscarPorIndice(0).getAciertos() >= ordenada.buscarPorIndice(1).getAciertos());
        verificar("Desempate por posicion: D antes que B", ordenada.buscarPorIndice(0).getNombreUsuario().equals("D"));
        verificar("Ultimo tiene menos aciertos",        ordenada.buscarPorIndice(3).getAciertos() <= ordenada.buscarPorIndice(2).getAciertos());

        // Caso borde: lista de un solo elemento
        ListaDoble<Jugador> uno = new ListaDoble<>();
        uno.insertarAlFinal(j1);
        ListaDoble<Jugador> ordUno = MergeSort.ordenar(uno);
        verificar("Lista de 1 elemento se ordena bien", ordUno.getTamanio() == 1);
    }

    // ============================================================
    // PRUEBAS DE VALIDACIONES Y CASOS BORDE
    // ============================================================

    /**
     * Prueba casos extremos y validaciones de todas las clases
     * Verifica que el sistema maneje correctamente valores inválidos
     */
    private static void probarValidaciones() {
        System.out.println("\n--- Validaciones de entrada ---");

        // Jugador con nombre vacío
        Jugador vacio = new Jugador("");
        verificar("Jugador con nombre vacio no lanza excepcion", vacio.getNombreUsuario().equals(""));

        // Casilla con número negativo
        Casilla c = new Casilla(-1, "NORMAL", -1);
        verificar("Casilla con numero negativo no lanza excepcion", c.getNumero() == -1);

        // Grafo con casillas fuera de rango (debe ignorarlas)
        Grafo g = new Grafo(10);
        g.agregarArista(-1, 5);   // Origen inválido
        g.agregarArista(5, 99);   // Destino inválido
        verificar("Grafo ignora aristas invalidas",     !g.tieneConexion(-1));

        // TablaHash buscar null (clave inexistente)
        TablaHash t = new TablaHash(5);
        verificar("TablaHash buscar null devuelve null", t.buscar("noexiste") == null);

        // ListaDoble buscar índice fuera de rango
        ListaDoble<Integer> l = new ListaDoble<>();
        l.insertarAlFinal(1);
        verificar("buscarPorIndice fuera de rango null", l.buscarPorIndice(5) == null);
        verificar("buscarPorIndice negativo null",       l.buscarPorIndice(-1) == null);

        // Reglas: dado inválido (0)
        verificar("puedeMover dado 0 en pos 50 false",  !Reglas.puedeMover(50, 0, 50));
        verificar("puedeMover desde pos 1 dado 6",      Reglas.puedeMover(1, 6, 50));
    }

    // ============================================================
    // MÉTODO HELPER
    // ============================================================

    /**
     * Verifica una condición y muestra el resultado
     * @param descripcion Texto descriptivo de la prueba
     * @param condicion Resultado booleano a verificar
     */
    private static void verificar(String descripcion, boolean condicion) {
        total++;
        if (condicion) {
            pasadas++;
            System.out.println("  OK  - " + descripcion);
        } else {
            System.out.println("  ERROR - " + descripcion);
        }
    }
}