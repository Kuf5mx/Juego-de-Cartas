import java.util.Random;
import java.util.Scanner;

public class MotorTurno {
    private final GestorPartida gestorPartida;
    private final Random random;
    private boolean partidaTerminada;
    private Jugador ganadorPartida;

        public MotorTurno(GestorPartida gestorPartida, Random random) {
        this.gestorPartida = gestorPartida;
        this.random = random;
    }

    public void jugarTurno(Scanner scanner, Jugador actual, Jugador rival, int turno, String campoActual) {
        System.out.println("\n================================================");
        System.out.println("Turno " + turno + " de " + actual.getNombre());
        System.out.println("Campo actual: " + campoActual);
        registrar("Turno " + turno + ": " + actual.getNombre() + " toma el turno con "
                + nombreSeguro(actual.getActivo()) + " como Pokemon activo.");
        String estado = actual.procesarEstadoInicioTurno(random);
        if (!estado.isEmpty()) {
            System.out.println(estado);
            registrar(actual.getNombre() + ": " + estado);
        }
        if (actual.activoFueraDeCombate()) {
            resolverDerrota(scanner, actual, rival);
            return;
        }
        if (actual.robarCartaAMano()) {
            System.out.println("Robo una carta.");
            registrar(actual.getNombre() + " robo una carta.");
        }

        ColaPrioridad habilidades = new ColaPrioridad(4);
        if (actual.getActivo() != null && actual.getActivo().getHabilidad() != null) {
            habilidades.encolar(actual.getActivo(), 1);
            procesarHabilidades(habilidades, actual, rival);
        }
        boolean energiaDisponible = true;
        boolean ataco = false;
        boolean terminar = false;
        while (!terminar) {
            mostrarEstado(actual, energiaDisponible, ataco);
            System.out.println("1. Asignar energia automatica");
            System.out.println("2. Jugar carta de la mano");
            System.out.println("3. Evolucionar Pokemon");
            System.out.println("4. Retirar activo pagando energia");
            System.out.println("5. Atacar");
            System.out.println("6. Terminar turno sin atacar");
            System.out.println("7. Consultar historial de la partida");
            int opcion = leerRango(scanner, "Selecciona una accion: ", 1, 7);
            switch (opcion) {
                case 1:
                    if (!energiaDisponible) System.out.println("Ya asignaste la energia de este turno.");
                    else energiaDisponible = !asignarEnergia(scanner, actual);
                    break;
                case 2: jugarCarta(scanner, actual, turno); break;
                case 3: evolucionarDesdeMano(scanner, actual, turno, false); break;
                case 4: retirarPokemon(scanner, actual); break;
                case 5:
                    if (ataco) System.out.println("Solo puedes atacar una vez por turno.");
                    else {
                        System.out.println("Advertencia: atacar sera tu ultima accion del turno.");
                        if (atacar(actual, rival, campoActual)) {
                            ataco = true;
                            if (rival.activoFueraDeCombate()) resolverDerrota(scanner, rival, actual);
                            System.out.println("Ataque realizado: es la ultima accion de este turno.");
                            registrar(actual.getNombre() + " termino su turno despues de atacar.");
                            terminar = true;
                        }
                    }
                    break;
                case 7:
                    gestorPartida.mostrarHistorial(scanner);
                    break;
                default:
                    if (energiaDisponible) System.out.println("La energia no asignada se pierde.");
                    terminar = true;
                    break;
            }
            if (actual.getPuntos() >= 3 || rival.getPuntos() >= 3) terminar = true;
        }
    }

    public boolean partidaTerminada() {
        return partidaTerminada;
    }

    public Jugador getGanadorPartida() {
        return ganadorPartida;
    }

    private void mostrarEstado(Jugador jugador, boolean energiaDisponible, boolean ataco) {
        System.out.println("\nActivo: " + jugador.getActivo());
        for (int i = 0; i < 3; i++) System.out.println("Banca " + (i + 1) + ": " + jugador.getPokemonBanca(i));
        System.out.println("Energia del turno: " + (energiaDisponible ? "sin asignar" : "asignada o perdida"));
        System.out.println("Ataque: " + (ataco ? "realizado" : "disponible"));
    }

