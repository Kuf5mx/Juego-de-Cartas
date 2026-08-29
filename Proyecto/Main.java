import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Prueba principal de todas las estructuras
 */
public class Main {

    public static void main(String[] args) {
        // Scanner para leer la consola y random para aleatorios
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        // Creamos y mostramos el catalogo base
        ListaSimple catalogo = new ListaSimple();
        catalogo.agregar(new Carta("Bulbasaur", 60, 20));
        catalogo.agregar(new Carta("Charmander", 60, 20));
        catalogo.agregar(new Carta("Squirtle", 60, 20));

        System.out.println("=== Catalogo de cartas ===");
        catalogo.mostrar();

        // Creamos la lista circular con los campos de batalla desordenados
        List<String> camposPosibles = new ArrayList<>();
        camposPosibles.add("Pasto");
        camposPosibles.add("Fuego");
        camposPosibles.add("Agua");
        camposPosibles.add("Rayo");
        Collections.shuffle(camposPosibles, random);

        ListaCircular campos = new ListaCircular();
        for (String campo : camposPosibles) {
            campos.agregar(campo);
        }

        // Creamos y llenamos el mazo inicial del jugador 1
        Pila mazo1 = new Pila(20);
        cargarMazoAleatorio(mazo1, new Carta[] {
                new Carta("Bulbasaur", 60, 20),
                new Carta("Ivysaur", 80, 30),
                new Carta("Venusaur", 100, 40),
                new Carta("Pikachu", 50, 20),
                new Carta("Raichu", 80, 30),
                new Carta("Pidgey", 40, 10),
                new Carta("Clefairy", 50, 10),
                new Carta("Oddish", 50, 15),
                new Carta("Poliwag", 50, 15),
                new Carta("Eevee", 60, 20)
        });

        // Creamos y llenamos el mazo inicial del jugador 2
        Pila mazo2 = new Pila(20);
        cargarMazoAleatorio(mazo2, new Carta[] {
                new Carta("Charmander", 60, 20),
                new Carta("Charmeleon", 80, 30),
                new Carta("Charizard", 110, 40),
                new Carta("Squirtle", 60, 20),
                new Carta("Wartortle", 80, 30),
                new Carta("Magmar", 70, 30),
                new Carta("Gyarados", 100, 40),
                new Carta("Psyduck", 50, 15),
                new Carta("Vulpix", 50, 15),
                new Carta("Magikarp", 30, 5)
        });

        // Creamos las instancias de los dos jugadores
        Jugador jugador1 = new Jugador("Joseph", mazo1);
        Jugador jugador2 = new Jugador("Cepi", mazo2);

        // Permitimos que ambos editen sus mazos antes de iniciar
        editarMazo(scanner, jugador1.getNombre(), mazo1);
        editarMazo(scanner, jugador2.getNombre(), mazo2);

        // Preparamos la cola de turnos con orden aleatorio
        Cola turnos = new Cola(2);
        if (random.nextBoolean()) {
            turnos.encolar(jugador1);
            turnos.encolar(jugador2);
            System.out.println("Inicia el combate: " + jugador1.getNombre());
        } else {
            turnos.encolar(jugador2);
            turnos.encolar(jugador1);
            System.out.println("Inicia el combate: " + jugador2.getNombre());
        }

        // Llenamos y mostramos la mano inicial
        System.out.println("\n=== Mano de Joseph ===");
        jugador1.llenarMano();
        jugador1.mostrarMano();

        System.out.println("\n=== Mano de Cepi ===");
        jugador2.llenarMano();
        jugador2.mostrarMano();

        // Sacamos las cartas del mazo para el tablero
        jugador1.prepararCampoInicial();
        jugador2.prepararCampoInicial();

        System.out.println("\n=== Campo inicial de Joseph ===");
        jugador1.mostrarCampo();

        System.out.println("\n=== Campo inicial de Cepi ===");
        jugador2.mostrarCampo();

        System.out.println("\n=== Simulacion de partida ===");
        int turno = 1;

        // Bucle principal de la batalla (maximo 12 turnos)
        while (turno <= 12) {
            // Sacamos al jugador del turno actual y al rival
            Jugador actual = turnos.desencolar();
            Jugador rival = turnos.desencolar();

            if (actual == null || rival == null) {
                break;
            }

            System.out.println("\nTurno " + turno + " de " + actual.getNombre());
            System.out.println("Campo: " + campos.obtenerActual());

            // Fases iniciales del turno: robar y llenar mano
            actual.robarCartaAMano();
            actual.llenarMano();
            actual.mostrarMano();

            // Menu para cambiar de pokemon activo con la banca
            System.out.println("Pokemon activo de " + actual.getNombre() + ": " + actual.getActivo());
            System.out.println("1. Mantener activo");
            for (int i = 0; i < 3; i++) {
                System.out.println((i + 2) + ". Cambiar activo con banca " + (i + 1) + " (" + actual.getPokemonBanca(i) + ")");
            }
            int opcionActivo = leerEntero(scanner, "Elige el pokemon que quieres usar: ");
            if (opcionActivo >= 2 && opcionActivo <= 4) {
                boolean cambio = actual.cambiarActivoConBanca(opcionActivo - 2);
                if (cambio) {
                    System.out.println("Ahora el activo de " + actual.getNombre() + " es " + actual.getActivo());
                } else {
                    System.out.println("Esa banca esta vacia, se mantiene el mismo activo.");
                }
            }

            // Opcion para usar o descartar cartas de la mano
            System.out.println("Elige una carta de tu mano");
            System.out.println("1. Carta 1");
            System.out.println("2. Carta 2");
            System.out.println("3. No jugar carta");
            int opcionMano = leerEntero(scanner, "Que carta quieres usar: ");

            if (opcionMano == 1 || opcionMano == 2) {
                int indiceMano = opcionMano - 1;
                Carta cartaElegida = actual.obtenerCartaDeMano(indiceMano);

                if (cartaElegida == null) {
                    System.out.println("Ahi no tienes carta.");
                } else if (cartaElegida.esPocion()) {
                    // Si es pocion cura al activo
                    if (actual.getActivo() == null) {
                        System.out.println("No tienes un pokemon activo para curar.");
                    } else {
                        actual.usarPocionEnActivo(indiceMano);
                        System.out.println("Se uso " + cartaElegida.getNombre() + ", ahora "
                                + actual.getActivo().getNombre() + " tiene " + actual.getActivo().getVida() + " de vida");
                    }
                } else {
                    // Si es pokemon se ubica en campo o banca
                    List<Integer> destinosValidos = new ArrayList<>();
                    System.out.println("Elige donde poner la carta:");
                    if (actual.getActivo() == null) {
                        System.out.println("1. Poner en activo");
                        destinosValidos.add(1);
                    }
                    for (int i = 0; i < 3; i++) {
                        if (actual.getPokemonBanca(i) == null) {
                            System.out.println((i + 2) + ". Poner en banca " + (i + 1));
                            destinosValidos.add(i + 2);
                        }
                    }
                    System.out.println("5. Descartar");
                    destinosValidos.add(5);

                    int destino = leerEntero(scanner, "Donde quieres poner la carta: ");

                    if (!destinosValidos.contains(destino)) {
                        System.out.println("Esa opcion no esta disponible, se descarta la carta.");
                        actual.descartarCartaDeMano(indiceMano);
                    } else if (destino == 5) {
                        actual.descartarCartaDeMano(indiceMano);
                        System.out.println("Se descarto " + cartaElegida.getNombre());
                    } else if (destino == 1) {
                        actual.ponerCartaEnActivo(cartaElegida);
                        actual.sacarCartaDeMano(indiceMano);
                        System.out.println(cartaElegida.getNombre() + " ahora es el activo de " + actual.getNombre());
                    } else {
                        int indiceBanca = destino - 2;
                        actual.ponerCartaEnBanca(cartaElegida, indiceBanca);
                        actual.sacarCartaDeMano(indiceMano);
                        System.out.println(cartaElegida.getNombre() + " se puso en la banca " + (indiceBanca + 1));
                    }
                }
            } else {
                System.out.println(actual.getNombre() + " no jugo carta");
            }

            // Si el activo murio o no hay, promueve uno nuevo
            if (actual.getActivo() == null) {
                actual.promoverSiguientePokemon();
            }

            // Si no hay mas pokemon en campo la partida se detiene
            if (actual.getActivo() == null || rival.getActivo() == null) {
                System.out.println("Falta un pokemon activo para seguir");
                break;
            }

            // Calculo y ejecucion del ataque
            int danio = calcularDanio(actual, rival, campos.obtenerActual());
            System.out.println(actual.getNombre() + " ataca con " + actual.getActivo().getNombre()
                    + " y hace " + danio + " de dano");

            rival.recibirDanio(danio);

            // Verificamos si el activo rival cayo
            if (rival.activoFueraDeCombate()) {
                Carta caido = rival.sacarActivo();
                actual.sumarPunto();
                System.out.println(rival.getNombre() + " perdio a " + caido.getNombre());
                System.out.println(actual.getNombre() + " tiene " + actual.getPuntos() + " punto(s)");

                rival.promoverSiguientePokemon();
            }

            // Rotamos el campo cada 2 turnos
            if (turno % 2 == 0) {
                campos.siguienteCampo();
                System.out.println("Cambio de campo a: " + campos.obtenerActual());
            }

            // Condiciones de victoria
            if (actual.getPuntos() >= 3) {
                System.out.println("\n" + actual.getNombre() + " gano la partida con 3 puntos");
                break;
            }

            if (rival.getPuntos() >= 3) {
                System.out.println("\n" + rival.getNombre() + " gano la partida con 3 puntos");
                break;
            }

            // Regresamos ambos jugadores a la cola en orden
            turnos.encolar(actual);
            turnos.encolar(rival);
            turno++;
        }

        // Mostramos el estado final del descarte
        System.out.println("\n=== Descarte de Joseph ===");
        jugador1.mostrarDescarte();

        System.out.println("\n=== Descarte de Cepi ===");
        jugador2.mostrarDescarte();

        scanner.close();
    }

