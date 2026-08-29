/**
 * Guarda el nombre del jugador para la cola de turnos.
 */
public class Jugador {

    // Nombre del jugador.
    private String nombre;
    // Mazo de robo.
    private Pila mazo;
    // Mazo de descarte.
    private Pila descarte;
    // Mano de dos cartas.
    private Carta[] mano;
    // Pokemon activo.
    private Carta activo;
    // Banca de apoyo.
    private Carta[] banca;
    // Puntos ganados.
    private int puntos;

    /**
     * Crea un jugador con su nombre.
     */
    public Jugador(String nombre, Pila mazo) {
        this.nombre = nombre;
        this.mazo = mazo;
        this.descarte = new Pila(20);
        this.mano = new Carta[2];
        this.banca = new Carta[3];
        this.puntos = 0;
    }

    // Devuelve el nombre.
    public String getNombre() {
        return nombre;
    }

    // Devuelve el pokemon activo.
    public Carta getActivo() {
        return activo;
    }

    // Devuelve una carta de la banca.
    public Carta getPokemonBanca(int indice) {
        return banca[indice];
    }

    // Devuelve los puntos.
    public int getPuntos() {
        return puntos;
    }

    // Devuelve el mazo.
    public Pila getMazo() {
        return mazo;
    }

    // Devuelve el descarte.
    public Pila getDescarte() {
        return descarte;
    }

    // Suma un punto.
    public void sumarPunto() {
        puntos++;
    }

    // Muestra la mano de dos cartas.
    public void mostrarMano() {
        System.out.println("Mano de " + nombre + ":");
        for (int i = 0; i < mano.length; i++) {
            System.out.println((i + 1) + ". " + mano[i]);
        }
    }

    // Llena la mano hasta tener dos cartas.
    public void llenarMano() {
        for (int i = 0; i < mano.length; i++) {
            if (mano[i] == null) {
                mano[i] = mazo.desapilar();
            }
        }
    }

    // Roba una sola carta y la guarda en la primera casilla libre de la mano.
    public boolean robarCartaAMano() {
        for (int i = 0; i < mano.length; i++) {
            if (mano[i] == null) {
                mano[i] = mazo.desapilar();
                return mano[i] != null;
            }
        }

        return false;
    }

    // Regresa una carta de la mano.
    public Carta obtenerCartaDeMano(int indice) {
        if (indice < 0 || indice >= mano.length) {
            return null;
        }

        return mano[indice];
    }

    // Saca una carta de la mano.
    public Carta sacarCartaDeMano(int indice) {
        if (indice < 0 || indice >= mano.length) {
            return null;
        }

        Carta carta = mano[indice];
        mano[indice] = null;
        return carta;
    }

    // Descarta una carta de la mano.
    public void descartarCartaDeMano(int indice) {
        Carta carta = sacarCartaDeMano(indice);
        if (carta != null) {
            descarte.apilar(carta);
        }
    }

    // Usa una pocion de la mano sobre el activo y la descarta.
    public boolean usarPocionEnActivo(int indiceMano) {
        Carta carta = obtenerCartaDeMano(indiceMano);
        if (carta == null || !carta.esPocion() || activo == null) {
            return false;
        }

        activo.curar(carta.getVida());
        descartarCartaDeMano(indiceMano);
        return true;
    }

    // Muestra el descarte.
    public void mostrarDescarte() {
        System.out.println("Descarte de " + nombre + ":");
        descarte.mostrar();
    }

    // Muestra el campo del jugador.
    public void mostrarCampo() {
        System.out.println("Activo de " + nombre + ": " + activo);
        for (int i = 0; i < banca.length; i++) {
            System.out.println("Banca " + (i + 1) + ": " + banca[i]);
        }
    }

    // Pone el primer campo inicial.
    public void prepararCampoInicial() {
        if (activo == null) {
            activo = mazo.desapilar();
        }

        for (int i = 0; i < banca.length; i++) {
            if (banca[i] == null) {
                banca[i] = mazo.desapilar();
            }
        }
    }

    // Pone una carta en activo si puede.
    public Carta robarCarta() {
        return mazo.desapilar();
    }

    // Intenta poner una carta en activo.
    public void jugarCarta(Carta carta) {
        if (carta == null) {
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

    // Coloca una carta en activo.
    public boolean ponerCartaEnActivo(Carta carta) {
        if (carta == null || activo != null) {
            return false;
        }

        activo = carta;
        return true;
    }

    // Coloca una carta en banca.
    public boolean ponerCartaEnBanca(Carta carta, int indice) {
        if (carta == null || indice < 0 || indice >= banca.length || banca[indice] != null) {
            return false;
        }

        banca[indice] = carta;
        return true;
    }

    // Cambia el activo con la banca.
    public boolean cambiarActivoConBanca(int indice) {
        if (indice < 0 || indice >= banca.length || banca[indice] == null) {
            return false;
        }

        Carta temporal = activo;
        activo = banca[indice];
        banca[indice] = temporal;
        return true;
    }

    // Manda una carta al descarte.
    public void descartarCarta(Carta carta) {
        if (carta != null) {
            descarte.apilar(carta);
        }
    }

    // Recibe dano en el pokemon activo.
    public void recibirDanio(int danio) {
        if (activo != null) {
            activo.recibirDanio(danio);
        }
    }

    // Revisa si el activo ya perdio.
    public boolean activoFueraDeCombate() {
        return activo != null && activo.estaFueraDeCombate();
    }

    // Saca al activo.
    public Carta sacarActivo() {
        Carta carta = activo;
        activo = null;
        return carta;
    }

    // Promueve una carta de la banca al activo.
    public boolean promoverSiguientePokemon() {
        for (int i = 0; i < banca.length; i++) {
            if (banca[i] != null) {
                activo = banca[i];
                banca[i] = null;
                return true;
            }
        }

        activo = mazo.desapilar();
        return activo != null;
    }

    // Revisa si sigue teniendo pokemon.
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

    /**
     * Devuelve un texto corto del jugador.
     */
    @Override
    public String toString() {
        return "Jugador{" + "nombre='" + nombre + "'" + '}';
    }
}