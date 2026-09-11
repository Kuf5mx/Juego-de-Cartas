public class ColaPrioridad {

    private Carta[] cartas;
    private int[] prioridades;
    private int tamano;

    public ColaPrioridad(int capacidad) {
        cartas = new Carta[capacidad];
        prioridades = new int[capacidad];
    }

    public boolean estaVacia() { return tamano == 0; }

    public void encolar(Carta carta, int prioridad) {
        if (carta == null || tamano == cartas.length) return;
        int posicion = tamano;
        while (posicion > 0 && prioridad < prioridades[posicion - 1]) {
            cartas[posicion] = cartas[posicion - 1];
            prioridades[posicion] = prioridades[posicion - 1];
            posicion--;
        }
        cartas[posicion] = carta;
        prioridades[posicion] = prioridad;
        tamano++;
    }

    public Carta desencolar() {
        if (estaVacia()) return null;
        Carta carta = cartas[0];
        for (int i = 1; i < tamano; i++) {
            cartas[i - 1] = cartas[i];
            prioridades[i - 1] = prioridades[i];
        }
        cartas[--tamano] = null;
        return carta;
    }
}