    // Permite modificar el mazo agregando o quitando cartas
    private static void editarMazo(Scanner scanner, String nombreJugador, Pila mazo) {
        System.out.println("\n=== Editar mazo de " + nombreJugador + " ===");
        boolean editando = true;

        while (editando) {
            mazo.mostrarConNumeros();
            System.out.println("1. Agregar una pocion (cura 20 de vida)");
            System.out.println("2. Quitar una carta del mazo");
            System.out.println("3. Terminar edicion");
            int opcion = leerEntero(scanner, nombreJugador + ", que quieres hacer: ");

            if (opcion == 1) {
                mazo.apilar(new Carta("Pocion", 20, 0, true));
                System.out.println("Se agrego una Pocion al mazo.");
            } else if (opcion == 2) {
                int numero = leerEntero(scanner, "Numero de la carta que quieres quitar: ");
                Carta quitada = mazo.quitarPorIndice(numero);
                if (quitada != null) {
                    System.out.println("Se quito: " + quitada);
                } else {
                    System.out.println("Ese numero no es valido.");
                }
            } else {
                editando = false;
            }
        }
    }

    // Lee un entero de la consola capturando errores de texto
    private static int leerEntero(Scanner scanner, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine();

            try {
                return Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                System.out.println("Pon un numero valido.");
            }
        }
    }

    // Calcula el dano total combinando tipo bonos de banca y campo
    private static int calcularDanio(Jugador atacante, Jugador defensor, String campoActual) {
        Carta cartaAtacante = atacante.getActivo();
        Carta cartaDefensora = defensor.getActivo();

        if (cartaAtacante == null || cartaDefensora == null) {
            return 0;
        }

        String tipoAtacante = tipoDe(cartaAtacante.getNombre());
        String tipoDefensor = tipoDe(cartaDefensora.getNombre());

        int danio = cartaAtacante.getDanio();

        if (tieneVentaja(tipoAtacante, tipoDefensor)) {
            danio = danio + 10;
        }

        danio = danio + bonoPorCampo(tipoAtacante, campoActual);
        danio = danio + bonoPorBanca(tipoAtacante, defensor);

        if (danio < 0) {
            danio = 0;
        }

        return danio;
    }

    // Calcula el bono si el atacante tiene ventaja sobre cartas de la banca rival
    private static int bonoPorBanca(String tipoAtacante, Jugador defensor) {
        int bono = 0;

        for (int i = 0; i < 3; i++) {
            Carta cartaBanca = defensor.getPokemonBanca(i);
            if (cartaBanca != null && tieneVentaja(tipoAtacante, tipoDe(cartaBanca.getNombre()))) {
                bono = bono + 10;
            }
        }

        return bono;
    }

    // Calcula bonos o penalizaciones segun el campo activo
    private static int bonoPorCampo(String tipoAtacante, String campoActual) {
        if (campoActual == null) {
            return 0;
        }

        if (campoActual.equals("Pasto")) {
            if (tipoAtacante.equals("Planta")) {
                return 10;
            }
            if (tipoAtacante.equals("Agua")) {
                return -10;
            }
        }

        if (campoActual.equals("Fuego")) {
            if (tipoAtacante.equals("Fuego")) {
                return 10;
            }
            if (tipoAtacante.equals("Planta")) {
                return -10;
            }
        }

        if (campoActual.equals("Agua")) {
            if (tipoAtacante.equals("Agua")) {
                return 10;
            }
            if (tipoAtacante.equals("Fuego")) {
                return -10;
            }
        }

        if (campoActual.equals("Rayo")) {
            if (tipoAtacante.equals("Rayo")) {
                return 10;
            }
            if (tipoAtacante.equals("Agua")) {
                return -10;
            }
        }

        return 0;
    }

    // Evalua si un tipo de pokemon le gana a otro
    private static boolean tieneVentaja(String tipoAtacante, String tipoDefensor) {
        if (tipoAtacante.equals("Planta") && tipoDefensor.equals("Agua")) {
            return true;
        }

        if (tipoAtacante.equals("Agua") && tipoDefensor.equals("Fuego")) {
            return true;
        }

        if (tipoAtacante.equals("Fuego") && tipoDefensor.equals("Planta")) {
            return true;
        }

        if (tipoAtacante.equals("Rayo") && tipoDefensor.equals("Agua")) {
            return true;
        }

        if (tipoAtacante.equals("Normal") && tipoDefensor.equals("Normal")) {
            return true;
        }

        return false;
    }

    // Asigna el tipo elemental buscando por el nombre de la carta
    private static String tipoDe(String nombre) {
        if (nombre.equals("Bulbasaur") || nombre.equals("Ivysaur") || nombre.equals("Venusaur")) {
            return "Planta";
        }

        if (nombre.equals("Charmander") || nombre.equals("Charmeleon") || nombre.equals("Charizard")
                || nombre.equals("Vulpix") || nombre.equals("Magmar")) {
            return "Fuego";
        }

        if (nombre.equals("Squirtle") || nombre.equals("Wartortle") || nombre.equals("Blastoise")
                || nombre.equals("Magikarp") || nombre.equals("Gyarados") || nombre.equals("Psyduck")) {
            return "Agua";
        }

        if (nombre.equals("Pikachu") || nombre.equals("Raichu")) {
            return "Rayo";
        }

        return "Normal";
    }

    // Mezcla las cartas recibidas y las apila en el mazo
    private static void cargarMazoAleatorio(Pila mazo, Carta[] cartas) {
        List<Carta> cartasAleatorias = new ArrayList<>();

        for (Carta carta : cartas) {
            cartasAleatorias.add(carta);
        }

        Collections.shuffle(cartasAleatorias, new Random());

        for (Carta carta : cartasAleatorias) {
            mazo.apilar(carta);
        }
    }
}