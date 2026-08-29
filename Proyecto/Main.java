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
        if (nombreJ2.isEmpty()) nombreJ2 = "Jugador 2";

        // Carga de campos
        List<String> camposPosibles = new ArrayList<>();
        camposPosibles.add("Llanura de Fuego");
        camposPosibles.add("Bosque de Pasto");
        camposPosibles.add("Oceano de Agua");
        camposPosibles.add("Valle de Rayo");
        Collections.shuffle(camposPosibles, random);

        ListaCircular campos = new ListaCircular();
        for (String campo : camposPosibles) {
            campos.agregar(campo);
        }

        // Mazos base sin pociones
        Pila mazo1 = new Pila(30);
        Carta[] pokemones1 = new Carta[] {
                new Carta("Bulbasaur", 60, 20),
                new Carta("Ivysaur", 80, 30),
                new Carta("Pikachu", 50, 20),
                new Carta("Pidgey", 40, 10),
                new Carta("Clefairy", 50, 10),
                new Carta("Oddish", 50, 15)
        };

        Pila mazo2 = new Pila(30);
        Carta[] pokemones2 = new Carta[] {
                new Carta("Charmander", 60, 20),
                new Carta("Charmeleon", 80, 30),
                new Carta("Squirtle", 60, 20),
                new Carta("Wartortle", 80, 30),
                new Carta("Magmar", 70, 30),
                new Carta("Gyarados", 100, 40)
        };

        Jugador jugador1 = new Jugador(nombreJ1, mazo1);
        Jugador jugador2 = new Jugador(nombreJ2, mazo2);

        // Crear mazos aleatorios y permitir ajustes opcionales
        personalizarMazo(scanner, jugador1, pokemones1);
        personalizarMazo(scanner, jugador2, pokemones2);

        // Se preparan las manos iniciales
        jugador1.robarCartasIniciales(4);
        jugador2.robarCartasIniciales(4);

        jugador1.elegirCampoInicial(scanner);
        jugador2.elegirCampoInicial(scanner);

        // Decidir al azar quién comienza
        Jugador primerJugador = random.nextBoolean() ? jugador1 : jugador2;
        Jugador segundoJugador = (primerJugador == jugador1) ? jugador2 : jugador1;

        Cola turnos = new Cola(2);
        turnos.encolar(primerJugador);
        turnos.encolar(segundoJugador);

        System.out.println("\n=== Simulacion de partida ===");
        System.out.println("¡" + primerJugador.getNombre() + " comienza el juego!");
        int turno = 1;

        while (turno <= 20) {
            Jugador actual = turnos.desencolar();
            if (actual == null) break;
            
            // El rival es el otro jugador
            Jugador rival = (actual == jugador1) ? jugador2 : jugador1;

            System.out.println("\n================================================");
            System.out.println("Turno " + turno + " de " + actual.getNombre());
            System.out.println("Campo actual: " + campos.obtenerActual());

            if (actual.robarCartaAMano()) {
                System.out.println(actual.getNombre() + " robo una carta.");
            }
            System.out.println("Mano actual:");
            actual.mostrarMano();

            // Menu interactivo para ambos jugadores
            turnoJugador(scanner, actual);

            if (actual.getActivo() == null) {
                actual.promoverSiguientePokemon();
            }

            if (actual.getActivo() == null || rival.getActivo() == null) {
                System.out.println("Falta un Pokemon activo para seguir el combate.");
                break;
            }

            // Calculo de dano
            int danio = calcularDanio(actual, rival, campos.obtenerActual());
            String tipoAtaque = tipoDe(actual.getActivo().getNombre());
            String tipoDefensa = tipoDe(rival.getActivo().getNombre());
            
            System.out.println("\n--- ATAQUE ---");
            System.out.println(actual.getNombre() + " (" + tipoAtaque + ") ataca con " + actual.getActivo().getNombre()
                    + " a " + rival.getActivo().getNombre() + " (" + tipoDefensa + ")");
            System.out.println("   Danio infligido: " + danio);
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
                System.out.println("\n=== ¡" + actual.getNombre() + " ha ganado la partida! ===");
                System.out.println("Puntos finales: " + actual.getNombre() + " = " + actual.getPuntos() + " | " + rival.getNombre() + " = " + rival.getPuntos());
                break;
            }

            // Volver a encolar al jugador
            turnos.encolar(actual);
            turno++;
        }

        scanner.close();
    }

    private static void personalizarMazo(Scanner scanner, Jugador jugador, Carta[] pokemones) {
        System.out.println("\n=== PERSONALIZAR MAZO DE " + jugador.getNombre().toUpperCase() + " ===");
        System.out.println("Pokemones disponibles:");
        for (int i = 0; i < pokemones.length; i++) {
            System.out.println((i + 1) + ". " + pokemones[i]);
        }

        List<Carta> mazoPersonalizado = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Carta base = pokemones[new Random().nextInt(pokemones.length)];
            mazoPersonalizado.add(new Carta(base.getNombre(), base.getVida(), base.getDanio()));
        }

        System.out.println("\nSe genero un mazo aleatorio de 10 cartas.");
        System.out.println("Puedes agregar o quitar cartas antes de comenzar (maximo 15).");
        boolean mazoListo = false;
        
        while (!mazoListo) {
            System.out.println("\nCartas en mazo: " + mazoPersonalizado.size() + "/15");
            System.out.println("1. Agregar Pokemon");
            System.out.println("2. Agregar Pocion");
            System.out.println("3. Agregar Pokeballs");
            System.out.println("4. Quitar carta");
            System.out.println("5. Comenzar con este mazo");
            
            int opcion = leerEnteroEnRango(scanner, "Selecciona opcion: ", 1, 5);
            
            switch(opcion) {
                case 1:
                    if (mazoPersonalizado.size() < 15) {
                        int idxPoke = leerEnteroEnRango(scanner, "Selecciona Pokemon (1-" + pokemones.length + "): ", 1, pokemones.length);
                        mazoPersonalizado.add(pokemones[idxPoke - 1]);
                        System.out.println("Agregado: " + pokemones[idxPoke - 1].getNombre());
                    } else {
                        System.out.println("Mazo lleno (15 cartas).");
                    }
                    break;
                case 2:
                    if (mazoPersonalizado.size() < 15) {
                        mazoPersonalizado.add(new Carta("Pocion", 20, 0, true));
                        System.out.println("Agregada: Pocion (Cura 20 de vida)");
                    } else {
                        System.out.println("Mazo lleno (15 cartas).");
                    }
                    break;
                case 3:
                    if (mazoPersonalizado.size() < 15) {
                        mazoPersonalizado.add(new Carta("Pokeball", 1, 0, true));
                        System.out.println("Agregada: Pokeball (Da un Pokemon en mano)");
                    } else {
                        System.out.println("Mazo lleno (15 cartas).");
                    }
                    break;
                case 4:
                    if (!mazoPersonalizado.isEmpty()) {
                        System.out.println("Cartas en mazo:");
                        for (int i = 0; i < mazoPersonalizado.size(); i++) {
                            System.out.println((i + 1) + ". " + mazoPersonalizado.get(i).getNombre());
                        }
                        int idxQuitar = leerEnteroEnRango(scanner, "Selecciona carta a quitar (1-" + mazoPersonalizado.size() + "): ", 1, mazoPersonalizado.size()) - 1;
                        Carta quitada = mazoPersonalizado.remove(idxQuitar);
                        System.out.println("Quitada: " + quitada.getNombre());
                    } else {
                        System.out.println("No hay cartas para quitar.");
                    }
                    break;
                case 5:
                    long numPokemones = mazoPersonalizado.stream().filter(c -> !c.esPocion()).count();
                    if (numPokemones < 4) {
                        System.out.println("Necesitas al menos 4 Pokemones. Te faltan " + (4 - numPokemones));
                    } else {
                        System.out.println("Mazo finalizado!");
                        Collections.shuffle(mazoPersonalizado, new Random());
                        for (Carta c : mazoPersonalizado) {
                            jugador.getMazo().apilar(c);
                        }
                        System.out.println("Mazo de " + jugador.getNombre() + " listo con " + mazoPersonalizado.size() + " cartas.\n");
                        mazoListo = true;
                    }
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    private static void turnoJugador(Scanner scanner, Jugador actual) {
        System.out.println("\n=== TURNO DE " + actual.getNombre().toUpperCase() + " ===");
        
        if (actual.getActivo() == null) {
            System.out.println("Buscando Pokemon activo...");
            actual.promoverSiguientePokemon();
            if (actual.getActivo() == null) {
                System.out.println("¡No hay Pokemon disponibles!");
                return;
            }
        }
        
        System.out.println("Pokemon activo: " + actual.getActivo());
        System.out.println("Vida: " + actual.getActivo().getVida());
        System.out.println("\nBanca:");
        for (int i = 0; i < 3; i++) {
            System.out.println((i + 1) + ". " + actual.getPokemonBanca(i));
        }
        
        System.out.println("\n=== SELECCIONE UNA ACCION ===");
        System.out.println("1. Cambiar Pokemon activo con la banca");
        System.out.println("2. Usar / Jugar carta de la mano");
        System.out.println("3. Omitir accion e ir a la fase de ataque");

        int accion = leerEnteroEnRango(scanner, "Opcion (1-3): ", 1, 3);

        switch (accion) {
            case 1:
                for (int i = 0; i < 3; i++) {
                    System.out.println((i + 1) + ". Banca " + (i + 1) + " (" + actual.getPokemonBanca(i) + ")");
                }
                int opcionBanca = leerEnteroEnRango(scanner, "Selecciona slot de banca para cambiar (0 para cancelar): ", 0, 3);
                if (opcionBanca == 0) {
                    System.out.println("No cambiaste tu Pokemon activo.");
                    break;
                }
                int idxBanca = opcionBanca - 1;
                if (actual.cambiarActivoConBanca(idxBanca)) {
                    System.out.println("Cambiaste a " + actual.getActivo().getNombre());
                } else {
                    System.out.println("Ese slot de banca esta vacio.");
                }
                break;

            case 2:
                if (actual.getTamanoMano() == 0) {
                    System.out.println("No tienes cartas en la mano.");
                    break;
                }
                actual.mostrarMano();
                int opcionMano = leerEnteroEnRango(scanner, "Elige carta a jugar (0 para cancelar): ", 0, actual.getTamanoMano());
                if (opcionMano == 0) {
                    System.out.println("No jugaste ninguna carta.");
                    break;
                }
                int idx = opcionMano - 1;
                Carta elegida = actual.obtenerCartaDeMano(idx);
                if (elegida.esPocion()) {
                    if (elegida.getNombre().equals("Pocion")) {
                        if (actual.usarPocionEnActivo(idx)) {
                            System.out.println("Usaste una Pocion en " + actual.getActivo().getNombre() 
                                + ". Vida actual: " + actual.getActivo().getVida());
                        }
                    } else if (elegida.getNombre().equals("Pokeball")) {
                        if (actual.usarPokeball(idx)) {
                            System.out.println("Usaste una Pokeball! Recibiste un Pokemon en tu mano.");
                        } else {
                            System.out.println("Usaste una Pokeball pero no quedan Pokemones en el mazo.");
                        }
                    }
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
                break;

            default:
                System.out.println(actual.getNombre() + " se prepara para atacar...");
                break;
        }

        System.out.println("\nMano de " + actual.getNombre() + " despues de la accion:");
        actual.mostrarMano();
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

    private static int leerEnteroEnRango(Scanner scanner, String mensaje, int minimo, int maximo) {
        while (true) {
            int valor = leerEntero(scanner, mensaje);
            if (valor >= minimo && valor <= maximo) return valor;
            System.out.println("Numero invalido. Escoge un numero entre " + minimo + " y " + maximo + ".");
        }
    }

    private static int calcularDanio(Jugador atacante, Jugador defensor, String campoActual) {
        Carta cartaAtacante = atacante.getActivo();
        Carta cartaDefensora = defensor.getActivo();

        if (cartaAtacante == null || cartaDefensora == null) return 0;

        String tipoAtacante = tipoDe(cartaAtacante.getNombre());
        String tipoDefensor = tipoDe(cartaDefensora.getNombre());

        int danio = cartaAtacante.getDanio();
        int bonoExtra = 0;
        StringBuilder bonos = new StringBuilder(" [");
        boolean tieneBono = false;

        boolean ventajaElemental = tieneVentaja(tipoAtacante, tipoDefensor);
        boolean terrenoFavorable = coincideCampo(tipoAtacante, campoActual);
        boolean defensorDebilPorTerreno = tieneDesventaja(tipoDefensor, campoActual);

        // Bono 1: el atacante tiene ventaja elemental (+10).
        if (ventajaElemental) {
            bonoExtra += 10;
            bonos.append("+10 VENTAJA ELEMENTAL");
            tieneBono = true;
        }

        // Bono 2: el terreno favorece al atacante (+10).
        if (terrenoFavorable) {
            bonoExtra += 10;
            if (tieneBono) bonos.append(" | ");
            bonos.append("+10 VENTAJA DE TERRENO");
            tieneBono = true;
        }

        // Bono 3: el terreno tiene ventaja sobre el defensor (+10).
        if (defensorDebilPorTerreno) {
            bonoExtra += 10;
            if (tieneBono) bonos.append(" | ");
            bonos.append("+10 TERRENO DESFAVORABLE PARA DEFENSOR");
            tieneBono = true;
        }

        danio += Math.min(bonoExtra, 30);

        if (tieneBono) {
            bonos.append("]");
            System.out.println("  " + bonos.toString());
        }

        return Math.max(0, danio);
    }

    private static boolean tieneDesventaja(String tipo, String campoActual) {
        if (campoActual == null) return false;
        return tieneVentaja(tipoDeCampo(campoActual), tipo);
    }

    private static String tipoDeCampo(String campoActual) {
        if (campoActual.contains("Fuego")) return "Fuego";
        if (campoActual.contains("Pasto")) return "Planta";
        if (campoActual.contains("Agua")) return "Agua";
        if (campoActual.contains("Rayo")) return "Rayo";
        return "Normal";
    }

    private static boolean coincideCampo(String tipo, String campoActual) {
        if (campoActual == null) return false;
        if (campoActual.contains("Fuego") && tipo.equals("Fuego")) return true;
        if (campoActual.contains("Pasto") && tipo.equals("Planta")) return true;
        if (campoActual.contains("Agua") && tipo.equals("Agua")) return true;
        if (campoActual.contains("Rayo") && tipo.equals("Rayo")) return true;
        return false;
    }

    private static boolean tieneVentaja(String atacante, String defensor) {
        if (atacante.equals("Planta") && defensor.equals("Agua")) return true;
        if (atacante.equals("Agua") && defensor.equals("Fuego")) return true;
        if (atacante.equals("Fuego") && defensor.equals("Planta")) return true;
        if (atacante.equals("Rayo") && defensor.equals("Agua")) return true;
        return false;
    }

    private static String tipoDe(String nombre) {
        if (nombre.equals("Bulbasaur") || nombre.equals("Ivysaur") || nombre.equals("Oddish")) return "Planta";
        if (nombre.equals("Charmander") || nombre.equals("Charmeleon") || nombre.equals("Magmar")) return "Fuego";
        if (nombre.equals("Squirtle") || nombre.equals("Wartortle") || nombre.equals("Gyarados")) return "Agua";
        if (nombre.equals("Pikachu") || nombre.equals("Raichu")) return "Rayo";
        return "Normal";
    }
}