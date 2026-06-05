package modelo;

public class Pregunta {

    private int dificultad;     // 1, 2 o 3 (corresponde al nivel del tablero)
    private String enunciado;   // Texto de la pregunta
    private String respuesta;   // Respuesta correcta
    private String categoria;   // Ej: "Historia", "Ciencia", etc.

    public Pregunta(int dificultad, String categoria, String enunciado, String respuesta) {
        this.dificultad = dificultad;
        this.enunciado = enunciado;
        this.categoria = categoria;
        this.respuesta = respuesta;
    }

    // Getters
    public int getDificultad() {
        return dificultad;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "[Niv." + dificultad + " | " + categoria + "] " + enunciado;
    }

}