    private boolean asignarEnergia(Scanner scanner, Jugador jugador) {
        System.out.println("0. Activo");
        for (int i = 0; i < 3; i++) System.out.println((i + 1) + ". Banca " + (i + 1));
        int opcion = leerRango(scanner, "Destino: ", 0, 3);
        boolean resultado = jugador.asignarEnergia(opcion == 0 ? -1 : opcion - 1);
        System.out.println(resultado ? "Energia asignada." : "Ese espacio no tiene Pokemon.");
        if (resultado) {
            Carta destino = opcion == 0 ? jugador.getActivo() : jugador.getPokemonBanca(opcion - 1);
            jugador.registrarJugada(destino);
            registrar(jugador.getNombre() + " asigno una energia a " + nombreSeguro(destino) + ".");
        }
        return resultado;
    }

    private void jugarCarta(Scanner scanner, Jugador jugador, int turno) {
        if (jugador.getTamanoMano() == 0) { System.out.println("No tienes cartas."); return; }
        jugador.mostrarMano();
        int opcion = leerRango(scanner, "Carta a jugar (0 cancela): ", 0, jugador.getTamanoMano());
        if (opcion == 0) return;
        int indice = opcion - 1;
        Carta carta = jugador.obtenerCartaDeMano(indice);
        if (carta.esPokemon()) {
            if (carta.getFase() != 1) {
                System.out.println("No puedes colocar a " + carta.getNombre() + ": necesita su fase anterior.");
                return;
            }
            if (jugador.ponerPokemonEnTablero(carta, turno)) {
                jugador.sacarCartaDeMano(indice);
                jugador.registrarJugada(carta);
                System.out.println(carta.getNombre() + " entro al tablero.");
                registrar(jugador.getNombre() + " puso a " + carta.getNombre() + " en el tablero.");
            } else System.out.println("No hay espacio en el tablero.");
            return;
        }
        try {
            if (carta.esCarta("Pocion") && jugador.usarPocionEnActivo(indice)) {
                registrar(jugador.getNombre() + " uso una Pocion y la envio al descarte.");
            } else if (carta.esCarta("Superpocion") && jugador.usarSuperPocion(indice, random)) {
                registrar(jugador.getNombre() + " uso una Superpocion y la envio al descarte.");
            } else if (carta.esCarta("Pokeball") && jugador.usarPokeball(indice)) {
                registrar(jugador.getNombre() + " uso una Pokeball para buscar un Pokemon de primera fase.");
            } else if (carta.esCarta("Caramelo Raro")) {
                evolucionarDesdeMano(scanner, jugador, turno, true);
            } else {
                System.out.println("No puedes usar este objeto ahora.");
            }
        } catch (ListaVaciaException e) { System.out.println(e.getMessage()); }
    }

    private void evolucionarDesdeMano(Scanner scanner, Jugador jugador, int turno, boolean caramelo) {
        System.out.println("0. Activo");
        for (int i = 0; i < 3; i++) System.out.println((i + 1) + ". Banca " + (i + 1));
        int posicion = leerRango(scanner, "Pokemon a evolucionar: ", 0, 3);
        Carta objetivo = posicion == 0 ? jugador.getActivo() : jugador.getPokemonBanca(posicion - 1);
        if (objetivo == null) { System.out.println("No hay un Pokemon en esa posicion."); return; }
        Carta siguienteCarta = gestorPartida.siguienteEvolucion(objetivo.getNombre());
        if (siguienteCarta == null) { System.out.println(objetivo.getNombre() + " no tiene una evolucion registrada."); return; }
        int indice = buscarEnMano(jugador, siguienteCarta.getNombre());
        if (indice < 0) {
            System.out.println("No puedes evolucionar: necesitas tener a " + siguienteCarta.getNombre() + " en la mano.");
            return;
        }
        Carta evolucion = jugador.obtenerCartaDeMano(indice);
        Carta anterior = posicion == 0 ? jugador.evolucionSiguiente(evolucion, turno, caramelo)
                : jugador.evolucionarBanca(posicion - 1, evolucion, turno, caramelo);
        if (anterior == null) { System.out.println("Todavia no puedes evolucionar en este turno."); return; }
        jugador.sacarCartaDeMano(indice);
        jugador.registrarJugada(evolucion);
        if (caramelo) {
            int indiceCaramelo = buscarEnMano(jugador, "Caramelo Raro");
            if (indiceCaramelo >= 0) jugador.descartarCartaDeMano(indiceCaramelo);
        }
        System.out.println(anterior.getNombre() + " evoluciono a " + evolucion.getNombre() + ".");
        registrar(jugador.getNombre() + " evoluciono a " + anterior.getNombre() + " hacia " + evolucion.getNombre() + ".");
    }

