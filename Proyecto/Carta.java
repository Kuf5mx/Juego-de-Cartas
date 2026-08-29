/**
 * Representa una carta simple del proyecto.
 */
public class Carta {

    // Nombre de la carta.
    private String nombre;
    // Vida de la carta.
    private int vida;
    // Vida con la que nacio, sirve de tope para curarse.
    private int vidaMaxima;
    // Dano que hace.
    private int danio;
    // Si es true, la carta es una pocion y no un pokemon.
    private boolean esPocion;

    /**
     * Crea una carta con nombre, costo y poder.
     */
    public Carta(String nombre, int vida, int danio) {
        this(nombre, vida, danio, false);
    }

    /**
     * Crea una carta pokemon o una pocion (cuando esPocion es true, vida = cuanto cura).
     */
    public Carta(String nombre, int vida, int danio, boolean esPocion) {
        this.nombre = nombre;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.danio = danio;
        this.esPocion = esPocion;
    }

    // Regresa el nombre.
    public String getNombre() {
        return nombre;
    }

    // Regresa la vida.
    public int getVida() {
        return vida;
    }

    // Regresa el dano.
    public int getDanio() {
        return danio;
    }

    // Resta vida cuando recibe dano.
    public void recibirDanio(int cantidad) {
        vida = vida - cantidad;
        if (vida < 0) {
            vida = 0;
        }
    }

    // Revisa si ya perdio toda la vida.
    public boolean estaFueraDeCombate() {
        return vida <= 0;
    }

    // Dice si esta carta es una pocion.
    public boolean esPocion() {
        return esPocion;
    }

    // Sube la vida sin pasarse de la vida maxima.
    public void curar(int cantidad) {
        vida = vida + cantidad;
        if (vida > vidaMaxima) {
            vida = vidaMaxima;
        }
    }

    /**
     * Regresa la carta en un formato sencillo para imprimir.
     */
    @Override
    public String toString() {
        if (esPocion) {
            return "Carta{nombre='" + nombre + "', cura=" + vida + ", pocion=true}";
        }
        return "Carta{nombre='" + nombre + "', vida=" + vida + ", danio=" + danio + "}";
    }
}