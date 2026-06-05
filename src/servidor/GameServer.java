package servidor;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

import logica.Reglas;
import logica.Tablero;
import logica.Turnos;
import modelo.Casilla;
import modelo.Jugador;
import modelo.Pregunta;
import estructuras.ArbolBST;
import estructuras.Pila;
import estructuras.TablaHash;

/**
 * Servidor HTTP del juego de Serpientes y Escaleras con preguntas
 * Maneja la lógica del juego y la comunicación con el frontend
 */
public class GameServer {

    private static final int PORT = 8080;  // Puerto donde corre el servidor

    // ============ ESTADO GLOBAL DEL JUEGO ============
    private static Tablero      tablero;           // Tablero con casillas especiales
    private static Turnos       turnos;            // Gestor de turnos circular
    private static TablaHash    tabla;             // Hash table para estadísticas
    private static Pila<String> historial;         // Pila con últimos eventos (LIFO)
    private static ArbolBST     arbol = new ArbolBST();  // Árbol binario de preguntas
    private static Random       random  = new Random();   // Generador aleatorio
    private static boolean      juegoIniciado      = false;   // Flag de partida activa
    private static String       ganador            = null;    // Nombre del ganador
    private static Pregunta     preguntaActual     = null;    // Pregunta activa del reto
    private static boolean      esperandoRespuesta = false;   // Esperando respuesta del jugador
    private static boolean      esperandoCategoria = false;   // Esperando que elija categoría
    private static int          nivelRetoActual    = 1;       // Nivel de dificultad del reto
    private static String       jugadorEnReto      = null;    // Jugador enfrentando reto
    private static Set<String>  nombresHumanos     = new HashSet<>();  // Nombres de humanos (no bots)

    // ============ ÚLTIMO MOVIMIENTO (para UI) ============
    private static int    ultimoDado       = 0;        // Último valor del dado
    private static String ultimoJugador    = null;     // Último jugador que tiró
    private static int    ultimoDesde      = 0;        // Posición inicial del movimiento
    private static int    ultimoHasta      = 0;        // Posición final
    private static String ultimoEvento     = "normal"; // Tipo: normal, escalera, serpiente

    // Categorías disponibles para preguntas
    private static final String[] CATEGORIAS = {
        "Matematicas", "Geografia", "Literatura", "Deportes", "Entretenimiento"
    };

    public static void main(String[] args) throws Exception {
        // Crear servidor HTTP en el puerto especificado
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Registrar endpoints REST
        server.createContext("/api/iniciar",          new IniciarHandler());
        server.createContext("/api/estado",           new EstadoHandler());
        server.createContext("/api/tirar",            new TirarHandler());
        server.createContext("/api/elegir-categoria", new ElegirCategoriaHandler());
        server.createContext("/api/responder",        new ResponderHandler());
        server.createContext("/api/historial",        new HistorialHandler());
        server.createContext("/api/ranking",          new RankingHandler());
        server.createContext("/api/reiniciar",        new ReiniciarHandler());
        server.createContext("/",                     new StaticHandler());  // Archivos estáticos

        server.setExecutor(null);  // Usar executor por defecto
        server.start();
        System.out.println("🎲 Servidor iniciado en http://localhost:" + PORT);
        System.out.println("   Abre tu navegador en esa dirección para jugar.");
    }

    // ============================================================
    // HANDLERS - Procesan cada endpoint
    // ============================================================

    /**
     * Handler: Iniciar una nueva partida (POST /api/iniciar)
     * Recibe lista de jugadores y cantidad de bots
     */
    static class IniciarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { 
                ex.sendResponseHeaders(204, -1); 
                return; 
            }
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { 
                sendError(ex, 405, "Method Not Allowed"); 
                return; 
            }

            // Leer cuerpo de la petición JSON
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            // Extraer nombres de jugadores
            List<String> nombresJugadores = extraerArrayStrings(body, "jugadores");
            if (nombresJugadores.isEmpty()) {
                String nombre = extraerCampo(body, "nombre");
                if (nombre != null && !nombre.isBlank()) nombresJugadores.add(nombre.trim());
            }

            // Validaciones
            if (nombresJugadores.isEmpty()) { 
                sendError(ex, 400, "Se requiere al menos un jugador"); 
                return; 
            }
            if (nombresJugadores.size() > 4) { 
                sendError(ex, 400, "Máximo 4 jugadores humanos"); 
                return; 
            }

