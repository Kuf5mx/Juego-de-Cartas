import java.util.Random;
import java.util.Scanner;

/**
 * Fachada pequena para que la interfaz no dependa de las reglas internas del turno.
 */
public class ControladorTurno {
    private final MotorTurno motor;

    public ControladorTurno(GestorPartida gestorPartida, Random random) {
        motor = new MotorTurno(gestorPartida, random);
    }

    public void jugarTurno(Scanner scanner, Jugador actual, Jugador rival,
            int turno, String campoActual) {
        motor.jugarTurno(scanner, actual, rival, turno, campoActual);
    }

    public boolean partidaTerminada() {
        return motor.partidaTerminada();
    }

    public Jugador getGanadorPartida() {
        return motor.getGanadorPartida();
    }
}