    private void retirarPokemon(Scanner scanner, Jugador jugador) {
        if (jugador.getActivo() == null) return;
        System.out.println("Retirarse cuesta " + jugador.costoRetirada() + " energia.");
        for (int i = 0; i < 3; i++) System.out.println((i + 1) + ". Banca " + (i + 1));
        int posicion = leerRango(scanner, "Banca de destino (0 cancela): ", 0, 3);
        if (posicion > 0 && jugador.retirarActivoConBanca(posicion - 1)) {
            System.out.println("Retirada realizada.");
            registrar(jugador.getNombre() + " retiro a su Pokemon activo pagando energia.");
        } else if (posicion > 0) {
            System.out.println("No puedes retirarte: revisa banca, estado y energia.");
        }
    }

    private boolean atacar(Jugador atacante, Jugador defensor, String campoActual) {
        Carta pokemon = atacante.getActivo();
        if (pokemon == null || defensor.getActivo() == null) {
            System.out.println("No puedes atacar: ambos jugadores necesitan tener un Pokemon activo.");
            return false;
        }
        if ("Paralisis".equals(pokemon.getEstado()) || "Congelacion".equals(pokemon.getEstado())) {
            System.out.println(pokemon.getNombre() + " no puede atacar por su estado.");
            return false;
        }
        int costoAtaque = pokemon.getFase();
        if (pokemon.getEnergias() < costoAtaque) {
            System.out.println("No tienes suficiente energia para atacar: necesita " + costoAtaque + ".");
            return false;
        }
        int danio = pokemon.getDanio();
        StringBuilder bonos = new StringBuilder();
        if (tieneVentaja(pokemon.getTipo(), defensor.getActivo().getTipo())) { danio += 10; bonos.append("ventaja elemental +10"); }
        if (coincideCampo(pokemon.getTipo(), campoActual)) { danio += 10; agregarBono(bonos, "campo favorable +10"); }
        if (tieneDesventaja(defensor.getActivo().getTipo(), campoActual)) { danio += 10; agregarBono(bonos, "campo desfavorable +10"); }
        int bonoBanca = modificadorBancaDefensa(defensor, pokemon.getTipo());
        if (bonoBanca > 0) {
            danio = Math.max(0, danio - bonoBanca);
            agregarBono(bonos, "banca defensiva -" + bonoBanca);
        }
        if (pokemon.esCarta("Charizard")) danio += 10;
        if (pokemon.esCarta("Blastoise") && pokemon.getEnergias() >= 1) danio += 10;
        if (pokemon.esCarta("Pikachu") && random.nextInt(100) < 50) danio += 10;
        if (pokemon.esCarta("Raichu")) danio += 15;
        if (pokemon.esCarta("Gyarados") && pokemon.getVida() <= pokemon.getVidaMaxima() / 2) danio += 15;
        defensor.recibirDanio(danio);
        atacante.registrarJugada(pokemon);
        System.out.println(pokemon.getNombre() + " uso " + pokemon.getAtaque() + " e hizo " + danio + " de daño.");
        if (bonos.length() > 0) System.out.println("Bonos aplicados: " + bonos + ".");
        registrar(atacante.getNombre() + " ataco con " + pokemon.getNombre() + " por " + danio + " de daño.");
        if (pokemon.tieneAtaqueElemental() && random.nextInt(100) < 40) {
            String estado = estadoDeTipo(pokemon.getTipo());
            defensor.getActivo().aplicarEstado(estado);
            System.out.println("El ataque aplico " + estado + ".");
            registrar(pokemon.getNombre() + " aplico " + estado + " a " + defensor.getActivo().getNombre() + ".");
        }
        return true;
    }

