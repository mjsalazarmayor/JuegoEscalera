package modelo;

public class Pregunta {

    private int dificultad;
    private String enunciado;
    private String respuesta;
    private String categoria;

    public Pregunta(int dificultad, String categoria, String enunciado, String respuesta) {
        this.dificultad = dificultad;
        this.enunciado = enunciado;
        this.categoria = categoria;
        this.respuesta = respuesta;
    }

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
