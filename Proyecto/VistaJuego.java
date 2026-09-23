import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * AQUI DEBES ESTAR: tablero visual del juego.
 * Las cartas son genericas y de texto hasta que se agreguen imagenes PNG.
 */
public class VistaJuego {
    private static final String[] CARTAS_INICIALES = {
        "Bulbasaur", "Charmander", "Squirtle", "Pichu", "Oddish", "Magikarp",
        "Pidgey", "Clefairy", "Magmar", "Pocion", "Superpocion", "Pokeball",
        "Caramelo Raro"
    };

    private final Random random = new Random();
    private final Map<String, Carta> catalogo = new CatalogoCartas(random).obtenerCartas();
    private final List<String> mazoElegido = new ArrayList<>();
    private Stage stage;
    private Jugador jugador;
    private Jugador rival;
    private GestorPartida gestor;
    private Jugador turno;
    private Jugador defensor;
    private int numeroTurno;
    private boolean energiaUsada;
    private boolean ataqueUsado;
    private Label estado;
    private VBox tablero;
    private VBox detalle;

    public void mostrar(Stage stage) {
        this.stage = stage;
        mostrarConfiguracion();
    }

    private void mostrarConfiguracion() {
        TextField nombre1 = new TextField("Jugador 1");
        TextField nombre2 = new TextField("Jugador 2");
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().addAll(CARTAS_INICIALES);
        selector.getSelectionModel().selectFirst();

        ListView<String> listaMazo = new ListView<>();
        listaMazo.setPrefHeight(260);
        Button agregar = new Button("Agregar carta");
        Button quitar = new Button("Quitar seleccionada");
        Button mazoBase = new Button("Mazo base");
        mazoBase.setOnAction(event -> {
            mazoElegido.clear();
            for (int i = 0; i < 8; i++) mazoElegido.add(CARTAS_INICIALES[i]);
            for (int i = 0; i < 3; i++) mazoElegido.add("Pocion");
            mazoElegido.add("Superpocion");
            mazoElegido.add("Pokeball");
            mazoElegido.add("Caramelo Raro");
            actualizarListaMazo(listaMazo);
        });
        agregar.setOnAction(event -> {
            if (mazoElegido.size() < 15) {
                mazoElegido.add(selector.getValue());
                actualizarListaMazo(listaMazo);
            }
        });
        quitar.setOnAction(event -> {
            int indice = listaMazo.getSelectionModel().getSelectedIndex();
            if (indice >= 0) {
                mazoElegido.remove(indice);
                actualizarListaMazo(listaMazo);
            }
        });
        mazoBase.fire();

        Button comenzar = new Button("Comenzar partida");
        comenzar.setDefaultButton(true);
        comenzar.setOnAction(event -> iniciarPartida(nombre1.getText(), nombre2.getText()));

        VBox nombres = new VBox(8,
                new Label("Nombre del jugador 1"), nombre1,
                new Label("Nombre del jugador 2"), nombre2);
        HBox controlesMazo = new HBox(8, selector, agregar, quitar, mazoBase);
        VBox editor = new VBox(10, new Label("Mazo de 15 cartas (se usara para ambos jugadores)"),
                controlesMazo, listaMazo, comenzar);
        editor.setMaxWidth(760);

        VBox contenido = new VBox(22, titulo("Preparar partida"), nombres, editor);
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setPadding(new Insets(30));
        contenido.setStyle("-fx-background-color: #183642;");
        stage.setTitle("Juego de Cartas Pokemon");
        stage.setScene(new Scene(contenido, 1050, 760));
        stage.show();
    }

    private void iniciarPartida(String nombre1, String nombre2) {
        if (mazoElegido.size() < 15 || contarPokemones() < 4) {
            alerta("El mazo necesita 15 cartas y al menos 4 Pokemon.");
            return;
        }
        jugador = crearJugador(nombre1.trim().isEmpty() ? "Jugador 1" : nombre1.trim());
        rival = crearJugador(nombre2.trim().isEmpty() ? "Jugador 2" : nombre2.trim());
        gestor = new GestorPartida(catalogo);
        cargarMazo(jugador);
        cargarMazo(rival);
        jugador.robarCartasIniciales(4);
        rival.robarCartasIniciales(4);
        prepararCampo(jugador, 1);
        prepararCampo(rival, 1);
        turno = jugador;
        defensor = rival;
        numeroTurno = 1;
        energiaUsada = false;
        ataqueUsado = false;
        construirTablero();
    }

    private Jugador crearJugador(String nombre) {
        return new Jugador(nombre, new Pila(30));
    }

