import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class CatalogoCartas {
    private static final int MAX_CARTAS_MAZO = 15;
    private final Random random;
    private final Map<String, Carta> cartas;

    public CatalogoCartas(Random random) {
        this.random = random;
        this.cartas = crearCatalogo();
    }

    public Map<String, Carta> obtenerCartas() {
        return cartas;
    }

    public void personalizarMazo(Scanner scanner, Jugador jugador) {
        Carta[] catalogo = cartas.values().toArray(new Carta[0]);
        List<Carta> mazo = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            mazo.add(clonar(catalogo[indicePokemonPrimeraFase(catalogo)]));
        }
        mazo.add(new Carta("Pocion", 20, 0, true));
        mazo.add(new Carta("Pokeball", 0, 0, true));
        mazo.add(new Carta("Superpocion", 40, 0, true));
        mazo.add(new Carta("Caramelo Raro", 0, 0, true));

        boolean listo = false;
        while (!listo) {
            System.out.println("\nMazo de " + jugador.getNombre() + ": " + mazo.size() + "/15");
            System.out.println("1 Agregar Pokemon por fase  2 Pocion (cura 20)  3 Pokeball (busca fase 1)");
            System.out.println("4 Superpocion (cura 40, 50% quita energia)  5 Caramelo Raro (evoluciona sin esperar)");
            System.out.println("6 Quitar  7 Listo");
            int opcion = leerRango(scanner, "Selecciona: ", 1, 7);
            if (opcion == 1 && mazo.size() < MAX_CARTAS_MAZO) {
                Carta pokemon = seleccionarPokemonPorFase(scanner, catalogo);
                if (pokemon != null) mazo.add(clonar(pokemon));
            } else if (opcion == 2 && mazo.size() < MAX_CARTAS_MAZO) {
                mazo.add(new Carta("Pocion", 20, 0, true));
            } else if (opcion == 3 && mazo.size() < MAX_CARTAS_MAZO) {
                mazo.add(new Carta("Pokeball", 0, 0, true));
            } else if (opcion == 4 && mazo.size() < MAX_CARTAS_MAZO) {
                mazo.add(new Carta("Superpocion", 40, 0, true));
            } else if (opcion == 5 && mazo.size() < MAX_CARTAS_MAZO) {
                mazo.add(new Carta("Caramelo Raro", 0, 0, true));
            } else if (opcion == 6 && !mazo.isEmpty()) {
                mazo.remove(mazo.size() - 1);
            } else if (opcion == 7) {
                if (mazo.stream().filter(Carta::esPokemon).count() < 4) {
                    System.out.println("Necesitas al menos 4 Pokemon.");
                } else {
                    listo = true;
                }
            } else if (mazo.size() >= MAX_CARTAS_MAZO) {
                System.out.println("El mazo ya tiene 15 cartas.");
            }
        }
        Collections.shuffle(mazo, random);
        for (Carta carta : mazo) jugador.getMazo().apilar(carta);
    }

    private Map<String, Carta> crearCatalogo() {
        Map<String, Carta> catalogo = new HashMap<>();
        pokemon(catalogo, "Bulbasaur", 60, 20, "Planta", 1, "Latigo Cepa", true, null);
        pokemon(catalogo, "Ivysaur", 80, 30, "Planta", 2, "Latigo Cepa", true, null);
        pokemon(catalogo, "Venusaur", 120, 50, "Planta", 3, "Rayo Solar", true, "Curacion natural");
        pokemon(catalogo, "Charmander", 60, 20, "Fuego", 1, "Ascuas", true, null);
        pokemon(catalogo, "Charmeleon", 80, 35, "Fuego", 2, "Ascuas", true, null);
        pokemon(catalogo, "Charizard", 130, 55, "Fuego", 3, "Llamarada", true, "Llamarada intensa");
        pokemon(catalogo, "Squirtle", 60, 20, "Agua", 1, "Rayo Burbuja", true, null);
        pokemon(catalogo, "Wartortle", 85, 30, "Agua", 2, "Rayo Burbuja", true, null);
        pokemon(catalogo, "Blastoise", 130, 50, "Agua", 3, "Hidrobomba", true, "Hidrobomba");
        pokemon(catalogo, "Pichu", 40, 10, "Rayo", 1, "Impactrueno", true, null);
        pokemon(catalogo, "Pikachu", 60, 25, "Rayo", 2, "Impactrueno", true, "Impactrueno");
        pokemon(catalogo, "Raichu", 100, 45, "Rayo", 3, "Impactrueno", true, "Impactrueno fuerte");
        pokemon(catalogo, "Oddish", 50, 15, "Planta", 1, "Latigo", false, null);
        pokemon(catalogo, "Gloom", 70, 25, "Planta", 2, "Mordisco", false, null);
        pokemon(catalogo, "Vileplume", 100, 40, "Planta", 3, "Polvo Veneno", true, "Esporas");
        pokemon(catalogo, "Magikarp", 40, 5, "Agua", 1, "Placaje", false, null);
        pokemon(catalogo, "Gyarados", 110, 45, "Agua", 2, "Mordisco", false, "Furia");
        pokemon(catalogo, "Pidgey", 40, 10, "Normal", 1, "Picotazo", false, null);
        pokemon(catalogo, "Clefairy", 50, 10, "Normal", 1, "Mordisco", false, "Canto");
        pokemon(catalogo, "Magmar", 70, 30, "Fuego", 1, "Mordisco", false, null);
        return catalogo;
    }

    private void pokemon(Map<String, Carta> catalogo, String nombre, int vida, int danio,
            String tipo, int fase, String ataque, boolean elemental, String habilidad) {
        catalogo.put(nombre, new Carta(nombre, vida, danio, false, tipo, fase, ataque, elemental, habilidad));
    }

    private Carta seleccionarPokemonPorFase(Scanner scanner, Carta[] catalogo) {
        System.out.println("\nFase del Pokemon:");
        System.out.println("1. Fase 1 (Pokemon basico)");
        System.out.println("2. Fase 2 (evoluciona de fase 1)");
        System.out.println("3. Fase 3 (evoluciona de fase 2)");
        int fase = leerRango(scanner, "Selecciona la fase: ", 1, 3);
        List<Carta> opciones = new ArrayList<>();
        for (Carta carta : catalogo) {
            if (carta.esPokemon() && carta.getFase() == fase) opciones.add(carta);
        }
        for (int i = 0; i < opciones.size(); i++) {
            Carta carta = opciones.get(i);
            System.out.println((i + 1) + ". " + carta.getNombre());
        }
        int seleccion = leerRango(scanner, "Pokemon (0 cancela): ", 0, opciones.size());
        return seleccion == 0 ? null : opciones.get(seleccion - 1);
    }

    private Carta clonar(Carta carta) {
        return new Carta(carta.getNombre(), carta.getVidaMaxima(), carta.getDanio(), false,
                carta.getTipo(), carta.getFase(), carta.getAtaque(), carta.tieneAtaqueElemental(), carta.getHabilidad());
    }

    private int indicePokemonPrimeraFase(Carta[] catalogo) {
        List<Integer> candidatos = new ArrayList<>();
        for (int i = 0; i < catalogo.length; i++) {
            if (catalogo[i].esPokemon() && catalogo[i].getFase() == 1) candidatos.add(i);
        }
        return candidatos.get(random.nextInt(candidatos.size()));
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