            // Extraer cantidad de bots (si no viene, calcular para llegar a 5)
            int bots = 0;
            String botsStr = extraerCampo(body, "bots");
            if (botsStr != null) {
                try { 
                    bots = Integer.parseInt(botsStr.trim()); 
                } catch (NumberFormatException ignored) {}
            }
            if (botsStr == null) {
                bots = Math.max(0, 5 - nombresJugadores.size());
            }

            // ===== REINICIALIZAR TODO EL ESTADO =====
            tablero            = new Tablero();          // Crear tablero con escaleras/serpientes
            turnos             = new Turnos();           // Nueva cola de turnos
            tabla              = new TablaHash(10);      // Nueva tabla hash
            historial          = new Pila<>();           // Nuevo historial vacío
            arbol              = new ArbolBST();         // Nuevo árbol de preguntas
            ganador            = null;
            preguntaActual     = null;
            esperandoRespuesta = false;
            esperandoCategoria = false;
            nivelRetoActual    = 1;
            jugadorEnReto      = null;
            nombresHumanos     = new HashSet<>();

            System.out.println("📚 Preguntas cargadas en el árbol");

            // Registrar jugadores humanos
            for (String n : nombresJugadores) {
                Jugador j = new Jugador(n.trim());
                turnos.encolarJugador(j);
                tabla.insertar(j);
                nombresHumanos.add(n.trim());
            }

            // Agregar bots automáticos
            String[] botNames = { "Bot-Julian", "Bot-Eddie", "Bot-Daniela", "Bot-Sebastian" };
            for (int i = 0; i < bots && i < botNames.length; i++) {
                Jugador bot = new Jugador(botNames[i]);
                turnos.encolarJugador(bot);
                tabla.insertar(bot);
            }

