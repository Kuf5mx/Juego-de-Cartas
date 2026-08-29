import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un jugador con mano dinamica y tablero
 */
public class Jugador {

    private String nombre;
    private Pila mazo;
    private Pila descarte;
    private List<Carta> mano;
    private Carta activo;
    private Carta[] banca;
    private int puntos;

    public Jugador(String nombre, Pila mazo) {
        this.nombre = nombre;
        this.mazo = mazo;
        this.descarte = new Pila(25);
        this.mano = new ArrayList<>();
        this.banca = new Carta[3];
        this.puntos = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public Carta getActivo() {
        return activo;
    }

    public Carta getPokemonBanca(int indice) {
        if (indice < 0 || indice >= banca.length) return null;
        return banca[indice];
    }

    public int getPuntos() {
        return puntos;
    }

    public Pila getMazo() {
        return mazo;
    }

    public Pila getDescarte() {
        return descarte;
    }

    public int getTamanoMano() {
        return mano.size();
    }

    public void sumarPunto() {
        puntos++;
    }

    public boolean robarCartaAMano() {
        if (!mazo.estaVacia()) {
            Carta carta = mazo.desapilar();
            if (carta != null) {
                mano.add(carta);
                return true;
            }
        }
        return false;
    }

    public void mostrarMano() {
        System.out.println("Mano de " + nombre + " (" + mano.size() + " cartas):");
        if (mano.isEmpty()) {
            System.out.println("  (Mano vacia)");
            return;
        }
        for (int i = 0; i < mano.size(); i++) {
            System.out.println((i + 1) + ". " + mano.get(i));
        }
    }

    public Carta obtenerCartaDeMano(int indice) {
        if (indice < 0 || indice >= mano.size()) return null;
        return mano.get(indice);
    }

    public Carta sacarCartaDeMano(int indice) {
        if (indice < 0 || indice >= mano.size()) return null;
        return mano.remove(indice);
    }

    public void descartarCartaDeMano(int indice) {
        Carta carta = sacarCartaDeMano(indice);
        if (carta != null) {
            descarte.apilar(carta);
        }
    }

    public boolean usarPocionEnActivo(int indiceMano) {
        Carta carta = obtenerCartaDeMano(indiceMano);
        if (carta == null || !carta.esPocion() || activo == null) return false;

        activo.curar(carta.getVida());
        sacarCartaDeMano(indiceMano);
        descarte.apilar(carta);
        return true;
    }

    public void mostrarDescarte() {
        System.out.println("Descarte de " + nombre + ":");
        descarte.mostrar();
    }

    public void mostrarCampo() {
        System.out.println("Activo de " + nombre + ": " + activo);
        for (int i = 0; i < banca.length; i++) {
            System.out.println("Banca " + (i + 1) + ": " + banca[i]);
        }
    }

    public void prepararCampoInicial() {
        while (activo == null && !mazo.estaVacia()) {
            Carta c = mazo.desapilar();
            if (!c.esPocion()) {
                activo = c;
            } else {
                descarte.apilar(c);
            }
        }

        for (int i = 0; i < banca.length; i++) {
            while (banca[i] == null && !mazo.estaVacia()) {
                Carta c = mazo.desapilar();
                if (!c.esPocion()) {
                    banca[i] = c;
                } else {
                    descarte.apilar(c);
                }
            }
        }
    }

    public boolean ponerCartaEnActivo(Carta carta) {
        if (carta == null || carta.esPocion() || activo != null) return false;
        activo = carta;
        return true;
    }

    public boolean ponerCartaEnBanca(Carta carta, int indice) {
        if (carta == null || carta.esPocion() || indice < 0 || indice >= banca.length || banca[indice] != null) {
            return false;
        }
        banca[indice] = carta;
        return true;
    }

    public boolean cambiarActivoConBanca(int indice) {
        if (indice < 0 || indice >= banca.length || banca[indice] == null) return false;
        Carta temp = activo;
        activo = banca[indice];
        banca[indice] = temp;
        return true;
    }

    public void recibirDanio(int danio) {
        if (activo != null) {
            activo.recibirDanio(danio);
        }
    }

    public boolean activoFueraDeCombate() {
        return activo != null && activo.estaFueraDeCombate();
    }

    public Carta sacarActivo() {
        Carta carta = activo;
        activo = null;
        return carta;
    }

    public boolean promoverSiguientePokemon() {
        for (int i = 0; i < banca.length; i++) {
            if (banca[i] != null) {
                activo = banca[i];
                banca[i] = null;
                return true;
            }
        }
        while (!mazo.estaVacia()) {
            Carta c = mazo.desapilar();
            if (!c.esPocion()) {
                activo = c;
                return true;
            } else {
                descarte.apilar(c);
            }
        }
        return false;
    }
}