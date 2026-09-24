import java.util.Random;

/**
 * Acciones sin Scanner ni System.out para conectar botones de una interfaz.
 */
public class AccionesGrafica {
    private final GestorPartida gestorPartida;
    private final Random random;

    public AccionesGrafica(GestorPartida gestorPartida, Random random) {
        this.gestorPartida = gestorPartida;
        this.random = random;
    }

    public boolean asignarEnergia(Jugador jugador, int posicion) {
        return jugador.asignarEnergia(posicion);
    }

    public boolean jugarPokemon(Jugador jugador, int indiceMano, int turno) {
        Carta carta = jugador.obtenerCartaDeMano(indiceMano);
        if (carta == null || !carta.esPokemon() || carta.getFase() != 1
                || !jugador.ponerPokemonEnTablero(carta, turno)) {
            return false;
        }
        jugador.sacarCartaDeMano(indiceMano);
        jugador.registrarJugada(carta);
        gestorPartida.registrar(jugador.getNombre() + " puso a " + carta.getNombre() + " en el tablero.");
        return true;
    }

    public boolean retirar(Jugador jugador, int banca) {
        return jugador.retirarActivoConBanca(banca);
    }

    public Carta evolucionar(Jugador jugador, int posicion, Carta evolucion,
            int turno, boolean caramelo) {
        return posicion < 0
                ? jugador.evolucionSiguiente(evolucion, turno, caramelo)
                : jugador.evolucionarBanca(posicion, evolucion, turno, caramelo);
    }

    public Carta usarCarameloRaro(Jugador jugador, int indiceCaramelo, int indiceEvolucion,
            int posicion, int turno) {
        Carta caramelo = jugador.obtenerCartaDeMano(indiceCaramelo);
        Carta evolucion = jugador.obtenerCartaDeMano(indiceEvolucion);
        if (caramelo == null || !caramelo.esCarta("Caramelo Raro") || evolucion == null) {
            return null;
        }

        Carta anterior = evolucionar(jugador, posicion, evolucion, turno, true);
        if (anterior == null) return null;

        jugador.sacarCartaDeMano(indiceEvolucion);
        int indiceCarameloActual = indiceCaramelo;
        if (indiceEvolucion < indiceCaramelo) indiceCarameloActual--;
        jugador.descartarCartaDeMano(indiceCarameloActual);
        jugador.registrarJugada(evolucion);
        gestorPartida.registrar(jugador.getNombre() + " uso Caramelo Raro para evolucionar "
                + anterior.getNombre() + " a " + evolucion.getNombre() + ".");
        return anterior;
    }

    public boolean usarPocion(Jugador jugador, int indiceMano) {
        return jugador.usarPocionEnActivo(indiceMano);
    }

    public boolean usarSuperPocion(Jugador jugador, int indiceMano) {
        return jugador.usarSuperPocion(indiceMano, random);
    }

    public boolean usarPokeball(Jugador jugador, int indiceMano) {
        return jugador.usarPokeball(indiceMano);
    }

    public int atacar(Jugador atacante, Jugador defensor) {
        Carta pokemonAtacante = atacante.getActivo();
        Carta pokemonDefensor = defensor.getActivo();
        if (pokemonAtacante == null || pokemonDefensor == null
                || pokemonAtacante.getEnergias() < pokemonAtacante.getFase()) {
            return -1;
        }

        int danio = pokemonAtacante.getDanio();
        if (tieneVentaja(pokemonAtacante.getTipo(), pokemonDefensor.getTipo())) danio += 10;
        if (coincideCampo(pokemonAtacante.getTipo(), gestorPartida.campoActual())) danio += 10;
        pokemonAtacante.quitarEnergia(pokemonAtacante.getFase());
        pokemonDefensor.recibirDanio(danio);
        atacante.registrarJugada(pokemonAtacante);
        gestorPartida.registrar(atacante.getNombre() + " ataco con " + pokemonAtacante.getNombre()
                + " por " + danio + " de dano.");
        return danio;
    }

    public Carta siguienteEvolucion(Carta pokemon) {
        return pokemon == null ? null : gestorPartida.siguienteEvolucion(pokemon.getNombre());
    }

    public ListaDoble historial() {
        return gestorPartida.getHistorial();
    }

    public String campoActual() {
        return gestorPartida.campoActual();
    }

    private boolean tieneVentaja(String atacante, String defensor) {
        return ("Planta".equals(atacante) && "Agua".equals(defensor))
                || ("Agua".equals(atacante) && "Fuego".equals(defensor))
                || ("Fuego".equals(atacante) && "Planta".equals(defensor))
                || ("Rayo".equals(atacante) && "Agua".equals(defensor));
    }

    private boolean coincideCampo(String tipo, String campo) {
        return (campo.contains("Fuego") && tipo.equals("Fuego"))
                || (campo.contains("Pasto") && tipo.equals("Planta"))
                || (campo.contains("Agua") && tipo.equals("Agua"))
                || (campo.contains("Rayo") && tipo.equals("Rayo"));
    }
}