/**
 * Representa una carta del juego (Pokémon o Poción).
 */
public class Carta {

    private final String nombre;
    private int vida;
    private final int vidaMaxima;
    private final int danio;
    private final boolean esPocion;

    /**
     * Crea una carta de tipo Pokémon.
     */
    public Carta(String nombre, int vida, int danio) {
        this(nombre, vida, danio, false);
    }

    /**
     * Crea una carta Pokémon o una Poción.
     */
    public Carta(String nombre, int vida, int danio, boolean esPocion) {
        this.nombre = nombre;
        this.vida = Math.max(0, vida);
        this.vidaMaxima = this.vida;
        this.danio = Math.max(0, danio);
        this.esPocion = esPocion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getDanio() {
        return danio;
    }

    public boolean esPocion() {
        return esPocion;
    }

    /**
     * Resta vida a la carta si es un Pokémon.
     */
    public void recibirDanio(int cantidad) {
        if (esPocion || cantidad <= 0) {
            return; // Las pociones no reciben daño y no se acepta daño negativo.
        }

        vida = Math.max(0, vida - cantidad);
    }

    public boolean estaFueraDeCombate() {
        return !esPocion && vida <= 0;
    }

    /**
     * Recupera vida sin exceder el máximo inicial.
     */
    public void curar(int cantidad) {
        if (esPocion || cantidad <= 0 || estaFueraDeCombate()) {
            return; // No cura pociones, cantidades negativas o cartas debilitadas.
        }

        vida = Math.min(vidaMaxima, vida + cantidad);
    }

    @Override
    public String toString() {
        if (esPocion) {
            return "Carta{nombre='" + nombre + "', cura=" + vidaMaxima + ", pocion=true}";
        }
        return "Carta{nombre='" + nombre + "', vida=" + vida + "/" + vidaMaxima + ", danio=" + danio + "}";
    }
}