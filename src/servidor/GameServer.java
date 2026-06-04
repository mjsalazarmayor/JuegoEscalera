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
 * Servidor HTTP embebido que expone el JuegoEscalera como una API REST JSON.
 *
 * Endpoints:
 *   POST /api/iniciar     { "jugadores": ["Ana","Bob"], "bots": 2 }
 *   GET  /api/estado
 *   POST /api/tirar       — procesa SOLO el turno actual (humano o bot)
 *   POST /api/responder   { "respuesta": "42" }
 *   GET  /api/historial
 *   GET  /api/ranking
 *   POST /api/reiniciar
 */
public class GameServer {

    private static final int PORT = 8080;

    // ── Estado global ──────────────────────────────────────────────────
    private static Tablero      tablero;
    private static Turnos       turnos;
    private static TablaHash    tabla;
    private static Pila<String> historial;
    private static ArbolBST     arbol;
    private static Random       random  = new Random();
    private static boolean      juegoIniciado      = false;
    private static String       ganador            = null;
    private static Pregunta     preguntaActual     = null;
    private static boolean      esperandoRespuesta = false;
    private static String       jugadorEnReto      = null;
    private static Set<String>  nombresHumanos     = new HashSet<>();

    // ── Último movimiento (para el frontend) ───────────────────────────
    private static int    ultimoDado       = 0;
    private static String ultimoJugador    = null;
    private static int    ultimoDesde      = 0;
    private static int    ultimoHasta      = 0;
    private static String ultimoEvento     = "normal"; // normal | escalera | serpiente

    private static final String[] CATEGORIAS = {
        "Matematicas", "Geografia", "Literatura", "Deportes", "Entretenimiento"
    };

    // ────────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/iniciar",   new IniciarHandler());
        server.createContext("/api/estado",    new EstadoHandler());
        server.createContext("/api/tirar",     new TirarHandler());
        server.createContext("/api/responder", new ResponderHandler());
        server.createContext("/api/historial", new HistorialHandler());
        server.createContext("/api/ranking",   new RankingHandler());
        server.createContext("/api/reiniciar", new ReiniciarHandler());
        server.createContext("/",              new StaticHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("🎲 Servidor iniciado en http://localhost:" + PORT);
        System.out.println("   Abre tu navegador en esa dirección para jugar.");
    }

    // ════════════════════════════════════════════════════════════════════
    //  Handlers
    // ════════════════════════════════════════════════════════════════════

