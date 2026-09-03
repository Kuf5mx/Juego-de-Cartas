/**
 * Lista circular eficiente para cambiar los campos de batalla
 */
public class ListaCircular {

    private Nodo actual;

    private class Nodo {
        String campo;
        Nodo siguiente;

        public Nodo(String campo) {
            this.campo = campo;
            this.siguiente = null;
        }
    }

    public boolean estaVacia() {
        return actual == null;
    }

    /**
     * Agrega un campo al ciclo en O(1) sin recorrer la lista
     */
    public void agregar(String campo) {
        if (campo == null) {
            return;
        }

        Nodo nuevo = new Nodo(campo);

        if (actual == null) {
            actual = nuevo;
            actual.siguiente = actual;
            return;
        }

        // Insercion instantanea O(1) entre actual y actual.siguiente
        nuevo.siguiente = actual.siguiente;
        actual.siguiente = nuevo;
    }

    public void siguienteCampo() {
        if (actual != null) {
            actual = actual.siguiente;
        }
    }

    public void mostrarActual() {
        if (actual == null) {
            System.out.println("No hay campo activo");
            return;
        }

        System.out.println("Campo activo: " + actual.campo);
    }

    public String obtenerActual() {
        return actual == null ? null : actual.campo;
    }
}