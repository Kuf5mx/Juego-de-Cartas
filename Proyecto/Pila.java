import java.util.Scanner;

/**
 * Pila sencilla para guardar cartas.
 */
public class Pila {

    // Arreglo donde se guardan las cartas
    private Carta[] elementos;
    // Indice que indica cuantas cartas hay en la pila
    private int tope;
    // Cantidad maxima de cartas que soporta la pila
    private int capacidad;

    /**
     * Constructor para definir el tamano de la pila
     */
    public Pila(int capacidad) {
        this.capacidad = capacidad;
        this.elementos = new Carta[capacidad];
        this.tope = 0;
    }

    // Comprueba si no hay ninguna carta en la pila
    public boolean estaVacia() {
        return tope == 0;
    }

    // Comprueba si la pila alcanzo su limite de capacidad
    public boolean estaLlena() {
        return tope == capacidad;
    }

    /**
     * Agrega una carta en la parte superior de la pila
     */
    public void apilar(Carta carta) {
        // Validamos que exista espacio antes de insertar
        if (estaLlena()) {
            System.out.println("La pila está llena");
            return;
        }

        elementos[tope] = carta;
        tope++;
    }

    /**
     * Retira y devuelve la carta ubicada en el tope
     */
    public Carta desapilar() {
        // Si no hay cartas devolvemos null
        if (estaVacia()) {
            return null;
        }

        // Retrocedemos el tope y liberamos la posicion
        tope--;
        Carta carta = elementos[tope];
        elementos[tope] = null;
        return carta;
    }

    /**
     * Retorna la carta superior sin eliminarla de la pila
     */
    public Carta cima() {
        if (estaVacia()) {
            return null;
        }

        return elementos[tope - 1];
    }

    /**
     * Imprime las cartas desde la parte superior hasta la base
     */
    public void mostrar() {
        if (estaVacia()) {
            System.out.println("(vacia)");
            return;
        }

        // Recorremos el arreglo de atras hacia adelante
        for (int i = tope - 1; i >= 0; i--) {
            System.out.println(elementos[i]);
        }
    }

    // Imprime la lista numerada para facilitar la eleccion de cartas
    public void mostrarConNumeros() {
        if (estaVacia()) {
            System.out.println("(vacio)");
            return;
        }

        // Mostramos un contador visual para la seleccion
        for (int i = tope - 1; i >= 0; i--) {
            System.out.println((tope - i) + ". " + elementos[i]);
        }
    }

    // Remueve una carta especifica usando el numero de opcion ingresado
    public Carta quitarPorIndice(int numero) {
        // Convertimos el numero ingresado al indice real del arreglo
        int posicion = tope - numero;

        // Verificamos que la seleccion este dentro del rango valido
        if (numero < 1 || posicion < 0 || posicion >= tope) {
            return null;
        }

        // Guardamos la carta elegida
        Carta carta = elementos[posicion];

        // Desplazamos los elementos para cubrir el hueco dejado
        for (int i = posicion; i < tope - 1; i++) {
            elementos[i] = elementos[i + 1];
        }

        // Actualizamos el tope y vaciamos el ultimo espacio sobrante
        tope--;
        elementos[tope] = null;
        return carta;
    }
}