    /**
     * POST /api/iniciar
     * Body: { "jugadores": ["Ana","Bob"], "bots": 2 }
     * jugadores: 1..4 nombres de humanos
     * bots: número de bots (0 .. 4-cantHumanos)
     */
    static class IniciarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204,-1); return; }
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { sendError(ex, 405, "Method Not Allowed"); return; }

            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            // Parsear array "jugadores"
            List<String> nombresJugadores = extraerArrayStrings(body, "jugadores");
            if (nombresJugadores.isEmpty()) {
                // Compatibilidad: campo "nombre" antiguo
                String nombre = extraerCampo(body, "nombre");
                if (nombre != null && !nombre.isBlank()) nombresJugadores.add(nombre.trim());
            }

            if (nombresJugadores.isEmpty()) { sendError(ex, 400, "Se requiere al menos un jugador"); return; }
            if (nombresJugadores.size() > 4) { sendError(ex, 400, "Máximo 4 jugadores humanos"); return; }

            int bots = 0;
            String botsStr = extraerCampo(body, "bots");
            if (botsStr != null) {
                try { bots = Integer.parseInt(botsStr.trim()); } catch (NumberFormatException ignored) {}
            }
            // Si no se especificó bots, completar hasta 5 participantes total
            if (botsStr == null) {
                bots = Math.max(0, 5 - nombresJugadores.size());
            }
            bots = Math.min(bots, 4 - nombresJugadores.size() + (4 - nombresJugadores.size()));
            bots = Math.max(0, Math.min(bots, 4));

            // (Re)inicializar estado
            tablero            = new Tablero();
            turnos             = new Turnos();
            tabla              = new TablaHash(10);
            historial          = new Pila<>();
            arbol              = new ArbolBST();
            ganador            = null;
            preguntaActual     = null;
            esperandoRespuesta = false;
            jugadorEnReto      = null;
            nombresHumanos     = new HashSet<>();

            // Registrar jugadores humanos
            for (String n : nombresJugadores) {
                Jugador j = new Jugador(n.trim());
                turnos.encolarJugador(j);
                tabla.insertar(j);
                nombresHumanos.add(n.trim());
            }

            // Registrar bots
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

    /** GET /api/estado */
    static class EstadoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204,-1); return; }
            if (!juegoIniciado) { sendError(ex, 400, "Juego no iniciado"); return; }
            sendJson(ex, 200, estadoJson());
        }
    }

    /**
     * POST /api/tirar
     * Procesa UN SOLO turno: el del jugador actual.
     * Si es bot → resuelve su turno y retorna (el frontend llama de nuevo para el siguiente).
     * Si es humano → tira el dado, aplica movimiento y retorna.
     * Si hay reto para humano → retorna con esperandoRespuesta=true.
     */
    static class TirarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204,-1); return; }
            if (!juegoIniciado || ganador != null) { sendError(ex, 400, "Juego no activo"); return; }
            if (esperandoRespuesta)               { sendError(ex, 400, "Hay un reto pendiente"); return; }

            Jugador jugador = turnos.jugadorActual();
            procesarTurnoJugador(jugador, esBot(jugador));

            sendJson(ex, 200, estadoJson());
        }
    }

    /** POST /api/responder { "respuesta": "42" } */
    static class ResponderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204,-1); return; }
            if (!esperandoRespuesta) { sendError(ex, 400, "No hay reto activo"); return; }

            String body   = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String resp   = extraerCampo(body, "respuesta");
            Jugador jugador = buscarJugadorPorNombre(jugadorEnReto);

            boolean correcto = arbol.validar(preguntaActual, resp != null ? resp : "");
            String evento;
            if (correcto) {
                jugador.setAciertos(jugador.getAciertos() + 1);
                evento = "correcto";
                // turno extra: NO avanzamos el turno
            } else {
                jugador.setFallos(jugador.getFallos() + 1);
                evento = "incorrecto";
                turnos.siguienteTurno(); // pierde turno
            }

            esperandoRespuesta = false;
            String respuestaCorrecta = preguntaActual.getRespuesta();
            jugadorEnReto  = null;
            preguntaActual = null;

            if (Reglas.esGanador(jugador.getPosicion(), tablero.getTotalCasillas())) {
                ganador = jugador.getNombreUsuario();
            }

            String json = "{ \"evento\": \"" + evento + "\", \"respuestaCorrecta\": " + jsonStr(respuestaCorrecta) + ", " + estadoJsonInner() + " }";
            sendJson(ex, 200, json);
        }
    }

    /** GET /api/historial */
    static class HistorialHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (!juegoIniciado) { sendError(ex, 400, "Juego no iniciado"); return; }
            sendJson(ex, 200, "{ \"historial\": " + historialJson() + " }");
        }
    }

    /** GET /api/ranking */
    static class RankingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if (!juegoIniciado) { sendError(ex, 400, "Juego no iniciado"); return; }
            sendJson(ex, 200, "{ \"ranking\": " + rankingJson() + " }");
        }
    }

    /** POST /api/reiniciar */
    static class ReiniciarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            juegoIniciado = false;
            ganador = null;
            ultimoDado = 0; ultimoJugador = null; ultimoDesde = 0; ultimoHasta = 0; ultimoEvento = "normal";
            sendJson(ex, 200, "{ \"ok\": true }");
        }
    }

    /** Sirve archivos estáticos desde web/ */
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File base = new File(System.getProperty("user.dir"));
            File file = new File(base, "web" + path);
            if (!file.exists() || !file.isFile()) { sendError(ex, 404, "Not Found: " + path); return; }
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

    // ════════════════════════════════════════════════════════════════════
    //  Lógica del juego
    // ════════════════════════════════════════════════════════════════════

    private static boolean esBot(Jugador j) {
        return !nombresHumanos.contains(j.getNombreUsuario());
    }

    private static void procesarTurnoJugador(Jugador jugador, boolean bot) {
        int dado = random.nextInt(6) + 1;
        historial.apilar(jugador.getNombreUsuario() + " sacó " + dado);

        // Registrar movimiento para el frontend
        ultimoDado    = dado;
        ultimoJugador = jugador.getNombreUsuario();
        ultimoEvento  = "normal";

        if (!Reglas.puedeMover(jugador.getPosicion(), dado, tablero.getTotalCasillas())) {
            historial.apilar(jugador.getNombreUsuario() + " no puede mover (necesita exacto)");
            ultimoDesde = jugador.getPosicion();
            ultimoHasta = jugador.getPosicion();
            turnos.siguienteTurno();
            return;
        }

        ultimoDesde = jugador.getPosicion();
        int nuevaPos = Reglas.calcularNuevaPos(jugador.getPosicion(), dado);
        jugador.setPosicion(nuevaPos);
        ultimoHasta = nuevaPos;
        historial.apilar(jugador.getNombreUsuario() + " avanzó a casilla " + nuevaPos);

        Casilla casilla = tablero.buscarPorNumero(nuevaPos);
        boolean turnoExtra = procesarCasilla(jugador, casilla, bot);

        if (Reglas.esGanador(jugador.getPosicion(), tablero.getTotalCasillas())) {
            ganador = jugador.getNombreUsuario();
            return;
        }

        if (!turnoExtra && !esperandoRespuesta) {
            turnos.siguienteTurno();
        }
    }

    private static boolean procesarCasilla(Jugador jugador, Casilla casilla, boolean bot) {
        return switch (casilla.getTipo()) {
            case "ESCALERA" -> {
                int dest = tablero.getGrafo().obtenerDestino(casilla.getNumero());
                jugador.setPosicion(dest);
                ultimoDesde  = casilla.getNumero(); // casilla de inicio de escalera
                ultimoHasta  = dest;
                ultimoEvento = "escalera";
                historial.apilar("🪜 ¡Escalera! " + jugador.getNombreUsuario() + " sube a " + dest);
                yield false;
            }
            case "SERPIENTE" -> {
                int dest = tablero.getGrafo().obtenerDestino(casilla.getNumero());
                jugador.setPosicion(dest);
                ultimoDesde  = casilla.getNumero(); // casilla de la cabeza de serpiente
                ultimoHasta  = dest;
                ultimoEvento = "serpiente";
                historial.apilar("🐍 ¡Serpiente! " + jugador.getNombreUsuario() + " baja a " + dest);
                yield false;
            }
            case "RETO" -> {
                int nivel = Reglas.calcularNivel(jugador.getPosicion(), tablero.getTotalCasillas());
                String cat = CATEGORIAS[random.nextInt(CATEGORIAS.length)];
                Pregunta p = arbol.buscar(nivel, cat);
                if (p == null) {
                    historial.apilar("❓ Reto sin pregunta para " + jugador.getNombreUsuario());
                    yield false;
                }
                if (bot) {
                    boolean acierta = random.nextBoolean();
                    if (acierta) {
                        jugador.setAciertos(jugador.getAciertos() + 1);
                        historial.apilar("🤖 " + jugador.getNombreUsuario() + " respondió CORRECTO");
                        yield true;
                    } else {
                        jugador.setFallos(jugador.getFallos() + 1);
                        historial.apilar("🤖 " + jugador.getNombreUsuario() + " respondió MAL");
                        turnos.siguienteTurno();
                        yield false;
                    }
                } else {
                    preguntaActual     = p;
                    esperandoRespuesta = true;
                    jugadorEnReto      = jugador.getNombreUsuario();
                    historial.apilar("❓ RETO para " + jugador.getNombreUsuario() + ": [" + p.getCategoria() + " Niv." + p.getDificultad() + "]");
                    yield false;
                }
            }
            default -> { yield false; }
        };
    }

    // ════════════════════════════════════════════════════════════════════
    //  JSON helpers
    // ════════════════════════════════════════════════════════════════════

    private static String estadoJson() {
        return "{ " + estadoJsonInner() + " }";
    }

    private static String estadoJsonInner() {
        StringBuilder sb = new StringBuilder();
        Jugador actual = turnos.jugadorActual();
        sb.append("\"ganador\": ").append(ganador != null ? jsonStr(ganador) : "null").append(",");
        sb.append("\"turnoActual\": ").append(jsonStr(actual.getNombreUsuario())).append(",");
        sb.append("\"esBot\": ").append(esBot(actual)).append(",");
        sb.append("\"esperandoRespuesta\": ").append(esperandoRespuesta).append(",");
        if (esperandoRespuesta && preguntaActual != null) {
            sb.append("\"reto\": {");
            sb.append("\"enunciado\": ").append(jsonStr(preguntaActual.getEnunciado())).append(",");
            sb.append("\"categoria\": ").append(jsonStr(preguntaActual.getCategoria())).append(",");
            sb.append("\"dificultad\": ").append(preguntaActual.getDificultad());
            sb.append("},");
        } else {
            sb.append("\"reto\": null,");
        }
        // Lista de nombres humanos
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

    private static String jugadoresJson() {
        int cant = turnos.cantidadJugadores();
        List<Jugador> lista = new ArrayList<>();
        for (int i = 0; i < cant; i++) {
            lista.add(turnos.siguienteTurno());
        }
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

    private static String historialJson() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String s = historial.desapilar();
            if (s == null) break;
            items.add(s);
        }
        for (int i = items.size() - 1; i >= 0; i--) historial.apilar(items.get(i));
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append(jsonStr(items.get(i)));
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String rankingJson() {
        int cant = turnos.cantidadJugadores();
        List<Jugador> lista = new ArrayList<>();
        for (int i = 0; i < cant; i++) lista.add(turnos.siguienteTurno());
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

    private static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    // ════════════════════════════════════════════════════════════════════
    //  HTTP utils
    // ════════════════════════════════════════════════════════════════════

    private static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private static void sendError(HttpExchange ex, int code, String msg) throws IOException {
        sendJson(ex, code, "{ \"error\": " + jsonStr(msg) + " }");
    }

    private static String extraerCampo(String json, String campo) {
        String key = "\"" + campo + "\"";
        int idx = json.indexOf(key);
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx + key.length());
        if (colon < 0) return null;
        // Valor numérico (sin comillas)
        String rest = json.substring(colon + 1).trim();
        if (!rest.startsWith("\"")) {
            // número u otro valor primitivo
            int end = rest.indexOf(',');
            if (end < 0) end = rest.indexOf('}');
            if (end < 0) end = rest.length();
            return rest.substring(0, end).trim();
        }
        int q1 = json.indexOf("\"", colon + 1);
        if (q1 < 0) return null;
        int q2 = json.indexOf("\"", q1 + 1);
        if (q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    /** Extrae un array de strings JSON: ["a","b","c"] del campo dado */
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
        // Parsear strings del array
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

    private static Jugador buscarJugadorPorNombre(String nombre) {
        int cant = turnos.cantidadJugadores();
        for (int i = 0; i < cant; i++) {
            Jugador j = turnos.siguienteTurno();
            if (j.getNombreUsuario().equals(nombre)) return j;
        }
        return null;
    }
}
