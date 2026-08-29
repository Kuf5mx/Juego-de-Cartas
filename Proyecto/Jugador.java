/**
 * Guarda el nombre del jugador para la cola de turnos
 */
public class Jugador {

    private String nombre;
    private Pila mazo;
    private Pila descarte;
    private Carta[] mano;
    private Carta activo;
    private Carta[] banca;
    private int puntos;

    /**
     * Crea un jugador con su nombre
     */
    public Jugador(String nombre, Pila mazo) {
        this.nombre = nombre;
        this.mazo = mazo;
        this.descarte = new Pila(20);
        this.mano = new Carta[2];
        this.banca = new Carta[3];
        this.puntos = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public Carta getActivo() {
        return activo;
    }

    // Corregido: Validacion de indice
    public Carta getPokemonBanca(int indice) {
        if (indice < 0 || indice >= banca.length) {
            return null;
        }
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

    public void sumarPunto() {
        puntos++;
    }

    public void mostrarMano() {
        System.out.println("Mano de " + nombre + ":");
        for (int i = 0; i < mano.length; i++) {
            System.out.println((i + 1) + ". " + mano[i]);
        }
    }

    public void llenarMano() {
        for (int i = 0; i < mano.length; i++) {
            if (mano[i] == null) {
                Carta carta = mazo.desapilar();
                if (carta != null) {
                    mano[i] = carta;
                }
            }
        }
    }

    // Corregido: Solo asigna si la carta desapilada no es null
    public boolean robarCartaAMano() {
        for (int i = 0; i < mano.length; i++) {
            if (mano[i] == null) {
                Carta carta = mazo.desapilar();
                if (carta != null) {
                    mano[i] = carta;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public Carta obtenerCartaDeMano(int indice) {
        if (indice < 0 || indice >= mano.length) {
            return null;
        }
        return mano[indice];
    }

    public Carta sacarCartaDeMano(int indice) {
        if (indice < 0 || indice >= mano.length) {
            return null;
        }
        Carta carta = mano[indice];
        mano[indice] = null;
        return carta;
    }

    public void descartarCartaDeMano(int indice) {
        Carta carta = sacarCartaDeMano(indice);
        if (carta != null) {
            descarte.apilar(carta);
        }
    }

    public boolean usarPocionEnActivo(int indiceMano) {
        Carta carta = obtenerCartaDeMano(indiceMano);
        if (carta == null || !carta.esPocion() || activo == null) {
            return false;
        }

        activo.curar(carta.getVida());
        descartarCartaDeMano(indiceMano);
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
        if (activo == null) {
            Carta carta = mazo.desapilar();
            if (carta != null && !carta.esPocion()) {
                activo = carta;
            } else if (carta != null) {
                descarte.apilar(carta);
            }
        }

        for (int i = 0; i < banca.length; i++) {
            if (banca[i] == null) {
                Carta carta = mazo.desapilar();
                if (carta != null && !carta.esPocion()) {
                    banca[i] = carta;
                } else if (carta != null) {
                    descarte.apilar(carta);
                }
            }
        }
    }

    public Carta robarCarta() {
        return mazo.desapilar();
    }

    // Corregido: Las pociones no van al campo activo o banca
    public void jugarCarta(Carta carta) {
        if (carta == null) {
            return;
        }

        if (carta.esPocion()) {
            descarte.apilar(carta);
            return;
        }

        if (activo == null) {
            activo = carta;
            return;
        }

        for (int i = 0; i < banca.length; i++) {
            if (banca[i] == null) {
                banca[i] = carta;
                return;
            }
        }

        descarte.apilar(carta);
    }

    // Corregido: Validar que no sea pocion
    public boolean ponerCartaEnActivo(Carta carta) {
        if (carta == null || carta.esPocion() || activo != null) {
            return false;
        }

        activo = carta;
        return true;
    }

    // Corregido: Validar que no sea pocion
    public boolean ponerCartaEnBanca(Carta carta, int indice) {
        if (carta == null || carta.esPocion() || indice < 0 || indice >= banca.length || banca[indice] != null) {
            return false;
        }

        banca[indice] = carta;
        return true;
    }

    public boolean cambiarActivoConBanca(int indice) {
        if (indice < 0 || indice >= banca.length || banca[indice] == null) {
            return false;
        }

        Carta temporal = activo;
        activo = banca[indice];
        banca[indice] = temporal;
        return true;
    }

    public void descartarCarta(Carta carta) {
        if (carta != null) {
            descarte.apilar(carta);
        }
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

        Carta cartaMazo = mazo.desapilar();
        while (cartaMazo != null && cartaMazo.esPocion()) {
            descarte.apilar(cartaMazo);
            cartaMazo = mazo.desapilar();
        }

        activo = cartaMazo;
        return activo != null;
    }

    public boolean tienePokemonEnJuego() {
        if (activo != null) {
            return true;
        }

        for (int i = 0; i < banca.length; i++) {
            if (banca[i] != null) {
                return true;
            }
        }

        return !mazo.estaVacia();
    }

    @Override
    public String toString() {
        return "Jugador{" + "nombre='" + nombre + "'" + '}';
    }
}