import java.util.Scanner;

/**
 * Representa a un jugador con mano dinamica y tablero
 */
public class Jugador {

    private String nombre;
    private Pila mazo;
    private Pila descarte;
    private ListaSimple mano;
    private ListaDoble historialJugadas;
    private Carta activo;
    private Carta[] banca;
    private int puntos;

    public Jugador(String nombre, Pila mazo) {
        this.nombre = nombre;
        this.mazo = mazo;
        this.descarte = new Pila(25);
        this.mano = new ListaSimple();
        this.historialJugadas = new ListaDoble();
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

    public ListaDoble getHistorialJugadas() {
        return historialJugadas;
    }

    public int getTamanoMano() {
        return mano.size();
    }

    public void registrarJugada(Carta carta) {
        if (carta != null) {
            historialJugadas.agregar(carta);
        }
    }

    public void mostrarHistorialJugadas() {
        historialJugadas.mostrarAdelante();
    }

    public void sumarPunto() {
        puntos++;
    }

    public boolean robarCartaAMano() {
        if (!mazo.estaVacia()) {
            Carta carta = mazo.desapilar();
            if (carta != null) {
                mano.agregar(carta);
                return true;
            }
        }
        return false;
    }

    public void robarCartasIniciales(int cantidad) {
        int pokemonesEnMano = 0;
        for (int i = 0; i < cantidad; i++) {
            if (!robarCartaAMano()) break;
            Carta ultima = mano.obtenerPorIndice(mano.size() - 1);
            if (ultima != null && !ultima.esPocion()) pokemonesEnMano++;
        }
        while (pokemonesEnMano < 2 && robarCartaAMano()) {
            Carta ultima = mano.obtenerPorIndice(mano.size() - 1);
            if (ultima != null && !ultima.esPocion()) pokemonesEnMano++;
        }
    }

    public void elegirCampoInicial(Scanner scanner) {
        System.out.println("\n" + nombre + ", elige tu Pokemon activo y uno para la banca.");
        mostrarMano();

        while (activo == null) {
            int opcion = leerOpcionMano(scanner, "Numero del Pokemon activo: ");
            Carta elegido = obtenerCartaDeMano(opcion - 1);
            if (elegido != null && !elegido.esPocion()) {
                activo = sacarCartaDeMano(opcion - 1);
            } else {
                System.out.println("Debes elegir un Pokemon.");
            }
        }

        while (true) {
            int opcion = leerOpcionMano(scanner, "Numero del Pokemon para la banca (0 para dejarla vacia): ");
            if (opcion == 0) return;
            Carta elegido = obtenerCartaDeMano(opcion - 1);
            if (elegido != null && !elegido.esPocion()) {
                banca[0] = sacarCartaDeMano(opcion - 1);
                return;
            }
            System.out.println("Debes elegir un Pokemon.");
        }
    }

    private int leerOpcionMano(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero valido.");
            }
        }
    }

    public boolean usarPokeball(int indiceMano) {
        Carta pokeball = obtenerCartaDeMano(indiceMano);
        if (pokeball == null || !pokeball.getNombre().equals("Pokeball")) return false;

        sacarCartaDeMano(indiceMano);
        descarte.apilar(pokeball);

        Pila cartasTemporales = new Pila(15);
        Carta pokemon = null;
        while (!mazo.estaVacia()) {
            Carta carta = mazo.desapilar();
            if (!carta.esPocion()) {
                pokemon = carta;
                break;
            }
            cartasTemporales.apilar(carta);
        }

        while (!cartasTemporales.estaVacia()) {
            mazo.apilar(cartasTemporales.desapilar());
        }

        if (pokemon == null) return false;
        mano.agregar(pokemon);
        return true;
    }

    public void mostrarMano() {
        System.out.println("Mano de " + nombre + " (" + mano.size() + " cartas):");
        if (mano.estaVacia()) {
            System.out.println("  (Mano vacia)");
            return;
        }
        for (int i = 0; i < mano.size(); i++) {
            System.out.println((i + 1) + ". " + mano.obtenerPorIndice(i));
        }
    }

    public Carta obtenerCartaDeMano(int indice) {
        if (indice < 0 || indice >= mano.size()) return null;
        return mano.obtenerPorIndice(indice);
    }

    public Carta sacarCartaDeMano(int indice) {
        if (indice < 0 || indice >= mano.size()) {
            throw new ListaVaciaException("No se puede sacar una carta de una lista vacia.");
        }
        return mano.quitarPorIndice(indice);
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