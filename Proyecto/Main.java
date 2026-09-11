import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final int MAX_CARTAS_MAZO = 15;
    private static final Random RANDOM = new Random();
    private static final List<String> RESUMEN_PARTIDA = new ArrayList<>();
    private static boolean partidaTerminada;
    private static Jugador ganadorPartida;
    private static final String[] CAMPOS = {"Llanura de Fuego", "Bosque de Pasto", "Oceano de Agua", "Valle de Rayo"};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, Carta> catalogo = crearCatalogo();
        System.out.println("==========================================");
        System.out.println("   BIENVENIDO AL JUEGO DE CARTAS POKEMON  ");
        System.out.println("==========================================");
        String nombre1 = leerNombre(scanner, "Ingrese el nombre del Jugador 1: ", "Jugador 1");
        String nombre2 = leerNombre(scanner, "Ingrese el nombre del Jugador 2: ", "Jugador 2");
        Jugador jugador1 = new Jugador(nombre1, new Pila(30));
        Jugador jugador2 = new Jugador(nombre2, new Pila(30));
        partidaTerminada = false;
        ganadorPartida = null;
        Carta[] cartas = catalogo.values().toArray(new Carta[0]);
        personalizarMazo(scanner, jugador1, cartas);
        personalizarMazo(scanner, jugador2, cartas);
        jugador1.robarCartasIniciales(4);
        jugador2.robarCartasIniciales(4);
        jugador1.elegirCampoInicial(scanner);
        jugador2.elegirCampoInicial(scanner);

        Cola turnos = new Cola(2);
        if (RANDOM.nextBoolean()) { turnos.encolar(jugador1); turnos.encolar(jugador2); }
        else { turnos.encolar(jugador2); turnos.encolar(jugador1); }
        int turno = 1;
        while (!partidaTerminada && jugador1.getPuntos() < 3 && jugador2.getPuntos() < 3) {
            Jugador actual = turnos.desencolar();
            if (actual == null) break;
            Jugador rival = actual == jugador1 ? jugador2 : jugador1;
            String campoActual = CAMPOS[(turno - 1) / 3 % CAMPOS.length];
            jugarTurno(scanner, actual, rival, turno, campoActual);
            if (actual.getPuntos() < 3 && rival.getPuntos() < 3) turnos.encolar(actual);
            turno++;
        }
        System.out.println("\n=== Fin de la partida ===");
        Jugador ganador = null;
        if (ganadorPartida != null) ganador = ganadorPartida;
        else if (jugador1.getPuntos() >= 3) ganador = jugador1;
        else if (jugador2.getPuntos() >= 3) ganador = jugador2;

        if (ganador != null) {
            System.out.println("Ganador: " + ganador.getNombre() + " con " + ganador.getPuntos() + " punto(s).");
            registrar("La partida termino con " + ganador.getNombre() + " como ganador con "
                    + ganador.getPuntos() + " puntos.");
        } else {
            System.out.println("La partida termino sin ganador: ningun jugador alcanzo 3 puntos.");
            registrar("La partida termino sin ganador porque ningun jugador alcanzo 3 puntos.");
        }
        System.out.print("\n¿Quieres escuchar el resumen narrado de la partida? (1=Si, 2=No): ");
        if (leerRango(scanner, "", 1, 2) == 1) mostrarResumenPartida();
        scanner.close();
    }

    private static void jugarTurno(Scanner scanner, Jugador actual, Jugador rival, int turno, String campoActual) {
        System.out.println("\n================================================");
        System.out.println("Turno " + turno + " de " + actual.getNombre());
        System.out.println("Campo actual: " + campoActual);
        registrar("Turno " + turno + ": " + actual.getNombre() + " toma el turno con "
                + nombreSeguro(actual.getActivo()) + " como Pokemon activo.");
        String estado = actual.procesarEstadoInicioTurno(RANDOM);
        if (!estado.isEmpty()) {
            System.out.println(estado);
            registrar(actual.getNombre() + ": " + estado);
        }
        if (actual.activoFueraDeCombate()) { resolverDerrota(scanner, actual, rival); return; }
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
            int opcion = leerRango(scanner, "Selecciona una accion: ", 1, 6);
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
                default:
                    if (energiaDisponible) System.out.println("La energia no asignada se pierde.");
                    terminar = true;
                    break;
            }
            if (actual.getPuntos() >= 3 || rival.getPuntos() >= 3) terminar = true;
        }
    }

    private static void mostrarEstado(Jugador jugador, boolean energiaDisponible, boolean ataco) {
        System.out.println("\nActivo: " + jugador.getActivo());
        for (int i = 0; i < 3; i++) System.out.println("Banca " + (i + 1) + ": " + jugador.getPokemonBanca(i));
        System.out.println("Energia del turno: " + (energiaDisponible ? "sin asignar" : "asignada o perdida"));
        System.out.println("Ataque: " + (ataco ? "realizado" : "disponible"));
    }

    private static boolean asignarEnergia(Scanner scanner, Jugador jugador) {
        System.out.println("0. Activo");
        for (int i = 0; i < 3; i++) System.out.println((i + 1) + ". Banca " + (i + 1));
        int opcion = leerRango(scanner, "Destino: ", 0, 3);
        boolean resultado = jugador.asignarEnergia(opcion == 0 ? -1 : opcion - 1);
        System.out.println(resultado ? "Energia asignada." : "Ese espacio no tiene Pokemon.");
        if (resultado) registrar(jugador.getNombre() + " asigno una energia a "
            + nombreSeguro(opcion == 0 ? jugador.getActivo() : jugador.getPokemonBanca(opcion - 1)) + ".");
        return resultado;
    }

    private static void jugarCarta(Scanner scanner, Jugador jugador, int turno) {
        if (jugador.getTamanoMano() == 0) { System.out.println("No tienes cartas."); return; }
        jugador.mostrarMano();
        int opcion = leerRango(scanner, "Carta a jugar (0 cancela): ", 0, jugador.getTamanoMano());
        if (opcion == 0) return;
        int indice = opcion - 1;
        Carta carta = jugador.obtenerCartaDeMano(indice);
        if (carta.esPokemon()) {
            if (carta.getFase() != 1) {
                System.out.println("No puedes colocar a " + carta.getNombre() + " directamente: es fase "
                        + carta.getFase() + ". Coloca primero su fase anterior y evolucionalo desde el tablero.");
                return;
            }
            if (jugador.ponerPokemonEnTablero(carta, turno)) {
                jugador.sacarCartaDeMano(indice);
                System.out.println(carta.getNombre() + " entro al tablero.");
                registrar(jugador.getNombre() + " puso a " + carta.getNombre() + " en el tablero.");
            } else System.out.println("No hay espacio en el tablero.");
            return;
        }
        try {
            if (carta.esCarta("Pocion") && jugador.usarPocionEnActivo(indice)) {
                System.out.println("Pocion usada y enviada al descarte.");
                registrar(jugador.getNombre() + " uso una Pocion y la envio al descarte.");
            } else if (carta.esCarta("Superpocion") && jugador.usarSuperPocion(indice, RANDOM)) {
                System.out.println("Superpocion usada y enviada al descarte.");
                registrar(jugador.getNombre() + " uso una Superpocion y la envio al descarte.");
            } else if (carta.esCarta("Pokeball") && jugador.usarPokeball(indice)) {
                System.out.println("Pokeball usada: busco solo primera fase.");
                registrar(jugador.getNombre() + " uso una Pokeball para buscar un Pokemon de primera fase.");
            } else if (carta.esCarta("Caramelo Raro")) {
                evolucionarDesdeMano(scanner, jugador, turno, true);
            } else if (carta.esCarta("Pocion") && jugador.getActivo() != null
                    && jugador.getActivo().getVida() >= jugador.getActivo().getVidaMaxima()) {
                System.out.println("No puedes usar la Pocion: tu Pokemon activo tiene la vida completa. Elige un Pokemon danado.");
            } else if (carta.esCarta("Superpocion") && jugador.getActivo() != null
                    && jugador.getActivo().getVida() >= jugador.getActivo().getVidaMaxima()) {
                System.out.println("No puedes usar la Superpocion: tu Pokemon activo tiene la vida completa. Elige un Pokemon danado.");
            } else {
                System.out.println("No puedes usar este objeto ahora. Revisa que tengas un objetivo valido y que se cumpla su efecto.");
            }
        } catch (ListaVaciaException e) { System.out.println(e.getMessage()); }
    }

    private static void evolucionarDesdeMano(Scanner scanner, Jugador jugador, int turno, boolean caramelo) {
        System.out.println("0. Activo");
        for (int i = 0; i < 3; i++) System.out.println((i + 1) + ". Banca " + (i + 1));
        int posicion = leerRango(scanner, "Pokemon a evolucionar: ", 0, 3);
        Carta objetivo = posicion == 0 ? jugador.getActivo() : jugador.getPokemonBanca(posicion - 1);
        if (objetivo == null) {
            System.out.println("No hay un Pokemon en esa posicion. Elige el activo o una banca ocupada.");
            return;
        }
        String siguiente = siguienteDe(objetivo.getNombre());
        if (siguiente.isEmpty()) {
            System.out.println(objetivo.getNombre() + " no tiene una evolucion registrada.");
            return;
        }
        int indice = buscarEnMano(jugador, siguiente);
        if (indice < 0) {
            System.out.println("No puedes evolucionar: necesitas tener a " + siguiente + " en la mano.");
            return;
        }
        Carta evolucion = jugador.obtenerCartaDeMano(indice);
        Carta anterior = posicion == 0 ? jugador.evolucionSiguiente(evolucion, turno, caramelo)
                : jugador.evolucionarBanca(posicion - 1, evolucion, turno, caramelo);
        if (anterior == null) {
            if (!caramelo) {
                System.out.println("Todavia no puedes evolucionar: el Pokemon debe esperar un turno completo. Intentalo en el siguiente turno o usa Caramelo Raro.");
            } else {
                System.out.println("El Caramelo Raro no pudo usarse: la carta de tu mano no corresponde a la siguiente fase.");
            }
            return;
        }
        jugador.sacarCartaDeMano(indice);
        if (caramelo) {
            int indiceCaramelo = buscarEnMano(jugador, "Caramelo Raro");
            if (indiceCaramelo >= 0) jugador.descartarCartaDeMano(indiceCaramelo);
        }
        System.out.println(anterior.getNombre() + " evoluciono a " + evolucion.getNombre() + ".");
        registrar(jugador.getNombre() + " evoluciono a " + anterior.getNombre() + " hacia "
            + evolucion.getNombre() + ".");
    }

    private static void retirarPokemon(Scanner scanner, Jugador jugador) {
        if (jugador.getActivo() == null) return;
        System.out.println("Retirarse cuesta " + jugador.costoRetirada() + " energia.");
        for (int i = 0; i < 3; i++) System.out.println((i + 1) + ". Banca " + (i + 1));
        int posicion = leerRango(scanner, "Banca de destino (0 cancela): ", 0, 3);
        if (posicion > 0 && jugador.retirarActivoConBanca(posicion - 1)) {
            System.out.println("Retirada realizada.");
            registrar(jugador.getNombre() + " retiro a su Pokemon activo pagando energia.");
        }
        else if (posicion > 0) {
            System.out.println("No puedes retirarte: necesitas una banca ocupada, no estar paralizado/congelado y tener "
                + jugador.costoRetirada() + " energia.");
        }
    }

    private static boolean atacar(Jugador atacante, Jugador defensor, String campoActual) {
        Carta pokemon = atacante.getActivo();
        if (pokemon == null || defensor.getActivo() == null) {
            System.out.println("No puedes atacar: ambos jugadores necesitan tener un Pokemon activo.");
            return false;
        }
        if ("Paralisis".equals(pokemon.getEstado()) || "Congelacion".equals(pokemon.getEstado())) {
            System.out.println(pokemon.getNombre() + " no puede atacar por su estado. Espera a curarte o usa una evolucion que elimine el estado."); return false;
        }
        int costoAtaque = pokemon.getFase();
        if (pokemon.getEnergias() < costoAtaque) {
            System.out.println("No tienes suficiente energia para atacar: " + pokemon.getNombre()
                    + " necesita " + costoAtaque + " energia por estar en fase " + pokemon.getFase() + ". Asigna energia en este turno o espera al siguiente.");
            return false;
        }
        int danio = pokemon.getDanio();
        StringBuilder bonos = new StringBuilder();
        if (tieneVentaja(pokemon.getTipo(), defensor.getActivo().getTipo())) {
            danio += 10;
            bonos.append("ventaja elemental +10");
        }
        if (coincideCampo(pokemon.getTipo(), campoActual)) {
            danio += 10;
            if (bonos.length() > 0) bonos.append(", ");
            bonos.append("campo favorable +10");
        }
        if (tieneDesventaja(defensor.getActivo().getTipo(), campoActual)) {
            danio += 10;
            if (bonos.length() > 0) bonos.append(", ");
            bonos.append("campo desfavorable +10");
        }
        if (pokemon.esCarta("Charizard")) danio += 10;
        if (pokemon.esCarta("Blastoise") && pokemon.getEnergias() >= 1) danio += 10;
        if (pokemon.esCarta("Pikachu") && RANDOM.nextInt(100) < 50) danio += 10;
        if (pokemon.esCarta("Raichu")) danio += 15;
        if (pokemon.esCarta("Gyarados") && pokemon.getVida() <= pokemon.getVidaMaxima() / 2) danio += 15;
        defensor.recibirDanio(danio);
        System.out.println(pokemon.getNombre() + " uso " + pokemon.getAtaque() + " e hizo " + danio + " de daño.");
        if (bonos.length() > 0) System.out.println("Bonos aplicados: " + bonos + ".");
        registrar(atacante.getNombre() + " ataco con " + pokemon.getNombre() + " usando "
            + pokemon.getAtaque() + " por " + danio + " de daño.");
        if (pokemon.tieneAtaqueElemental() && RANDOM.nextInt(100) < 40) {
            String estado = estadoDeTipo(pokemon.getTipo());
            defensor.getActivo().aplicarEstado(estado);
            System.out.println("El ataque aplico " + estado + ".");
            registrar(pokemon.getNombre() + " aplico " + estado + " a " + defensor.getActivo().getNombre() + ".");
        }
        return true;
    }

    private static void procesarHabilidades(ColaPrioridad cola, Jugador jugador, Jugador rival) {
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

    private static void resolverDerrota(Scanner scanner, Jugador derrotado, Jugador ganador) {
        Carta caido = derrotado.sacarActivo();
        if (caido == null) return;
        ganador.sumarPunto();
        derrotado.getDescarte().apilar(caido);
        System.out.println(caido.getNombre() + " quedo en 0 de vida. Punto para " + ganador.getNombre() + ".");
        registrar(caido.getNombre() + " quedo fuera de combate. " + ganador.getNombre() + " gano un punto.");
        if (derrotado.promoverObligatoriamente(scanner)) {
            System.out.println(derrotado.getNombre() + " debe colocar a " + derrotado.getActivo().getNombre()
                    + " en la posicion activa.");
            registrar(derrotado.getNombre() + " coloco obligatoriamente a "
                    + derrotado.getActivo().getNombre() + " como nuevo activo.");
        } else {
            System.out.println(derrotado.getNombre() + " no tiene mas Pokemon disponibles.");
            System.out.println("La partida termina y gana " + ganador.getNombre() + ".");
            registrar(derrotado.getNombre() + " no pudo colocar otro Pokemon. "
                    + ganador.getNombre() + " gano la partida.");
                ganadorPartida = ganador;
            partidaTerminada = true;
        }
    }

    private static void registrar(String evento) {
        RESUMEN_PARTIDA.add(evento);
    }

    private static String nombreSeguro(Carta carta) {
        return carta == null ? "ningun Pokemon" : carta.getNombre();
    }

    private static void mostrarResumenPartida() {
        System.out.println("\n================ RESUMEN NARRADO ================");
        System.out.println("La partida comienza y los entrenadores preparan sus equipos.");
        for (String evento : RESUMEN_PARTIDA) System.out.println("- " + evento);
        System.out.println("==================================================");
    }

    private static int buscarEnMano(Jugador jugador, String nombre) {
        for (int i = 0; i < jugador.getTamanoMano(); i++)
            if (jugador.obtenerCartaDeMano(i).esCarta(nombre)) return i;
        return -1;
    }

    private static String estadoDeTipo(String tipo) {
        if ("Fuego".equals(tipo)) return "Quemadura";
        if ("Rayo".equals(tipo)) return "Paralisis";
        if ("Agua".equals(tipo)) return "Congelacion";
        return "Veneno";
    }

    private static boolean tieneVentaja(String atacante, String defensor) {
        return ("Planta".equals(atacante) && "Agua".equals(defensor))
                || ("Agua".equals(atacante) && "Fuego".equals(defensor))
                || ("Fuego".equals(atacante) && "Planta".equals(defensor))
                || ("Rayo".equals(atacante) && "Agua".equals(defensor));
    }

    private static boolean coincideCampo(String tipo, String campo) {
        return (campo.contains("Fuego") && tipo.equals("Fuego"))
                || (campo.contains("Pasto") && tipo.equals("Planta"))
                || (campo.contains("Agua") && tipo.equals("Agua"))
                || (campo.contains("Rayo") && tipo.equals("Rayo"));
    }

    private static boolean tieneDesventaja(String tipoPokemon, String campo) {
        if (campo.contains("Fuego")) return tipoPokemon.equals("Planta");
        if (campo.contains("Pasto")) return tipoPokemon.equals("Agua");
        if (campo.contains("Agua")) return tipoPokemon.equals("Fuego");
        if (campo.contains("Rayo")) return tipoPokemon.equals("Agua");
        return false;
    }

    private static String siguienteDe(String nombre) {
        String[] a = {"Bulbasaur", "Ivysaur", "Charmander", "Charmeleon", "Squirtle", "Wartortle", "Pichu", "Pikachu", "Oddish", "Gloom", "Magikarp"};
        String[] b = {"Ivysaur", "Venusaur", "Charmeleon", "Charizard", "Wartortle", "Blastoise", "Pikachu", "Raichu", "Gloom", "Vileplume", "Gyarados"};
        for (int i = 0; i < a.length; i++) if (a[i].equals(nombre)) return b[i];
        return "";
    }

    private static Map<String, Carta> crearCatalogo() {
        Map<String, Carta> c = new HashMap<>();
        pokemon(c, "Bulbasaur", 60, 20, "Planta", 1, "Latigo Cepa", true, null);
        pokemon(c, "Ivysaur", 80, 30, "Planta", 2, "Latigo Cepa", true, null);
        pokemon(c, "Venusaur", 120, 50, "Planta", 3, "Rayo Solar", true, "Curacion natural");
        pokemon(c, "Charmander", 60, 20, "Fuego", 1, "Ascuas", true, null);
        pokemon(c, "Charmeleon", 80, 35, "Fuego", 2, "Ascuas", true, null);
        pokemon(c, "Charizard", 130, 55, "Fuego", 3, "Llamarada", true, "Llamarada intensa");
        pokemon(c, "Squirtle", 60, 20, "Agua", 1, "Rayo Burbuja", true, null);
        pokemon(c, "Wartortle", 85, 30, "Agua", 2, "Rayo Burbuja", true, null);
        pokemon(c, "Blastoise", 130, 50, "Agua", 3, "Hidrobomba", true, "Hidrobomba");
        pokemon(c, "Pichu", 40, 10, "Rayo", 1, "Impactrueno", true, null);
        pokemon(c, "Pikachu", 60, 25, "Rayo", 2, "Impactrueno", true, "Impactrueno");
        pokemon(c, "Raichu", 100, 45, "Rayo", 3, "Impactrueno", true, "Impactrueno fuerte");
        pokemon(c, "Oddish", 50, 15, "Planta", 1, "Latigo", false, null);
        pokemon(c, "Gloom", 70, 25, "Planta", 2, "Mordisco", false, null);
        pokemon(c, "Vileplume", 100, 40, "Planta", 3, "Polvo Veneno", true, "Esporas");
        pokemon(c, "Magikarp", 40, 5, "Agua", 1, "Placaje", false, null);
        pokemon(c, "Gyarados", 110, 45, "Agua", 2, "Mordisco", false, "Furia");
        pokemon(c, "Pidgey", 40, 10, "Normal", 1, "Picotazo", false, null);
        pokemon(c, "Clefairy", 50, 10, "Normal", 1, "Mordisco", false, "Canto");
        pokemon(c, "Magmar", 70, 30, "Fuego", 1, "Mordisco", false, null);
        return c;
    }

    private static void pokemon(Map<String, Carta> c, String n, int v, int d, String t, int f, String a, boolean e, String h) {
        c.put(n, new Carta(n, v, d, false, t, f, a, e, h));
    }

    private static void mostrarArbol(Map<String, Carta> catalogo) {
        ArbolEvolucion arbol = new ArbolEvolucion(catalogo.get("Bulbasaur"));
        arbol.agregarEvolucion("Bulbasaur", catalogo.get("Ivysaur"));
        arbol.agregarEvolucion("Ivysaur", catalogo.get("Venusaur"));
        System.out.println("Arbol de evolucion cargado:");
        arbol.recorrer();
    }

    private static void personalizarMazo(Scanner scanner, Jugador jugador, Carta[] catalogo) {
        List<Carta> cartas = new ArrayList<>();
        for (int i = 0; i < 6; i++) cartas.add(clonar(catalogo[indicePokemonPrimeraFase(catalogo)]));
        cartas.add(new Carta("Pocion", 20, 0, true));
        cartas.add(new Carta("Pokeball", 0, 0, true));
        cartas.add(new Carta("Superpocion", 40, 0, true));
        cartas.add(new Carta("Caramelo Raro", 0, 0, true));
        boolean listo = false;
        while (!listo) {
            System.out.println("\nMazo de " + jugador.getNombre() + ": " + cartas.size() + "/15");
            System.out.println("1 Agregar Pokemon por fase  2 Pocion (cura 20)  3 Pokeball (busca fase 1)");
            System.out.println("4 Superpocion (cura 40, 50% quita energia)  5 Caramelo Raro (evoluciona sin esperar)");
            System.out.println("6 Quitar  7 Listo");
            int opcion = leerRango(scanner, "Selecciona: ", 1, 7);
            if (opcion == 1 && cartas.size() < MAX_CARTAS_MAZO) {
                Carta pokemon = seleccionarPokemonPorFase(scanner, catalogo);
                if (pokemon != null) cartas.add(clonar(pokemon));
            } else if (opcion == 2 && cartas.size() < MAX_CARTAS_MAZO) cartas.add(new Carta("Pocion", 20, 0, true));
            else if (opcion == 3 && cartas.size() < MAX_CARTAS_MAZO) cartas.add(new Carta("Pokeball", 0, 0, true));
            else if (opcion == 4 && cartas.size() < MAX_CARTAS_MAZO) cartas.add(new Carta("Superpocion", 40, 0, true));
            else if (opcion == 5 && cartas.size() < MAX_CARTAS_MAZO) cartas.add(new Carta("Caramelo Raro", 0, 0, true));
            else if (opcion == 6 && !cartas.isEmpty()) cartas.remove(cartas.size() - 1);
            else if (opcion == 7) {
                if (cartas.stream().filter(Carta::esPokemon).count() < 4) System.out.println("Necesitas al menos 4 Pokemon.");
                else listo = true;
            } else if (cartas.size() >= MAX_CARTAS_MAZO) System.out.println("El mazo ya tiene 15 cartas.");
        }
        Collections.shuffle(cartas);
        for (Carta carta : cartas) jugador.getMazo().apilar(carta);
    }

    private static Carta seleccionarPokemonPorFase(Scanner scanner, Carta[] catalogo) {
        System.out.println("\nFase del Pokemon:");
        System.out.println("1. Fase 1 (Pokemon basico)");
        System.out.println("2. Fase 2 (evoluciona de fase 1)");
        System.out.println("3. Fase 3 (evoluciona de fase 2)");
        int fase = leerRango(scanner, "Selecciona la fase: ", 1, 3);
        List<Carta> opciones = new ArrayList<>();
        for (Carta carta : catalogo) {
            if (carta.esPokemon() && carta.getFase() == fase) opciones.add(carta);
        }
        System.out.println("\nPokemon disponibles de fase " + fase + ":");
        for (int i = 0; i < opciones.size(); i++) {
            Carta carta = opciones.get(i);
            String origen = fase == 1 ? "basico" : "evoluciona de " + evolucionAnterior(carta.getNombre());
            System.out.println((i + 1) + ". " + carta.getNombre() + " (" + origen + ")");
        }
        int seleccion = leerRango(scanner, "Pokemon (0 cancela): ", 0, opciones.size());
        return seleccion == 0 ? null : opciones.get(seleccion - 1);
    }

    private static String evolucionAnterior(String nombre) {
        String[] evoluciones = {"Ivysaur", "Venusaur", "Charmeleon", "Charizard", "Wartortle", "Blastoise",
                "Pikachu", "Raichu", "Gloom", "Vileplume", "Gyarados"};
        String[] anteriores = {"Bulbasaur", "Ivysaur", "Charmander", "Charmeleon", "Squirtle", "Wartortle",
                "Pichu", "Pikachu", "Oddish", "Gloom", "Magikarp"};
        for (int i = 0; i < evoluciones.length; i++) {
            if (evoluciones[i].equals(nombre)) return anteriores[i];
        }
        return "ninguno";
    }

    private static Carta clonar(Carta carta) {
        return new Carta(carta.getNombre(), carta.getVidaMaxima(), carta.getDanio(), false, carta.getTipo(),
                carta.getFase(), carta.getAtaque(), carta.tieneAtaqueElemental(), carta.getHabilidad());
    }

    private static int indicePokemonPrimeraFase(Carta[] catalogo) {
        List<Integer> candidatos = new ArrayList<>();
        for (int i = 0; i < catalogo.length; i++) {
            if (catalogo[i].esPokemon() && catalogo[i].getFase() == 1) candidatos.add(i);
        }
        return candidatos.get(RANDOM.nextInt(candidatos.size()));
    }

    private static String leerNombre(Scanner scanner, String mensaje, String defecto) {
        System.out.print(mensaje);
        String nombre = scanner.nextLine().trim();
        return nombre.isEmpty() ? defecto : nombre;
    }

    private static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Ingresa un numero valido."); }
        }
    }

    private static int leerRango(Scanner scanner, String mensaje, int minimo, int maximo) {
        while (true) {
            int valor = leerEntero(scanner, mensaje);
            if (valor >= minimo && valor <= maximo) return valor;
            System.out.println("Escoge un numero entre " + minimo + " y " + maximo + ".");
        }
    }
}