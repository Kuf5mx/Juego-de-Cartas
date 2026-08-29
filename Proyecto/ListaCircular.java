/**
 * Lista circular para cambiar los campos de batalla.
 */
public class ListaCircular {

    // Nodo actual del ciclo.
    private Nodo actual;

    // Nodo interno para el campo.
    private class Nodo {
        // Nombre del campo.
        String campo;
        // Siguiente campo.
        Nodo siguiente;

        /**
         * Nodo que guarda un campo.
         */
        public Nodo(String campo) {
            this.campo = campo;
            this.siguiente = null;
        }
    }

    /**
     * Agrega un campo al ciclo.
     */
    public void agregar(String campo) {
        Nodo nuevo = new Nodo(campo);

        if (actual == null) {
            actual = nuevo;
            actual.siguiente = actual;
            return;
        }

        // Buscamos el ultimo para cerrar el ciclo.
        Nodo ultimo = actual;
        while (ultimo.siguiente != actual) {
            ultimo = ultimo.siguiente;
        }

        ultimo.siguiente = nuevo;
        nuevo.siguiente = actual;
    }

    /**
     * Avanza al siguiente campo.
     */
    public void siguienteCampo() {
        // Avanza al siguiente campo.
        if (actual != null) {
            actual = actual.siguiente;
        }
    }

    /**
     * Muestra el campo activo.
     */
    public void mostrarActual() {
        // Si no hay campo, avisamos.
        if (actual == null) {
            System.out.println("No hay campo activo");
            return;
        }

        System.out.println("Campo activo: " + actual.campo);
    }

    /**
     * Devuelve el nombre del campo actual.
     */
    public String obtenerActual() {
        return actual == null ? null : actual.campo;
    }
}