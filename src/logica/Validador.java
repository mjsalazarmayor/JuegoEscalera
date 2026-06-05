package logica;

public class Validador {

    private Validador() {}  // Clase de utilidad, no instanciable

    // Restricciones del nombre
    private static final int MIN_NOMBRE = 2;
    private static final int MAX_NOMBRE = 20;

    /**
     * Valida que el nombre solo contenga letras y espacios
     */
    public static boolean nombreValido(String nombre) {
        if (nombre == null || nombre.isBlank()) return false;
        if (nombre.length() < MIN_NOMBRE) return false;
        if (nombre.length() > MAX_NOMBRE) return false;
        
        // Verifica caracter a caracter: solo letras o espacios
        for (char c : nombre.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') return false;
        }
        return true;
    }

    /**
     * Retorna mensaje específico según la causa del error
     */
    public static String mensajeNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            return "El nombre no puede estar vacio.";
        if (nombre.length() < MIN_NOMBRE)
            return "Minimo " + MIN_NOMBRE + " caracteres.";
        if (nombre.length() > MAX_NOMBRE)
            return "Maximo " + MAX_NOMBRE + " caracteres.";
        for (char c : nombre.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ')
                return "Caracter no permitido: '" + c + "'.";
        }
        return "";  // Nombre válido
    }

    /**
     * Verifica si una opción numérica está dentro del rango [min, max]
     */
    public static boolean opcionValida(String entrada, int min, int max) {
        if (entrada == null || entrada.isBlank()) return false;
        try {
            int valor = Integer.parseInt(entrada.trim());
            return valor >= min && valor <= max;
        } catch (NumberFormatException e) {
            return false;  // No es número válido
        }
    }

    public static boolean respuestaValida(String respuesta) {
        return respuesta != null && !respuesta.isBlank() && respuesta.length() <= 100;
    }
}