import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Logica principal del juego con seleccion de nombres, rotacion de campos y bonos
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // Registro dinamico de los nombres al inicio
        System.out.println("==========================================");
        System.out.println("   BIENVENIDO AL JUEGO DE CARTAS POKEMON  ");
        System.out.println("==========================================");
        System.out.print("Ingrese el nombre del Jugador 1: ");
        String nombreJ1 = scanner.nextLine().trim();
        if (nombreJ1.isEmpty()) nombreJ1 = "Jugador 1";

        System.out.print("Ingrese el nombre del Jugador 2: ");
        String nombreJ2 = scanner.nextLine().trim();
        if (nombreJ2.isEmpty()) nombreJ2 = "Jugador 2 (Bot)";

        // Carga de campos
        List<String> camposPosibles = new ArrayList<>();
        camposPosibles.add("Tierras de Fuego");
        camposPosibles.add("Bosque de Pasto");
        camposPosibles.add("Oceano de Agua");
        camposPosibles.add("Valle de Rayo");
        camposPosibles.add("Cueva de Tierra");
        Collections.shuffle(camposPosibles, random);

        ListaCircular campos = new ListaCircular();
        for (String campo : camposPosibles) {
            campos.agregar(campo);
        }

        // Mazos con 2 pociones y Pokemon (Tierra con menos vida)
        Pila mazo1 = new Pila(20);
        cargarMazoConDosPociones(mazo1, new Carta[] {
                new Carta("Bulbasaur", 60, 20),
                new Carta("Ivysaur", 80, 30),
                new Carta("Geodude", 40, 25), // Tipo Tierra (menos vida)
                new Carta("Pikachu", 50, 20),
                new Carta("Onix", 40, 30),    // Tipo Tierra (menos vida)
                new Carta("Pidgey", 40, 10),
                new Carta("Clefairy", 50, 10),
                new Carta("Oddish", 50, 15)
        });

        Pila mazo2 = new Pila(20);
        cargarMazoConDosPociones(mazo2, new Carta[] {
                new Carta("Charmander", 60, 20),
                new Carta("Charmeleon", 80, 30),
                new Carta("Cubone", 40, 20),  // Tipo Tierra (menos vida)
                new Carta("Squirtle", 60, 20),
                new Carta("Wartortle", 80, 30),
                new Carta("Magmar", 70, 30),
                new Carta("Gyarados", 100, 40),
                new Carta("Diglett", 40, 25)  // Tipo Tierra (menos vida)
        });

        Jugador jugador1 = new Jugador(nombreJ1, mazo1);
        Jugador jugador2 = new Jugador(nombreJ2, mazo2);

        // Se preparan las manos iniciales
        jugador1.robarCartaAMano();
        jugador1.robarCartaAMano();
        jugador2.robarCartaAMano();
        jugador2.robarCartaAMano();

        jugador1.prepararCampoInicial();
        jugador2.prepararCampoInicial();

        Cola turnos = new Cola(2);
        turnos.encolar(jugador1);
        turnos.encolar(jugador2);

        System.out.println("\n=== Simulacion de partida ===");
        int turno = 1;

        while (turno <= 12) {
            Jugador actual = turnos.desencolar();
            Jugador rival = turnos.desencolar();

            if (actual == null || rival == null) break;

            System.out.println("\n------------------------------------------------");
            System.out.println("Turno " + turno + " de " + actual.getNombre());
            System.out.println("Campo actual: " + campos.obtenerActual());

            actual.robarCartaAMano();

            // Menu interactivo para Jugador 1, automatico para Jugador 2
            if (actual == jugador1) {
                turnoJugadorHumano(scanner, actual);
            } else {
                turnoBot(actual);
            }

            if (actual.getActivo() == null) {
                actual.promoverSiguientePokemon();
            }

            if (actual.getActivo() == null || rival.getActivo() == null) {
                System.out.println("Falta un Pokemon activo para seguir el combate.");
                break;
            }

            // Calculo de dano
            int danio = calcularDanio(actual, rival, campos.obtenerActual());
            System.out.println(actual.getNombre() + " ataca con " + actual.getActivo().getNombre()
                    + " e inflige " + danio + " de dano a " + rival.getActivo().getNombre());

            rival.recibirDanio(danio);

            if (rival.activoFueraDeCombate()) {
                Carta caido = rival.sacarActivo();
                actual.sumarPunto();
                System.out.println(rival.getNombre() + " perdio a " + caido.getNombre());
                System.out.println(actual.getNombre() + " acumula " + actual.getPuntos() + " punto(s)");
                rival.promoverSiguientePokemon();
            }

            // El campo cambia CADA 3 TURNOS
            if (turno % 3 == 0) {
                campos.siguienteCampo();
                System.out.println("\n*** ¡EL CAMPO HA CAMBIADO A: " + campos.obtenerActual() + "! ***");
            }

            if (actual.getPuntos() >= 3 || rival.getPuntos() >= 3) {
                System.out.println("\n¡" + actual.getNombre() + " ha ganado la partida!");
                break;
            }

            turnos.encolar(actual);
            turnos.encolar(rival);
            turno++;
        }

        scanner.close();
    }

    private static void turnoJugadorHumano(Scanner scanner, Jugador actual) {
        System.out.println("\n=== SELECCIONE UNA ACCION ===");
        System.out.println("1. Cambiar Pokemon activo con la banca");
        System.out.println("2. Usar / Jugar carta de la mano");
        System.out.println("3. Omitir accion e ir a la fase de ataque");

        int accion = leerEntero(scanner, "Opcion: ");

        switch (accion) {
            case 1:
                System.out.println("Pokemon activo: " + actual.getActivo());
                for (int i = 0; i < 3; i++) {
                    System.out.println((i + 1) + ". Banca " + (i + 1) + " (" + actual.getPokemonBanca(i) + ")");
                }
                int idxBanca = leerEntero(scanner, "Selecciona slot de banca para cambiar (1-3): ") - 1;
                actual.cambiarActivoConBanca(idxBanca);
                break;

            case 2:
                if (actual.getTamanoMano() == 0) {
                    System.out.println("No tienes cartas en la mano.");
                    break;
                }
                actual.mostrarMano();
                int opcionMano = leerEntero(scanner, "Elige carta a jugar (1 a " + actual.getTamanoMano() + "): ");
                if (opcionMano > 0 && opcionMano <= actual.getTamanoMano()) {
                    int idx = opcionMano - 1;
                    Carta elegida = actual.obtenerCartaDeMano(idx);
                    if (elegida.esPocion()) {
                        actual.usarPocionEnActivo(idx);
                        System.out.println("Usaste una Pocion en tu Pokemon activo.");
                    } else {
                        for (int i = 0; i < 3; i++) {
                            if (actual.getPokemonBanca(i) == null) {
                                actual.ponerCartaEnBanca(elegida, i);
                                actual.sacarCartaDeMano(idx);
                                System.out.println("Pusiste a " + elegida.getNombre() + " en la banca " + (i + 1));
                                break;
                            }
                        }
                    }
                }
                break;

            default:
                System.out.println(actual.getNombre() + " no realizo cambios en la preparacion.");
                break;
        }
    }

    private static void turnoBot(Jugador bot) {
        System.out.println(bot.getNombre() + " esta tomando su turno...");

        // Usa pocion si esta herido
        for (int i = 0; i < bot.getTamanoMano(); i++) {
            Carta c = bot.obtenerCartaDeMano(i);
            if (c != null && c.esPocion()) {
                bot.usarPocionEnActivo(i);
                System.out.println(bot.getNombre() + " uso una Pocion.");
                break;
            }
        }

        // Coloca Pokemon en la banca disponible
        for (int i = 0; i < bot.getTamanoMano(); i++) {
            Carta c = bot.obtenerCartaDeMano(i);
            if (c != null && !c.esPocion()) {
                for (int b = 0; b < 3; b++) {
                    if (bot.getPokemonBanca(b) == null) {
                        bot.ponerCartaEnBanca(c, b);
                        bot.sacarCartaDeMano(i);
                        System.out.println(bot.getNombre() + " coloco a " + c.getNombre() + " en la banca.");
                        break;
                    }
                }
                break;
            }
        }
    }

    private static void cargarMazoConDosPociones(Pila mazo, Carta[] pokemones) {
        List<Carta> lista = new ArrayList<>();
        for (Carta p : pokemones) {
            lista.add(p);
        }
        lista.add(new Carta("Pocion", 20, 0, true));
        lista.add(new Carta("Pocion", 20, 0, true));

        Collections.shuffle(lista, new Random());

        for (Carta c : lista) {
            mazo.apilar(c);
        }
    }

    private static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine();
            try {
                return Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero valido.");
            }
        }
    }

    private static int calcularDanio(Jugador atacante, Jugador defensor, String campoActual) {
        Carta cartaAtacante = atacante.getActivo();
        Carta cartaDefensora = defensor.getActivo();

        if (cartaAtacante == null || cartaDefensora == null) return 0;

        String tipoAtacante = tipoDe(cartaAtacante.getNombre());
        String tipoDefensor = tipoDe(cartaDefensora.getNombre());

        int danio = cartaAtacante.getDanio();

        // 1. Bono por ventaja elemental directa (+10)
        if (tieneVentaja(tipoAtacante, tipoDefensor)) {
            danio += 10;
        }

        // 2. +10 de dano si el campo coincide con el tipo del atacante
        if (coincideCampo(tipoAtacante, campoActual)) {
            danio += 10;
        }

        // 3. Mas dano recibido si el atacante le hace counter al defensor en este campo (+10 extra)
        if (tieneVentaja(tipoAtacante, tipoDefensor) && coincideCampo(tipoAtacante, campoActual)) {
            danio += 10;
        }

        return Math.max(0, danio);
    }

    private static boolean coincideCampo(String tipo, String campoActual) {
        if (campoActual == null) return false;
        if (campoActual.contains("Fuego") && tipo.equals("Fuego")) return true;
        if (campoActual.contains("Pasto") && tipo.equals("Planta")) return true;
        if (campoActual.contains("Agua") && tipo.equals("Agua")) return true;
        if (campoActual.contains("Rayo") && tipo.equals("Rayo")) return true;
        if (campoActual.contains("Tierra") && tipo.equals("Tierra")) return true;
        return false;
    }

    private static boolean tieneVentaja(String atacante, String defensor) {
        if (atacante.equals("Planta") && (defensor.equals("Agua") || defensor.equals("Tierra"))) return true;
        if (atacante.equals("Agua") && (defensor.equals("Fuego") || defensor.equals("Tierra"))) return true;
        if (atacante.equals("Fuego") && defensor.equals("Planta")) return true;
        if (atacante.equals("Rayo") && defensor.equals("Agua")) return true;
        if (atacante.equals("Tierra") && (defensor.equals("Fuego") || defensor.equals("Rayo"))) return true;
        return false;
    }

    private static String tipoDe(String nombre) {
        if (nombre.equals("Bulbasaur") || nombre.equals("Ivysaur") || nombre.equals("Oddish")) return "Planta";
        if (nombre.equals("Charmander") || nombre.equals("Charmeleon") || nombre.equals("Magmar")) return "Fuego";
        if (nombre.equals("Squirtle") || nombre.equals("Wartortle") || nombre.equals("Gyarados")) return "Agua";
        if (nombre.equals("Pikachu") || nombre.equals("Raichu")) return "Rayo";
        if (nombre.equals("Geodude") || nombre.equals("Onix") || nombre.equals("Cubone") || nombre.equals("Diglett")) return "Tierra";
        return "Normal";
    }
}