            juegoIniciado = true;
            sendJson(ex, 200, estadoJson());
        }
    }

    /**
     * Handler: Obtener estado actual del juego (GET /api/estado)
     */
    static class EstadoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { 
                ex.sendResponseHeaders(204, -1); 
                return; 
            }
            if (!juegoIniciado) { 
                sendError(ex, 400, "Juego no iniciado"); 
                return; 
            }
            sendJson(ex, 200, estadoJson());
        }
    }

    /**
     * Handler: Tirar el dado (POST /api/tirar)
     * Procesa el turno del jugador actual
     */
    static class TirarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { 
                ex.sendResponseHeaders(204, -1); 
                return; 
            }
            if (!juegoIniciado || ganador != null) { 
                sendError(ex, 400, "Juego no activo"); 
                return; 
            }
            if (esperandoRespuesta) { 
                sendError(ex, 400, "Hay un reto pendiente"); 
                return; 
            }
            if (esperandoCategoria) { 
                sendError(ex, 400, "Esperando elección de categoría"); 
                return; 
            }

            Jugador jugador = turnos.jugadorActual();
            procesarTurnoJugador(jugador, esBot(jugador));  // Procesar el turno

            sendJson(ex, 200, estadoJson());
        }
    }

    /**
     * Handler: Elegir categoría para un reto (POST /api/elegir-categoria)
     * Solo para jugadores humanos
     */
    static class ElegirCategoriaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { 
                ex.sendResponseHeaders(204, -1); 
                return; 
            }
            if (!esperandoCategoria) { 
                sendError(ex, 400, "No hay reto esperando categoría"); 
                return; 
            }

            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String cat = extraerCampo(body, "categoria");
            if (cat == null || cat.isBlank()) { 
                sendError(ex, 400, "Falta el campo 'categoria'"); 
                return; 
            }

            // Buscar pregunta en el árbol por nivel y categoría
            Pregunta p = arbol.buscar(nivelRetoActual, cat.trim());
            if (p == null) {
                // No hay pregunta disponible, saltar turno
                esperandoCategoria = false;
                jugadorEnReto = null;
                turnos.siguienteTurno();
                historial.apilar("❓ Sin pregunta de " + cat + " para nivel " + nivelRetoActual + ". Turno saltado.");
                sendJson(ex, 200, estadoJson());
                return;
            }

            // Activar reto con la pregunta seleccionada
            preguntaActual     = p;
            esperandoRespuesta = true;
            esperandoCategoria = false;
            historial.apilar("❓ RETO para " + jugadorEnReto + ": [" + p.getCategoria() + " Niv." + p.getDificultad() + "]");
            sendJson(ex, 200, estadoJson());
        }
    }

    /**
     * Handler: Responder pregunta del reto (POST /api/responder)
     */
    static class ResponderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { 
                ex.sendResponseHeaders(204, -1); 
                return; 
            }
            if (!esperandoRespuesta) { 
                sendError(ex, 400, "No hay reto activo"); 
                return; 
            }

            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String resp = extraerCampo(body, "respuesta");
            Jugador jugador = buscarJugadorPorNombre(jugadorEnReto);

            // Validar respuesta (insensible a mayúsculas/minúsculas)
            boolean correcto = arbol.validar(preguntaActual, resp != null ? resp : "");
            String evento;

            if (correcto) {
                jugador.setAciertos(jugador.getAciertos() + 1);
                evento = "correcto";
                // ✅ REGLA CLAVE: Acertar = repetir turno (NO avanzar)
                historial.apilar("✅ " + jugador.getNombreUsuario() + " acertó! Tendrá otro turno.");
                System.out.println("🔁 " + jugador.getNombreUsuario() + " ACERTÓ - REPITE TURNO");
            } else {
                jugador.setFallos(jugador.getFallos() + 1);
                evento = "incorrecto";
                // ❌ REGLA CLAVE: Fallar = pierde turno
                turnos.siguienteTurno();
                historial.apilar("❌ " + jugador.getNombreUsuario() + " falló. Pierde turno.");
                System.out.println("➡️ " + jugador.getNombreUsuario() + " FALLÓ - Avanza turno");
            }

            // Limpiar estado del reto
            esperandoRespuesta = false;
            String respuestaCorrecta = preguntaActual.getRespuesta();
            jugadorEnReto = null;
            preguntaActual = null;

            // Verificar si el jugador ganó al caer en la meta
            if (Reglas.esGanador(jugador.getPosicion(), tablero.getTotalCasillas())) {
                ganador = jugador.getNombreUsuario();
            }

            // Enviar respuesta incluyendo la respuesta correcta
            String json = "{ \"evento\": \"" + evento + "\", \"respuestaCorrecta\": " + jsonStr(respuestaCorrecta) + ", " + estadoJsonInner() + " }";
            sendJson(ex, 200, json);
        }
    }

    /**
     * Handler: Obtener historial de movimientos (GET /api/historial)
     */
    static class HistorialHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (!juegoIniciado) { 
                sendError(ex, 400, "Juego no iniciado"); 
                return; 
            }
            sendJson(ex, 200, "{ \"historial\": " + historialJson() + " }");
        }
    }

    /**
     * Handler: Obtener ranking de jugadores (GET /api/ranking)
     */
    static class RankingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (!juegoIniciado) { 
                sendError(ex, 400, "Juego no iniciado"); 
                return; 
            }
            sendJson(ex, 200, "{ \"ranking\": " + rankingJson() + " }");
        }
    }

    /**
     * Handler: Reiniciar el juego (POST /api/reiniciar)
     */
    static class ReiniciarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            juegoIniciado = false;
            ganador = null;
            esperandoRespuesta = false;
            esperandoCategoria = false;
            jugadorEnReto = null;
            preguntaActual = null;
            ultimoDado = 0;
            ultimoJugador = null;
            ultimoDesde = 0;
            ultimoHasta = 0;
            ultimoEvento = "normal";
            sendJson(ex, 200, "{ \"ok\": true }");
        }
    }

    /**
     * Handler: Servir archivos estáticos (HTML, CSS, JS)
     */
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            // Buscar archivo en carpeta "web"
            File base = new File(System.getProperty("user.dir"));
            File file = new File(base, "web" + path);
            
            if (!file.exists() || !file.isFile()) { 
                sendError(ex, 404, "Not Found: " + path); 
                return; 
            }
            
            String mime = guessMime(path);
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().set("Content-Type", mime);
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            ex.sendResponseHeaders(200, bytes.length);
            ex.getResponseBody().write(bytes);
            ex.getResponseBody().close();
        }
        
        private String guessMime(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css"))  return "text/css; charset=utf-8";
            if (path.endsWith(".js"))   return "application/javascript; charset=utf-8";
            return "application/octet-stream";
        }
    }

    // ============================================================
    // LÓGICA DEL JUEGO
    // ============================================================

    /**
     * Determina si un jugador es bot (no es humano)
     */
    private static boolean esBot(Jugador j) {
        return !nombresHumanos.contains(j.getNombreUsuario());
    }

    /**
     * Procesa un turno completo de un jugador
     * @param jugador Jugador que juega el turno
     * @param bot true si es bot (comportamiento automático)
     */
    private static void procesarTurnoJugador(Jugador jugador, boolean bot) {
        // 1. Tirar dado
        int dado = random.nextInt(6) + 1;
        historial.apilar(jugador.getNombreUsuario() + " sacó " + dado);

        // Guardar datos del último movimiento
        ultimoDado    = dado;
        ultimoJugador = jugador.getNombreUsuario();
        ultimoEvento  = "normal";

        // 2. Validar si puede moverse (necesita caer exacto en la meta)
        if (!Reglas.puedeMover(jugador.getPosicion(), dado, tablero.getTotalCasillas())) {
            historial.apilar(jugador.getNombreUsuario() + " no puede mover (necesita exacto)");
            ultimoDesde = jugador.getPosicion();
            ultimoHasta = jugador.getPosicion();
            turnos.siguienteTurno();  // Pierde turno
            return;
        }

        // 3. Mover jugador
        ultimoDesde = jugador.getPosicion();
        int nuevaPos = Reglas.calcularNuevaPos(jugador.getPosicion(), dado);
        jugador.setPosicion(nuevaPos);
        ultimoHasta = nuevaPos;
        historial.apilar(jugador.getNombreUsuario() + " avanzó a casilla " + nuevaPos);

        // 4. Procesar casilla especial (Escalera, Serpiente o Reto)
        Casilla casilla = tablero.buscarPorNumero(nuevaPos);
        boolean turnoExtra = procesarCasilla(jugador, casilla, bot);

        // 5. Verificar ganador
        if (Reglas.esGanador(jugador.getPosicion(), tablero.getTotalCasillas())) {
            ganador = jugador.getNombreUsuario();
            return;
        }

        // 6. Determinar siguiente turno
        if (turnoExtra) {
            System.out.println("🔁 Turno extra para: " + jugador.getNombreUsuario() + " - NO se avanza");
        } else if (esperandoRespuesta || esperandoCategoria) {
            System.out.println("⏸️ Esperando respuesta/categoría - NO se avanza turno");
        } else {
            turnos.siguienteTurno();
            System.out.println("➡️ Avanzando turno a: " + turnos.jugadorActual().getNombreUsuario());
        }
    }

    /**
     * Procesa el efecto de una casilla especial
     * @return true si el jugador tiene turno extra (solo por acertar reto)
     */
    private static boolean procesarCasilla(Jugador jugador, Casilla casilla, boolean bot) {
        return switch (casilla.getTipo()) {
            case "ESCALERA" -> {
                // Subir a la casilla destino
                int dest = tablero.getGrafo().obtenerDestino(casilla.getNumero());
                jugador.setPosicion(dest);
                ultimoDesde = casilla.getNumero();
                ultimoHasta = dest;
                ultimoEvento = "escalera";
                historial.apilar("🪜 ¡Escalera! " + jugador.getNombreUsuario() + " sube a " + dest);
                yield false;  // No hay turno extra
            }
            case "SERPIENTE" -> {
                // Bajar a la casilla destino
                int dest = tablero.getGrafo().obtenerDestino(casilla.getNumero());
                jugador.setPosicion(dest);
                ultimoDesde = casilla.getNumero();
                ultimoHasta = dest;
                ultimoEvento = "serpiente";
                historial.apilar("🐍 ¡Serpiente! " + jugador.getNombreUsuario() + " baja a " + dest);
                yield false;  // No hay turno extra
            }
            case "RETO" -> {
                int nivel = Reglas.calcularNivel(jugador.getPosicion(), tablero.getTotalCasillas());
                System.out.println("DEBUG: RETO - Jugador: " + jugador.getNombreUsuario() + ", Nivel: " + nivel + ", Bot: " + bot);

                if (bot) {
                    // === COMPORTAMIENTO BOT ===
                    // Elige categoría aleatoria
                    String cat = CATEGORIAS[random.nextInt(CATEGORIAS.length)];
                    Pregunta p = arbol.buscar(nivel, cat);
                    historial.apilar("🤖 " + jugador.getNombreUsuario() + " eligió categoría: " + cat);

                    if (p == null) {
                        historial.apilar("❓ Reto sin pregunta para " + jugador.getNombreUsuario());
                        yield false;
                    }

                    // Bot responde aleatoriamente (50% probabilidad)
                    boolean acierta = random.nextBoolean();
                    if (acierta) {
                        jugador.setAciertos(jugador.getAciertos() + 1);
                        historial.apilar("🤖 " + jugador.getNombreUsuario() + " [" + cat + "] respondió CORRECTO → turno extra");
                        System.out.println("🔁 BOT acertó - turno extra");
                        yield true;  // Turno extra por acertar
                    } else {
                        jugador.setFallos(jugador.getFallos() + 1);
                        historial.apilar("🤖 " + jugador.getNombreUsuario() + " [" + cat + "] respondió MAL → pierde turno");
                        System.out.println("➡️ BOT falló - pierde turno");
                        yield false;  // Pierde turno
                    }
                } else {
                    // === COMPORTAMIENTO HUMANO ===
                    // Esperar que elija categoría y luego responda
                    nivelRetoActual = nivel;
                    esperandoCategoria = true;
                    jugadorEnReto = jugador.getNombreUsuario();
                    preguntaActual = null;
                    historial.apilar("❓ RETO para " + jugador.getNombreUsuario() + " (nivel " + nivel + ") – elige categoría");
                    System.out.println("❓ Humano en RETO - esperandoCategoria=true");
                    yield false;  // Pausar juego hasta respuesta
                }
            }
            default -> { yield false; }  // Casilla normal
        };
    }

    // ============================================================
    // CONSTRUCCIÓN DE JSON
    // ============================================================

    /**
     * Construye el JSON completo del estado del juego
     */
    private static String estadoJson() {
        return "{ " + estadoJsonInner() + " }";
    }

    /**
     * Parte interna del estado JSON (sin llaves externas)
     */
    private static String estadoJsonInner() {
        StringBuilder sb = new StringBuilder();
        Jugador actual = turnos.jugadorActual();
        
        sb.append("\"ganador\": ").append(ganador != null ? jsonStr(ganador) : "null").append(",");
        sb.append("\"turnoActual\": ").append(jsonStr(actual.getNombreUsuario())).append(",");
        sb.append("\"esBot\": ").append(esBot(actual)).append(",");
        sb.append("\"esperandoRespuesta\": ").append(esperandoRespuesta).append(",");
        sb.append("\"esperandoCategoria\": ").append(esperandoCategoria).append(",");
        
        if (esperandoCategoria) {
            sb.append("\"nivelReto\": ").append(nivelRetoActual).append(",");
        }
        
        if (esperandoRespuesta && preguntaActual != null) {
            sb.append("\"reto\": {");
            sb.append("\"enunciado\": ").append(jsonStr(preguntaActual.getEnunciado())).append(",");
            sb.append("\"categoria\": ").append(jsonStr(preguntaActual.getCategoria())).append(",");
            sb.append("\"dificultad\": ").append(preguntaActual.getDificultad());
            sb.append("},");
        } else {
            sb.append("\"reto\": null,");
        }
        
        sb.append("\"humanos\": [");
        String[] hArr = nombresHumanos.toArray(new String[0]);
        for (int i = 0; i < hArr.length; i++) {
            sb.append(jsonStr(hArr[i]));
            if (i < hArr.length - 1) sb.append(",");
        }
        sb.append("],");
        
        sb.append("\"tablero\": ").append(tableroJson()).append(",");
        sb.append("\"jugadores\": ").append(jugadoresJson());
        sb.append(",\"ultimoMovimiento\": {");
        sb.append("\"dado\":").append(ultimoDado).append(",");
        sb.append("\"jugador\":").append(jsonStr(ultimoJugador != null ? ultimoJugador : "")).append(",");
        sb.append("\"desde\":").append(ultimoDesde).append(",");
        sb.append("\"hasta\":").append(ultimoHasta).append(",");
        sb.append("\"evento\":").append(jsonStr(ultimoEvento));
        sb.append("}");
        
        return sb.toString();
    }

    /**
     * Convierte el tablero a JSON
     */
    private static String tableroJson() {
        StringBuilder sb = new StringBuilder("[");
        int total = tablero.getTotalCasillas();
        for (int i = 1; i <= total; i++) {
            Casilla c = tablero.buscarPorNumero(i);
            int dest = tablero.getGrafo().obtenerDestino(i);
            sb.append("{");
            sb.append("\"n\":").append(c.getNumero()).append(",");
            sb.append("\"tipo\":").append(jsonStr(c.getTipo())).append(",");
            sb.append("\"destino\":").append(dest);
            sb.append("}");
            if (i < total) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convierte la lista de jugadores a JSON
     */
    private static String jugadoresJson() {
        List<Jugador> lista = turnos.getTodosJugadores();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Jugador j = lista.get(i);
            sb.append("{");
            sb.append("\"nombre\":").append(jsonStr(j.getNombreUsuario())).append(",");
            sb.append("\"posicion\":").append(j.getPosicion()).append(",");
            sb.append("\"aciertos\":").append(j.getAciertos()).append(",");
            sb.append("\"fallos\":").append(j.getFallos()).append(",");
            sb.append("\"esBot\":").append(esBot(j));
            sb.append("}");
            if (i < lista.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convierte el historial a JSON (últimos 10 eventos)
     */
    private static String historialJson() {
        // Extraer hasta 10 eventos de la pila
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String s = historial.desapilar();
            if (s == null) break;
            items.add(s);
        }
        // Restaurar los eventos a la pila
        for (int i = items.size() - 1; i >= 0; i--) {
            historial.apilar(items.get(i));
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(jsonStr(items.get(i)));
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convierte el ranking a JSON (ordenado por posición y aciertos)
     */
    private static String rankingJson() {
        List<Jugador> lista = turnos.getTodosJugadores();
        // Ordenar: primero por posición (mayor a menor), luego por aciertos
        lista.sort((a, b) -> {
            int d = b.getPosicion() - a.getPosicion();
            return d != 0 ? d : b.getAciertos() - a.getAciertos();
        });
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Jugador j = lista.get(i);
            sb.append("{");
            sb.append("\"nombre\":").append(jsonStr(j.getNombreUsuario())).append(",");
            sb.append("\"posicion\":").append(j.getPosicion()).append(",");
            sb.append("\"aciertos\":").append(j.getAciertos()).append(",");
            sb.append("\"fallos\":").append(j.getFallos());
            sb.append("}");
            if (i < lista.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Escapa un string para JSON
     */
    private static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    // ============================================================
    // UTILIDADES HTTP
    // ============================================================

    /**
     * Agrega cabeceras CORS para permitir peticiones desde el frontend
     */
    private static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Envía respuesta JSON al cliente
     */
    private static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    /**
     * Envía error en formato JSON
     */
    private static void sendError(HttpExchange ex, int code, String msg) throws IOException {
        sendJson(ex, code, "{ \"error\": " + jsonStr(msg) + " }");
    }

    /**
     * Extrae un campo simple de un JSON manualmente
     */
    private static String extraerCampo(String json, String campo) {
        String key = "\"" + campo + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx + key.length());
        if (colon < 0) return null;
        String rest = json.substring(colon + 1).trim();
        
        // Si no es string entre comillas
        if (!rest.startsWith("\"")) {
            int end = rest.indexOf(',');
            if (end < 0) end = rest.indexOf('}');
            if (end < 0) end = rest.length();
            return rest.substring(0, end).trim();
        }
        
        // Extraer string entre comillas
        int q1 = json.indexOf("\"", colon + 1);
        if (q1 < 0) return null;
        int q2 = json.indexOf("\"", q1 + 1);
        if (q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    /**
     * Extrae un array de strings de un JSON manualmente
     */
    private static List<String> extraerArrayStrings(String json, String campo) {
        List<String> result = new ArrayList<>();
        String key = "\"" + campo + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return result;
        int arrStart = json.indexOf("[", idx);
        if (arrStart < 0) return result;
        int arrEnd = json.indexOf("]", arrStart);
        if (arrEnd < 0) return result;
        
        String arr = json.substring(arrStart + 1, arrEnd);
        int pos = 0;
        while (pos < arr.length()) {
            int q1 = arr.indexOf("\"", pos);
            if (q1 < 0) break;
            int q2 = arr.indexOf("\"", q1 + 1);
            if (q2 < 0) break;
            String val = arr.substring(q1 + 1, q2).trim();
            if (!val.isEmpty()) result.add(val);
            pos = q2 + 1;
        }
        return result;
    }

    /**
     * Busca un jugador por su nombre
     */
    private static Jugador buscarJugadorPorNombre(String nombre) {
        List<Jugador> lista = turnos.getTodosJugadores();
        for (Jugador j : lista) {
            if (j.getNombreUsuario().equals(nombre)) return j;
        }
        return null;
    }
}