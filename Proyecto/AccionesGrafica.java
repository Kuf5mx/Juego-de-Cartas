import java.util.Random;

/**
 * Acciones sin Scanner ni System.out para conectar botones de una interfaz.
 */
public class AccionesGrafica {
    private final GestorPartida gestorPartida;
    private final Random random;
    private String ultimoResumenAtaque = "";

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
        if (pokemonAtacante == null || pokemonDefensor == null) {
            ultimoResumenAtaque = "Ambos jugadores necesitan un Pokemon activo.";
            return -1;
        }
        if ("Paralisis".equals(pokemonAtacante.getEstado())
                || "Congelacion".equals(pokemonAtacante.getEstado())) {
            ultimoResumenAtaque = pokemonAtacante.getNombre() + " no puede atacar por su estado.";
            return -1;
        }
        if (pokemonAtacante.getEnergias() < pokemonAtacante.getFase()) {
            ultimoResumenAtaque = "Necesitas " + pokemonAtacante.getFase() + " energias para atacar.";
            return -1;
        }

        int danio = pokemonAtacante.getDanio();
        StringBuilder bonos = new StringBuilder();
        if (tieneVentaja(pokemonAtacante.getTipo(), pokemonDefensor.getTipo())) {
            danio += 10;
            agregarBono(bonos, "ventaja elemental +10");
        }
        if (coincideCampo(pokemonAtacante.getTipo(), gestorPartida.campoActual())) {
            danio += 10;
            agregarBono(bonos, "terreno favorable +10");
        }
        if (tieneDesventaja(pokemonDefensor.getTipo(), gestorPartida.campoActual())) {
            danio += 10;
            agregarBono(bonos, "terreno desfavorable +10");
        }
        if (pokemonAtacante.esCarta("Charizard")) {
            danio += 10;
            agregarBono(bonos, "Llamarada intensa +10");
        }
        if (pokemonAtacante.esCarta("Blastoise") && pokemonAtacante.getEnergias() >= 1) {
            danio += 10;
            agregarBono(bonos, "Hidrobomba +10");
        }
        if (pokemonAtacante.esCarta("Pikachu") && random.nextInt(100) < 50) {
            danio += 10;
            agregarBono(bonos, "Impactrueno +10");
        }
        if (pokemonAtacante.esCarta("Raichu")) {
            danio += 15;
            agregarBono(bonos, "Impactrueno fuerte +15");
        }
        if (pokemonAtacante.esCarta("Gyarados")
                && pokemonAtacante.getVida() <= pokemonAtacante.getVidaMaxima() / 2) {
            danio += 15;
            agregarBono(bonos, "Furia +15");
        }
        pokemonAtacante.quitarEnergia(pokemonAtacante.getFase());
        pokemonDefensor.recibirDanio(danio);
        atacante.registrarJugada(pokemonAtacante);
        gestorPartida.registrar(atacante.getNombre() + " ataco con " + pokemonAtacante.getNombre()
                + " por " + danio + " de dano.");
        ultimoResumenAtaque = pokemonAtacante.getNombre() + " hizo " + danio + " de dano"
                + (bonos.length() == 0 ? "." : " (" + bonos + ").");
        if (pokemonAtacante.tieneAtaqueElemental() && random.nextInt(100) < 40) {
            String estado = estadoDeTipo(pokemonAtacante.getTipo());
            pokemonDefensor.aplicarEstado(estado);
            ultimoResumenAtaque += " Aplico " + estado + ".";
        }
        return danio;
    }

    public String procesarHabilidadesInicioTurno(Jugador jugador, Jugador rival) {
        StringBuilder efectos = new StringBuilder();
        procesarHabilidad(jugador.getActivo(), rival, efectos);
        for (int i = 0; i < 3; i++) {
            procesarHabilidad(jugador.getPokemonBanca(i), rival, efectos);
        }
        return efectos.toString();
    }

    public String getUltimoResumenAtaque() {
        return ultimoResumenAtaque;
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

    private boolean tieneDesventaja(String tipo, String campo) {
        if (campo.contains("Fuego")) return tipo.equals("Planta");
        if (campo.contains("Pasto")) return tipo.equals("Agua");
        if (campo.contains("Agua")) return tipo.equals("Fuego");
        return campo.contains("Rayo") && tipo.equals("Agua");
    }

    private String estadoDeTipo(String tipo) {
        if ("Fuego".equals(tipo)) return "Quemadura";
        if ("Rayo".equals(tipo)) return "Paralisis";
        if ("Agua".equals(tipo)) return "Congelacion";
        return "Veneno";
    }

    private void procesarHabilidad(Carta carta, Jugador rival, StringBuilder efectos) {
        if (carta == null || carta.getHabilidad() == null) return;
        if (carta.esCarta("Venusaur")) {
            carta.curar(10);
            agregarBono(efectos, "Venusaur curo 10 de vida");
        } else if (carta.esCarta("Vileplume") && rival.getActivo() != null) {
            rival.getActivo().aplicarEstado("Veneno");
            agregarBono(efectos, "Vileplume aplico Veneno");
        } else if (carta.esCarta("Clefairy")) {
            carta.curar(10);
            carta.curarEstado();
            agregarBono(efectos, "Clefairy uso Canto");
        }
    }

    private void agregarBono(StringBuilder bonos, String bono) {
        if (bonos.length() > 0) bonos.append(", ");
        bonos.append(bono);
    }
}