    private void cargarMazo(Jugador destino) {
        List<String> cartas = new ArrayList<>(mazoElegido);
        Collections.shuffle(cartas, random);
        for (String nombre : cartas) destino.getMazo().apilar(clonar(cartaPorNombre(nombre)));
    }

    private Carta clonar(Carta carta) {
        if (carta == null) return new Carta("Carta", 0, 0, true);
        return new Carta(carta.getNombre(), carta.getVidaMaxima(), carta.getDanio(), carta.esPocion(),
                carta.getTipo(), carta.getFase(), carta.getAtaque(), carta.tieneAtaqueElemental(), carta.getHabilidad());
    }

    private void prepararCampo(Jugador destino, int turnoInicial) {
        Carta activo = quitarPrimeraBasicaDeMano(destino);
        if (activo != null) {
            activo.prepararParaTablero(turnoInicial);
            destino.ponerCartaEnActivo(activo);
        }
        for (int i = 0; i < 3; i++) {
            Carta banca = quitarPrimeraBasicaDeMano(destino);
            if (banca == null) break;
            banca.prepararParaTablero(turnoInicial);
            destino.ponerCartaEnBanca(banca, i);
        }
    }

    private Carta quitarPrimeraBasicaDeMano(Jugador destino) {
        for (int i = 0; i < destino.getTamanoMano(); i++) {
            Carta carta = destino.obtenerCartaDeMano(i);
            if (carta != null && carta.esPokemon() && carta.getFase() == 1) {
                return destino.sacarCartaDeMano(i);
            }
        }
        return null;
    }

