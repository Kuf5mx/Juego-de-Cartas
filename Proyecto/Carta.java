/**
 * Representa una carta del juego (Pokemon o Pocion)
 */
public class Carta {

    private String nombre;
    private int vida;
    private int vidaMaxima;
    private int danio;
    private boolean esPocion;

    // Constructor para Pokemon
    public Carta(String nombre, int vida, int danio) {
        this(nombre, vida, danio, false);
    }

    // Constructor general
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

    public void recibirDanio(int cantidad) {
        if (esPocion || cantidad <= 0) return;
        vida = Math.max(0, vida - cantidad);
    }

    public boolean estaFueraDeCombate() {
        return !esPocion && vida <= 0;
    }

    public void curar(int cantidad) {
        if (esPocion || cantidad <= 0 || estaFueraDeCombate()) return;
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