    private void procesarHabilidades(ColaPrioridad cola, Jugador jugador, Jugador rival) {
        while (!cola.estaVacia()) {
            Carta carta = cola.desencolar();
            if (carta.esCarta("Venusaur")) {
                carta.curar(10);
                System.out.println("Venusaur recupero 10 de vida.");
            } else if (carta.esCarta("Vileplume") && rival.getActivo() != null) {
                rival.getActivo().aplicarEstado("Veneno");
                System.out.println("Vileplume aplico Veneno al activo rival.");
            } else if (carta.esCarta("Clefairy")) {
                carta.curar(10);
                carta.curarEstado();
                System.out.println("Clefairy uso Canto y recupero 10 de vida.");
            } else if (carta.esCarta("Gyarados") && carta.getVida() <= carta.getVidaMaxima() / 2) {
                System.out.println("Gyarados activo: Furia aumentara su ataque con poca vida.");
            } else if (carta.esCarta("Charizard")) {
                System.out.println("Charizard activo: Llamarada intensa costara 3 energias y hara daño adicional.");
            } else if (carta.esCarta("Blastoise")) {
                System.out.println("Blastoise activo: Hidrobomba recibe daño adicional con energia de sobra.");
            } else if (carta.esCarta("Pikachu")) {
                System.out.println("Pikachu activo: Impactrueno puede recibir daño adicional.");
            } else if (carta.esCarta("Raichu")) {
                System.out.println("Raichu activo: Impactrueno fuerte hara daño adicional por 2 energias.");
            }
            System.out.println("Habilidad especial prioritaria de " + carta.getNombre() + " procesada.");
        }
    }

    private void resolverDerrota(Scanner scanner, Jugador derrotado, Jugador ganador) {
        Carta caido = derrotado.sacarActivo();
        if (caido == null) return;
        ganador.sumarPunto();
        derrotado.getDescarte().apilar(caido);
        registrar(caido.getNombre() + " quedo fuera de combate. " + ganador.getNombre() + " gano un punto.");
        if (derrotado.promoverObligatoriamente(scanner)) {
            registrar(derrotado.getNombre() + " coloco un nuevo Pokemon como activo.");
        } else {
            ganadorPartida = ganador;
            partidaTerminada = true;
        }
    }

    private void agregarBono(StringBuilder bonos, String bono) {
        if (bonos.length() > 0) bonos.append(", ");
        bonos.append(bono);
    }

    private void registrar(String evento) { gestorPartida.registrar(evento); }
    private String nombreSeguro(Carta carta) { return carta == null ? "ningun Pokemon" : carta.getNombre(); }

    private int buscarEnMano(Jugador jugador, String nombre) {
        for (int i = 0; i < jugador.getTamanoMano(); i++) {
            if (jugador.obtenerCartaDeMano(i).esCarta(nombre)) return i;
        }
        return -1;
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
        if (campo.contains("Rayo")) return tipo.equals("Agua");
        return false;
    }

    private int modificadorBancaDefensa(Jugador defensor, String tipoAtacante) {
        if (defensor == null || defensor.getActivo() == null) return 0;
        int total = 0;
        for (int i = 0; i < 3; i++) {
            Carta apoyo = defensor.getPokemonBanca(i);
            if (apoyo == null) continue;
            String tipoApoyo = apoyo.getTipo();
            if (tieneVentaja(tipoApoyo, tipoAtacante)) {
                total += 5;
            }
        }
        if (total > 15) total = 15;
        return total;
    }

    private String estadoDeTipo(String tipo) {
        if ("Fuego".equals(tipo)) return "Quemadura";
        if ("Rayo".equals(tipo)) return "Paralisis";
        if ("Agua".equals(tipo)) return "Congelacion";
        return "Veneno";
    }

    private int leerRango(Scanner scanner, String mensaje, int minimo, int maximo) {
        while (true) {
            System.out.print(mensaje);
            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor >= minimo && valor <= maximo) return valor;
            } catch (NumberFormatException e) {
                // Se vuelve a solicitar una opcion valida.
            }
            System.out.println("Escoge un numero entre " + minimo + " y " + maximo + ".");
        }
    }
}
