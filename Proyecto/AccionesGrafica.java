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

    public boolean retirar(Jugador jugador, int banca) {
        return jugador.retirarActivoConBanca(banca);
    }

    public Carta evolucionar(Jugador jugador, int posicion, Carta evolucion,
            int turno, boolean caramelo) {
        return posicion < 0
                ? jugador.evolucionSiguiente(evolucion, turno, caramelo)
                : jugador.evolucionarBanca(posicion, evolucion, turno, caramelo);
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

    public Carta siguienteEvolucion(Carta pokemon) {
        return pokemon == null ? null : gestorPartida.siguienteEvolucion(pokemon.getNombre());
    }

    public ListaDoble historial() {
        return gestorPartida.getHistorial();
    }

    public String campoActual() {
        return gestorPartida.campoActual();
    }
}