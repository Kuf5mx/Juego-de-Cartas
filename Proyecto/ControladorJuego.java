import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class ControladorJuego {
    private final Random random = new Random();
    private GestorPartida gestorPartida;
    private AccionesGrafica accionesGrafica;
    private Jugador jugador1;
    private Jugador jugador2;

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);
        CatalogoCartas catalogo = new CatalogoCartas(random);
        Map<String, Carta> cartas = catalogo.obtenerCartas();
        gestorPartida = new GestorPartida(cartas);
        accionesGrafica = new AccionesGrafica(gestorPartida, random);

        mostrarBienvenida();
        jugador1 = crearJugador(scanner, "Jugador 1");
        jugador2 = crearJugador(scanner, "Jugador 2");
        catalogo.personalizarMazo(scanner, jugador1);
        catalogo.personalizarMazo(scanner, jugador2);
        prepararJugadores(scanner);

        ControladorTurno controladorTurno = new ControladorTurno(gestorPartida, random);
        ejecutarTurnos(scanner, controladorTurno);
        mostrarResultado(scanner, controladorTurno);
        scanner.close();
    }

    private Jugador crearJugador(Scanner scanner, String nombreDefecto) {
        String nombre = leerNombre(scanner, "Ingrese el nombre del " + nombreDefecto + ": ", nombreDefecto);
        return new Jugador(nombre, new Pila(30));
    }

    private void prepararJugadores(Scanner scanner) {
        jugador1.robarCartasIniciales(4);
        jugador2.robarCartasIniciales(4);
        jugador1.elegirCampoInicial(scanner);
        jugador2.elegirCampoInicial(scanner);
    }

    private void ejecutarTurnos(Scanner scanner, ControladorTurno controladorTurno) {
        Cola turnos = new Cola(2);
        if (random.nextBoolean()) {
            turnos.encolar(jugador1);
            turnos.encolar(jugador2);
        } else {
            turnos.encolar(jugador2);
            turnos.encolar(jugador1);
        }

        int turno = 1;
        while (!controladorTurno.partidaTerminada()
                && jugador1.getPuntos() < 3 && jugador2.getPuntos() < 3) {
            Jugador actual = turnos.desencolar();
            if (actual == null) break;
            Jugador rival = actual == jugador1 ? jugador2 : jugador1;
            gestorPartida.avanzarCampoAlComenzarTurno(turno);
            controladorTurno.jugarTurno(scanner, actual, rival, turno, gestorPartida.campoActual());
            if (actual.getPuntos() < 3 && rival.getPuntos() < 3) turnos.encolar(actual);
            turno++;
        }
    }

    private void mostrarResultado(Scanner scanner, ControladorTurno controladorTurno) {
        System.out.println("\n=== Fin de la partida ===");
        Jugador ganador = controladorTurno.getGanadorPartida();
        if (ganador == null && jugador1.getPuntos() >= 3) ganador = jugador1;
        if (ganador == null && jugador2.getPuntos() >= 3) ganador = jugador2;

        if (ganador != null) {
            System.out.println("Ganador: " + ganador.getNombre() + " con " + ganador.getPuntos() + " punto(s).");
            gestorPartida.registrar("La partida termino con " + ganador.getNombre() + " como ganador.");
        } else {
            System.out.println("La partida termino sin ganador.");
            gestorPartida.registrar("La partida termino sin ganador.");
        }

        System.out.print("\n¿Quieres consultar el historial de jugadas? (1=Si, 2=No): ");
        if (leerRango(scanner, "", 1, 2) == 1) {
            gestorPartida.mostrarHistorial(scanner);
            System.out.println("\nCartas usadas por " + jugador1.getNombre() + ":");
            jugador1.mostrarHistorialJugadas();
            System.out.println("\nCartas usadas por " + jugador2.getNombre() + ":");
            jugador2.mostrarHistorialJugadas();
        }
    }

    private void mostrarBienvenida() {
        System.out.println("==========================================");
        System.out.println("   BIENVENIDO AL JUEGO DE CARTAS POKEMON  ");
        System.out.println("==========================================");
    }

    public GestorPartida getGestorPartida() { return gestorPartida; }
    public AccionesGrafica getAccionesGrafica() { return accionesGrafica; }
    public Jugador getJugador1() { return jugador1; }
    public Jugador getJugador2() { return jugador2; }

    private String leerNombre(Scanner scanner, String mensaje, String defecto) {
        System.out.print(mensaje);
        String nombre = scanner.nextLine().trim();
        return nombre.isEmpty() ? defecto : nombre;
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
