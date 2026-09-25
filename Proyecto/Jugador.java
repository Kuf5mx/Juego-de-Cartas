import java.util.Scanner;
import java.util.Random;

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
        for (int i = 0; i < cantidad; i++) {
            if (!robarCartaAMano()) break;
        }
        if (!tienePokemonBasicoEnMano()) {
            reemplazarUltimaCartaInicialPorBasico();
        }
    }

    private boolean tienePokemonBasicoEnMano() {
        for (int i = 0; i < getTamanoMano(); i++) {
            Carta carta = obtenerCartaDeMano(i);
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) return true;
        }
        return false;
    }

    private void reemplazarUltimaCartaInicialPorBasico() {
        if (getTamanoMano() == 0) return;
        Carta reemplazada = sacarCartaDeMano(getTamanoMano() - 1);
        Pila temporal = new Pila(30);
        Carta basico = null;
        while (!mazo.estaVacia()) {
            Carta carta = mazo.desapilar();
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                basico = carta;
                break;
            }
            temporal.apilar(carta);
        }
        while (!temporal.estaVacia()) mazo.apilar(temporal.desapilar());
        if (basico == null) {
            mano.agregar(reemplazada);
            return;
        }
        mano.agregar(basico);
        mazo.apilar(reemplazada);
    }

    public void elegirCampoInicial(Scanner scanner) {
        System.out.println("\n" + nombre + ", elige tu Pokemon activo y uno para la banca.");
        mostrarMano();

        while (activo == null) {
            int opcion = leerOpcionMano(scanner, "Numero del Pokemon activo: ");
            Carta elegido = obtenerCartaDeMano(opcion - 1);
                if (elegido != null && elegido.esPokemon() && elegido.getFase() == 1) {
                activo = sacarCartaDeMano(opcion - 1);
            } else {
                    System.out.println("Eleccion invalida: el activo debe ser un Pokemon de primera fase. Elige un numero de tu mano que corresponda a Bulbasaur, Pichu u otro basico.");
            }
        }

        while (true) {
            int opcion = leerOpcionMano(scanner, "Numero del Pokemon para la banca (0 para dejarla vacia): ");
            if (opcion == 0) return;
            Carta elegido = obtenerCartaDeMano(opcion - 1);
                if (elegido != null && elegido.esPokemon() && elegido.getFase() == 1) {
                banca[0] = sacarCartaDeMano(opcion - 1);
                return;
            }
                System.out.println("Eleccion invalida: la banca solo puede recibir un Pokemon de primera fase. Elige otro numero o escribe 0 para dejarla vacia.");
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

        if (mazo.estaVacia()) {
            throw new ListaVaciaException("No hay cartas en el mazo. No se puede usar la Pokeball.");
        }

        sacarCartaDeMano(indiceMano);
        descarte.apilar(pokeball);

        Pila cartasTemporales = new Pila(30);
        Carta pokemon = null;
        while (!mazo.estaVacia()) {
            Carta carta = mazo.desapilar();
                if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                pokemon = carta;
                break;
            }
            cartasTemporales.apilar(carta);
        }

        while (!cartasTemporales.estaVacia()) {
            mazo.apilar(cartasTemporales.desapilar());
        }

        if (pokemon == null) {
            throw new ListaVaciaException("No quedan Pokemones de primera fase en el mazo. La Pokeball se descarta.");
        }
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

        if (activo.getVida() >= activo.getVidaMaxima()) {
            throw new ListaVaciaException("El Pokemon " + activo.getNombre() + " ya tiene la vida completa. No se puede usar la pocion.");
        }

        activo.curar(carta.getVida());
        sacarCartaDeMano(indiceMano);
        descarte.apilar(carta);
        return true;
    }

    public boolean usarSuperPocion(int indiceMano, Random random) {
        Carta carta = obtenerCartaDeMano(indiceMano);
        if (carta == null || !carta.esPocion() || !carta.esCarta("Superpocion") || activo == null) return false;
        if (activo.getVida() >= activo.getVidaMaxima()) return false;
        activo.curar(carta.getVida());
        if (random.nextInt(100) < 50) activo.quitarEnergia(1);
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
            if (c.esPokemon() && c.getFase() == 1) {
                activo = c;
            } else {
                descarte.apilar(c);
            }
        }

        for (int i = 0; i < banca.length; i++) {
            while (banca[i] == null && !mazo.estaVacia()) {
                Carta c = mazo.desapilar();
                if (c.esPokemon() && c.getFase() == 1) {
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

    public boolean ponerPokemonEnTablero(Carta carta, int turno) {
        if (carta == null || !carta.esPokemon() || carta.getFase() != 1) return false;
        carta.prepararParaTablero(turno);
        if (activo == null) {
            activo = carta;
            return true;
        }
        for (int i = 0; i < banca.length; i++) {
            if (banca[i] == null) {
                banca[i] = carta;
                return true;
            }
        }
        return false;
    }

    public boolean asignarEnergia(int posicion) {
        Carta destino = posicion < 0 ? activo : getPokemonBanca(posicion);
        if (destino == null) return false;
        destino.agregarEnergia();
        return true;
    }

    public boolean puedeRetirarse() {
        return activo != null && !"Paralisis".equals(activo.getEstado())
                && !"Congelacion".equals(activo.getEstado());
    }

    public int costoRetirada() {
        return activo == null ? 0 : activo.getFase();
    }

    public boolean cambiarActivoConBanca(int indice) {
        if (indice < 0 || indice >= banca.length || banca[indice] == null) return false;
        Carta temp = activo;
        activo = banca[indice];
        banca[indice] = temp;
        return true;
    }

    public boolean retirarActivoConBanca(int indice) {
        if (!puedeRetirarse() || indice < 0 || indice >= banca.length || banca[indice] == null) return false;
        if (!activo.quitarEnergia(costoRetirada())) return false;
        Carta temp = activo;
        activo = banca[indice];
        banca[indice] = temp;
        return true;
    }

    public Carta evolucionSiguiente(Carta evolucion, int turno, boolean usarCaramelo) {
        if (evolucion == null || activo == null || !activo.puedeEvolucionar(turno) && !usarCaramelo) return null;
        if (!evolucion.getNombre().equalsIgnoreCase(nombreSiguiente(activo.getNombre()))) return null;
        Carta anterior = activo;
        activo = crearEvolucion(anterior, evolucion, turno);
        return anterior;
    }

    public Carta evolucionarBanca(int indice, Carta evolucion, int turno, boolean usarCaramelo) {
        if (indice < 0 || indice >= banca.length || banca[indice] == null || evolucion == null) return null;
        Carta anterior = banca[indice];
        if ((!anterior.puedeEvolucionar(turno) && !usarCaramelo)
                || !evolucion.getNombre().equalsIgnoreCase(nombreSiguiente(anterior.getNombre()))) return null;
        banca[indice] = crearEvolucion(anterior, evolucion, turno);
        return anterior;
    }

    private Carta crearEvolucion(Carta anterior, Carta evolucion, int turno) {
        double porcentaje = anterior.getVidaMaxima() == 0 ? 0
                : (double) anterior.getVida() / anterior.getVidaMaxima();
        evolucion.prepararParaTablero(turno);
        evolucion.registrarEvolucion(turno);
        evolucion.establecerEnergia(anterior.getEnergias());
        evolucion.recibirDanio(Math.max(0, evolucion.getVidaMaxima()
                - (int) Math.round(evolucion.getVidaMaxima() * porcentaje)));
        return evolucion;
    }

    private String nombreSiguiente(String nombre) {
        if (nombre.equals("Bulbasaur")) return "Ivysaur";
        if (nombre.equals("Ivysaur")) return "Venusaur";
        if (nombre.equals("Charmander")) return "Charmeleon";
        if (nombre.equals("Charmeleon")) return "Charizard";
        if (nombre.equals("Squirtle")) return "Wartortle";
        if (nombre.equals("Wartortle")) return "Blastoise";
        if (nombre.equals("Pichu")) return "Pikachu";
        if (nombre.equals("Pikachu")) return "Raichu";
        if (nombre.equals("Oddish")) return "Gloom";
        if (nombre.equals("Gloom")) return "Vileplume";
        if (nombre.equals("Magikarp")) return "Gyarados";
        return "";
    }

    public String procesarEstadoInicioTurno(Random random) {
        if (activo == null || !activo.tieneEstado()) return "";
        String estado = activo.getEstado();
        if (random.nextInt(100) < 50) {
            activo.curarEstado();
            return activo.getNombre() + " se curo de " + estado + ".";
        }
        activo.recibirDanioDeEstado();
        return activo.getNombre() + " sufre 10 de daño por " + estado + ".";
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

    public boolean promoverDesdeBanca(int indice) {
        if (indice < 0 || indice >= banca.length || banca[indice] == null || activo != null) return false;
        activo = banca[indice];
        banca[indice] = null;
        return true;
    }

    public boolean promoverDesdeMano(int indice, int turno) {
        Carta carta = obtenerCartaDeMano(indice);
        if (activo != null || carta == null || !carta.esPokemon() || carta.getFase() != 1) return false;
        carta = sacarCartaDeMano(indice);
        carta.prepararParaTablero(turno);
        activo = carta;
        return true;
    }

    public boolean promoverBasicoDelMazo(int turno) {
        if (activo != null) return false;
        Pila temporal = new Pila(30);
        Carta basico = null;
        while (!mazo.estaVacia()) {
            Carta carta = mazo.desapilar();
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                basico = carta;
                break;
            }
            temporal.apilar(carta);
        }
        while (!temporal.estaVacia()) mazo.apilar(temporal.desapilar());
        if (basico == null) return false;
        basico.prepararParaTablero(turno);
        activo = basico;
        return true;
    }

    public boolean tienePokemonDisponible() {
        if (activo != null) return true;
        for (int i = 0; i < banca.length; i++) {
            if (banca[i] != null) return true;
        }
        for (int i = 0; i < getTamanoMano(); i++) {
            Carta carta = obtenerCartaDeMano(i);
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) return true;
        }
        return hayPokemonBasicoEnMazo();
    }

    private boolean hayPokemonBasicoEnMazo() {
        Pila temporal = new Pila(30);
        boolean encontrado = false;
        while (!mazo.estaVacia()) {
            Carta carta = mazo.desapilar();
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) encontrado = true;
            temporal.apilar(carta);
        }
        while (!temporal.estaVacia()) mazo.apilar(temporal.desapilar());
        return encontrado;
    }

    public boolean promoverObligatoriamente(Scanner scanner) {
        int cantidadEnMano = 0;
        for (int i = 0; i < getTamanoMano(); i++) {
            Carta carta = obtenerCartaDeMano(i);
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                cantidadEnMano++;
            }
        }
        if (cantidadEnMano > 0) {
            System.out.println("Debes elegir un Pokemon de tu mano para ocupar la posicion activa.");
            for (int i = 0; i < getTamanoMano(); i++) {
                Carta carta = obtenerCartaDeMano(i);
                if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                    System.out.println((i + 1) + ". " + carta.getNombre());
                }
            }
            while (true) {
                System.out.print("Numero del Pokemon que entra como activo: ");
                try {
                    int opcion = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    Carta elegido = obtenerCartaDeMano(opcion);
                    if (elegido != null && elegido.esPokemon() && elegido.getFase() == 1) {
                        activo = sacarCartaDeMano(opcion);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // Se vuelve a solicitar una opcion valida.
                }
                System.out.println("Eleccion invalida: selecciona el numero de un Pokemon de fase 1 mostrado arriba.");
            }
        }
        for (int i = 0; i < banca.length; i++) {
            if (banca[i] != null) {
                activo = banca[i];
                banca[i] = null;
                return true;
            }
        }
        for (int i = 0; i < getTamanoMano(); i++) {
            Carta carta = obtenerCartaDeMano(i);
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                activo = sacarCartaDeMano(i);
                return true;
            }
        }
        while (!mazo.estaVacia()) {
            Carta carta = mazo.desapilar();
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                activo = carta;
                return true;
            }
            if (carta != null) descarte.apilar(carta);
        }
        return false;
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
                if (c != null && c.esPokemon() && c.getFase() == 1) {
                activo = c;
                return true;
            } else {
                descarte.apilar(c);
            }
        }
        return false;
    }
}