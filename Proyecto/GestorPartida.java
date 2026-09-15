import java.util.Map;
import java.util.Scanner;

/**
 * Coordina las estructuras que representan el estado global de una partida.
 * La interfaz puede consultar esta clase sin depender de la consola.
 */
public class GestorPartida {
    private static final String[] CAMPOS = {
        "Llanura de Fuego", "Bosque de Pasto", "Oceano de Agua", "Valle de Rayo"
    };

    private final ListaCircular campos;
    private final ArbolEvolucion evoluciones;
    private final ListaDoble historial;

    public GestorPartida(Map<String, Carta> catalogo) {
        campos = new ListaCircular();
        for (int i = CAMPOS.length - 1; i >= 0; i--) campos.agregar(CAMPOS[i]);
        campos.siguienteCampo();

        evoluciones = construirArbolEvolucion(catalogo);
        historial = new ListaDoble();
    }

    private ArbolEvolucion construirArbolEvolucion(Map<String, Carta> catalogo) {
        ArbolEvolucion arbol = new ArbolEvolucion(catalogo.get("Bulbasaur"));
        agregarCadena(arbol, catalogo, "Bulbasaur", "Ivysaur", "Venusaur");
        agregarCadena(arbol, catalogo, "Charmander", "Charmeleon", "Charizard");
        agregarCadena(arbol, catalogo, "Squirtle", "Wartortle", "Blastoise");
        agregarCadena(arbol, catalogo, "Pichu", "Pikachu", "Raichu");
        agregarCadena(arbol, catalogo, "Oddish", "Gloom", "Vileplume");
        agregarCadena(arbol, catalogo, "Magikarp", "Gyarados");
        return arbol;
    }

    private void agregarCadena(ArbolEvolucion arbol, Map<String, Carta> catalogo, String... nombres) {
        if (!nombres[0].equals("Bulbasaur")) {
            arbol.agregarRaiz(catalogo.get(nombres[0]));
        }
        for (int i = 1; i < nombres.length; i++) {
            arbol.agregarEvolucion(nombres[i - 1], catalogo.get(nombres[i]));
        }
    }

    public String campoActual() {
        return campos.obtenerActual();
    }

    public void avanzarCampoAlComenzarTurno(int turno) {
        if (turno > 1 && (turno - 1) % 3 == 0) campos.siguienteCampo();
    }

    public Carta siguienteEvolucion(String nombrePokemon) {
        return evoluciones.buscarSiguiente(nombrePokemon);
    }

    public void registrar(String evento) {
        historial.agregarEvento(evento);
    }

    public ListaDoble getHistorial() {
        return historial;
    }

    public void mostrarHistorial(Scanner scanner) {
        System.out.println("\n================ HISTORIAL DE JUGADAS ================");
        System.out.println("1. Ver desde la primera jugada");
        System.out.println("2. Ver desde la ultima jugada");
        System.out.println("3. Cerrar historial");
        int opcion;
        do {
            System.out.print("Selecciona una opcion: ");
            try {
                opcion = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                opcion = 0;
            }
            if (opcion == 1) historial.mostrarAdelante();
            else if (opcion == 2) historial.mostrarAtras();
            else if (opcion != 3) System.out.println("Escoge una opcion entre 1 y 3.");
        } while (opcion != 3);
    }
}