    private void construirTablero() {
        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(14));
        raiz.setStyle("-fx-background-color: #183642;");
        tablero = new VBox(10);
        detalle = new VBox(8);
        detalle.setPadding(new Insets(12));
        detalle.setPrefWidth(250);
        detalle.setStyle("-fx-background-color: #f4f1de; -fx-background-radius: 8;");
        estado = new Label();
        estado.setStyle("-fx-text-fill: #f4f1de; -fx-font-size: 14px;");
        raiz.setCenter(tablero);
        raiz.setRight(detalle);
        raiz.setBottom(crearAcciones());
        refrescar();
        stage.setScene(new Scene(raiz, 1400, 850));
    }

    private void refrescar() {
        tablero.getChildren().clear();
        tablero.getChildren().addAll(titulo("Campo: " + gestor.campoActual()),
                crearZonaJugador(rival, false), crearZonaJugador(jugador, true), estado);
        estado.setText("Turno " + numeroTurno + " de " + turno.getNombre()
                + " | Energia: " + (energiaUsada ? "usada" : "disponible")
                + " | Ataque: " + (ataqueUsado ? "usado" : "disponible"));
    }

    private VBox crearZonaJugador(Jugador jugadorVista, boolean esJugador) {
        VBox zona = new VBox(6);
        zona.setPadding(new Insets(8));
        zona.setStyle(esJugador ? "-fx-background-color: #2f6690; -fx-background-radius: 8;"
                : "-fx-background-color: #7f5539; -fx-background-radius: 8;");
        Label nombre = new Label(jugadorVista.getNombre() + " | Puntos: " + jugadorVista.getPuntos());
        nombre.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        HBox campo = new HBox(18);
        campo.setAlignment(Pos.CENTER);
        campo.getChildren().add(crearEspacioActivo(jugadorVista));
        for (int i = 0; i < 3; i++) campo.getChildren().add(crearCartaTablero(jugadorVista.getPokemonBanca(i)));
        zona.getChildren().addAll(nombre, campo);
        if (esJugador) zona.getChildren().add(crearMano());
        return zona;
    }

    private VBox crearEspacioActivo(Jugador propietario) {
        VBox espacio = crearCartaTablero(propietario.getActivo());
        Label etiqueta = new Label("ACTIVO");
        etiqueta.setStyle("-fx-text-fill: #ffd166; -fx-font-weight: bold;");
        espacio.getChildren().add(0, etiqueta);
        return espacio;
    }

    private VBox crearCartaTablero(Carta carta) {
        VBox visual = cartaVisual(carta, 155, 150);
        if (carta != null && carta == turno.getActivo()) {
            visual.setStyle(visual.getStyle() + "-fx-border-color: #ffd166; -fx-border-width: 4;");
        }
        if (carta != null) visual.setOnMouseClicked(event -> mostrarDetalle(carta));
        return visual;
    }

    private HBox crearMano() {
        HBox mano = new HBox(8);
        mano.setAlignment(Pos.CENTER);
        for (int i = 0; i < turno.getTamanoMano(); i++) {
            Carta carta = turno.obtenerCartaDeMano(i);
            VBox visual = cartaVisual(carta, 125, 115);
            visual.setOnMouseClicked(event -> mostrarDetalle(carta));
            mano.getChildren().add(visual);
        }
        return mano;
    }

    private VBox cartaVisual(Carta carta, double ancho, double alto) {
        VBox visual = new VBox(4);
        visual.setAlignment(Pos.CENTER);
        visual.setPrefSize(ancho, alto);
        visual.setPadding(new Insets(7));
        visual.setStyle("-fx-background-color: #f4f1de; -fx-background-radius: 7;"
                + "-fx-border-color: #283044; -fx-border-width: 2; -fx-border-radius: 7;");
        if (carta == null) {
            visual.getChildren().add(new Label("Vacio"));
            return visual;
        }
        Label nombre = new Label("Carta\n" + carta.getNombre());
        nombre.setWrapText(true);
        nombre.setAlignment(Pos.CENTER);
        nombre.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");
        Label datos = new Label(carta.esPokemon()
                ? carta.getTipo() + " | Vida " + carta.getVida() + "/" + carta.getVidaMaxima()
                : "Objeto");
        Label energia = new Label(carta.esPokemon() ? "Energia: " + carta.getEnergias() : "");
        visual.getChildren().addAll(nombre, datos, energia);
        return visual;
    }

    private VBox crearAcciones() {
        Button energiaActivo = new Button("Energia al activo");
        Button energiaBanca = new Button("Energia a banca 1");
        Button jugar = new Button("Jugar primera carta");
        Button evolucionar = new Button("Evolucionar activo");
        Button retirar = new Button("Retirar con banca 1");
        Button atacar = new Button("Atacar");
        Button terminar = new Button("Terminar turno");
        Button reiniciar = new Button("Nueva partida");

        energiaActivo.setOnAction(event -> asignarEnergia(-1));
        energiaBanca.setOnAction(event -> asignarEnergia(0));
        jugar.setOnAction(event -> jugarPrimeraCarta());
        evolucionar.setOnAction(event -> evolucionarActivo());
        retirar.setOnAction(event -> ejecutarRetirada());
        atacar.setOnAction(event -> ejecutarAtaque());
        terminar.setOnAction(event -> terminarTurno());
        reiniciar.setOnAction(event -> mostrarConfiguracion());

        HBox botones = new HBox(8, energiaActivo, energiaBanca, jugar, evolucionar,
                retirar, atacar, terminar, reiniciar);
        botones.setAlignment(Pos.CENTER);
        VBox inferior = new VBox(8, botones);
        inferior.setPadding(new Insets(10, 0, 0, 0));
        return inferior;
    }

    private void asignarEnergia(int posicion) {
        if (energiaUsada || !turno.asignarEnergia(posicion)) {
            estado.setText("No se puede asignar energia en ese espacio.");
            return;
        }
        energiaUsada = true;
        refrescar();
    }

    private void jugarPrimeraCarta() {
        if (turno.getTamanoMano() == 0) return;
        Carta carta = turno.obtenerCartaDeMano(0);
        try {
            if (carta.esPokemon() && carta.getFase() == 1 && turno.ponerPokemonEnTablero(carta, numeroTurno)) {
                turno.sacarCartaDeMano(0);
                estado.setText(carta.getNombre() + " entro al tablero.");
            } else if (carta.esCarta("Pocion") && turno.usarPocionEnActivo(0)) {
                estado.setText("Pocion usada.");
            } else if (carta.esCarta("Superpocion") && turno.usarSuperPocion(0, random)) {
                estado.setText("Superpocion usada.");
            } else if (carta.esCarta("Pokeball") && turno.usarPokeball(0)) {
                estado.setText("Pokeball usada.");
            } else {
                estado.setText("La primera carta no se puede jugar ahora.");
            }
        } catch (ListaVaciaException exception) {
            estado.setText(exception.getMessage());
        }
        refrescar();
    }

    private void evolucionarActivo() {
        Carta activo = turno.getActivo();
        if (activo == null) return;
        Carta siguiente = gestor.siguienteEvolucion(activo.getNombre());
        if (siguiente == null) return;
        for (int i = 0; i < turno.getTamanoMano(); i++) {
            Carta carta = turno.obtenerCartaDeMano(i);
            if (carta.esCarta(siguiente.getNombre())) {
                Carta anterior = turno.evolucionSiguiente(carta, numeroTurno, true);
                if (anterior != null) {
                    turno.sacarCartaDeMano(i);
                    estado.setText(anterior.getNombre() + " evoluciono a " + carta.getNombre() + ".");
                }
                refrescar();
                return;
            }
        }
        estado.setText("No tienes la siguiente evolucion en la mano.");
    }

    private void ejecutarRetirada() {
        if (turno.retirarActivoConBanca(0)) estado.setText("Retirada realizada.");
        else estado.setText("Necesitas banca y energia suficiente para retirarte.");
        refrescar();
    }

    private void ejecutarAtaque() {
        Carta atacante = turno.getActivo();
        Carta objetivo = defensor.getActivo();
        if (ataqueUsado || atacante == null || objetivo == null) return;
        int costo = atacante.getFase();
        if (atacante.getEnergias() < costo) {
            estado.setText("Necesitas " + costo + " energias para atacar.");
            return;
        }
        int danio = atacante.getDanio();
        if (tieneVentaja(atacante.getTipo(), objetivo.getTipo())) danio += 10;
        if (coincideCampo(atacante.getTipo(), gestor.campoActual())) danio += 10;
        objetivo.recibirDanio(danio);
        ataqueUsado = true;
        estado.setText(atacante.getNombre() + " hizo " + danio + " de dano.");
        if (objetivo.estaFueraDeCombate()) {
            turno.sumarPunto();
            defensor.sacarActivo();
            prepararCampo(defensor, numeroTurno);
            estado.setText("Punto para " + turno.getNombre() + ".");
        }
        refrescar();
    }

    private void terminarTurno() {
        Jugador temporal = turno;
        turno = defensor;
        defensor = temporal;
        numeroTurno++;
        gestor.avanzarCampoAlComenzarTurno(numeroTurno);
        energiaUsada = false;
        ataqueUsado = false;
        turno.robarCartaAMano();
        turno.procesarEstadoInicioTurno(random);
        refrescar();
    }

    private void mostrarDetalle(Carta carta) {
        detalle.getChildren().clear();
        detalle.getChildren().add(titulo("Detalle de carta"));
        detalle.getChildren().addAll(new Label("Nombre: Carta " + carta.getNombre()),
                new Label("Tipo: " + carta.getTipo()),
                new Label("Vida: " + carta.getVida() + "/" + carta.getVidaMaxima()),
                new Label("Ataque: " + carta.getAtaque()),
                new Label("Dano: " + carta.getDanio()),
                new Label("Ataque elemental: " + (carta.tieneAtaqueElemental() ? "si" : "no")),
                new Label("Energia actual: " + carta.getEnergias()),
                new Label("Energia para atacar: " + carta.getFase()),
                new Label("Costo de retirada: " + carta.getFase()),
                new Label("Estado: " + carta.getEstado()));
    }

    private Label titulo(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f4f1de;");
        return label;
    }

    private void actualizarListaMazo(ListView<String> lista) {
        lista.getItems().setAll(mazoElegido);
    }

    private int contarPokemones() {
        int total = 0;
        for (String nombre : mazoElegido) if (cartaPorNombre(nombre).esPokemon()) total++;
        return total;
    }

    private Carta cartaPorNombre(String nombre) {
        Carta carta = catalogo.get(nombre);
        if (carta != null) return carta;
        if ("Pocion".equals(nombre)) return new Carta("Pocion", 20, 0, true);
        if ("Superpocion".equals(nombre)) return new Carta("Superpocion", 40, 0, true);
        if ("Pokeball".equals(nombre)) return new Carta("Pokeball", 0, 0, true);
        if ("Caramelo Raro".equals(nombre)) return new Carta("Caramelo Raro", 0, 0, true);
        return new Carta("Carta", 0, 0, true);
    }

    private boolean tieneVentaja(String atacante, String defensor) {
        return ("Planta".equals(atacante) && "Agua".equals(defensor))
                || ("Agua".equals(atacante) && "Fuego".equals(defensor))
                || ("Fuego".equals(atacante) && "Planta".equals(defensor))
                || ("Rayo".equals(atacante) && "Agua".equals(defensor));
    }

    private boolean coincideCampo(String tipo, String campo) {
        return (campo.contains("Fuego") && tipo.equals("Fuego"))
                || (campo.contains("Pasto") && tipo.equals("Planta"))
                || (campo.contains("Agua") && tipo.equals("Agua"))
                || (campo.contains("Rayo") && tipo.equals("Rayo"));
    }

    private void alerta(String mensaje) {
        new Alert(Alert.AlertType.WARNING, mensaje).showAndWait();
    }
}
