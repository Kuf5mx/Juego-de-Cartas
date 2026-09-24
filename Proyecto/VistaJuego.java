import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.geometry.*;
import javafx.scene.Scene; 
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
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
    private final List<String> mazoJugador1 = new ArrayList<>();
    private final List<String> mazoJugador2 = new ArrayList<>();
    private Stage stage;
    private Jugador jugador;
    private Jugador rival;
    private GestorPartida gestor;
    private Jugador turno;
    private Jugador defensor;
    private int numeroTurno;
    private boolean energiaUsada;
    private int energiaDisponible;
    private boolean ataqueUsado;
    private Label estado;
    private HBox energiaVisual;
    private VBox tablero;
    private VBox detalle;
    private Jugador jugadorSeleccion;
    private int indiceSeleccionado = -1;
    private int indiceManoSeleccionada = -1;

    public void mostrar(Stage stage) {
        this.stage = stage;
        mostrarConfiguracion();
    }

    private void mostrarConfiguracion() {
        TextField nombre1 = new TextField("Jugador 1");
        TextField nombre2 = new TextField("Jugador 2");
        VBox editor1 = crearEditorMazo("Mazo de " + nombre1.getText(), mazoJugador1);
        VBox editor2 = crearEditorMazo("Mazo de " + nombre2.getText(), mazoJugador2);
        nombre1.textProperty().addListener((obs, anterior, nuevo) -> actualizarTituloEditor(editor1, "Mazo de " + nuevo));
        nombre2.textProperty().addListener((obs, anterior, nuevo) -> actualizarTituloEditor(editor2, "Mazo de " + nuevo));

        Button comenzar = new Button("Repartir y elegir activo");
        comenzar.setDefaultButton(true);
        comenzar.setOnAction(event -> iniciarPartida(nombre1.getText(), nombre2.getText()));

        VBox nombres = new VBox(8,
                new Label("Nombre del jugador 1"), nombre1,
                new Label("Nombre del jugador 2"), nombre2);
        HBox editores = new HBox(18, editor1, editor2);
        VBox contenido = new VBox(18, titulo("Preparar partida"), nombres, editores, comenzar);
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setPadding(new Insets(30));
        contenido.setStyle("-fx-background-color: #183642;");
        stage.setTitle("Juego de Cartas Pokemon");
        stage.setScene(new Scene(contenido, 1250, 820));
        stage.show();
    }

    private VBox crearEditorMazo(String encabezado, List<String> mazo) {
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().addAll(CARTAS_INICIALES);
        selector.getSelectionModel().selectFirst();
        ListView<String> lista = new ListView<>();
        lista.setPrefHeight(250);
        Label tituloMazo = new Label(encabezado);
        Button agregar = new Button("Agregar");
        Button quitar = new Button("Quitar");
        Button base = new Button("Mazo base");
        base.setOnAction(event -> {
            mazo.clear();
            for (int i = 0; i < 8; i++) mazo.add(CARTAS_INICIALES[i]);
            for (int i = 0; i < 3; i++) mazo.add("Pocion");
            mazo.add("Superpocion");
            mazo.add("Pokeball");
            mazo.add("Caramelo Raro");
            actualizarListaMazo(lista, mazo);
        });
        agregar.setOnAction(event -> {
            if (mazo.size() < 15) {
                mazo.add(selector.getValue());
                actualizarListaMazo(lista, mazo);
            }
        });
        quitar.setOnAction(event -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice >= 0) {
                mazo.remove(indice);
                actualizarListaMazo(lista, mazo);
            }
        });
        base.fire();
        VBox editor = new VBox(8, tituloMazo, selector, new HBox(6, agregar, quitar, base), lista);
        editor.setPrefWidth(500);
        return editor;
    }

    private void actualizarTituloEditor(VBox editor, String texto) {
        if (!editor.getChildren().isEmpty() && editor.getChildren().get(0) instanceof Label) {
            ((Label) editor.getChildren().get(0)).setText(texto);
        }
    }

    private void iniciarPartida(String nombre1, String nombre2) {
        if (!mazoValido(mazoJugador1) || !mazoValido(mazoJugador2)) {
            alerta("Cada jugador necesita un mazo de 15 cartas y al menos 4 Pokemon.");
            return;
        }
        jugador = crearJugador(nombre1.trim().isEmpty() ? "Jugador 1" : nombre1.trim());
        rival = crearJugador(nombre2.trim().isEmpty() ? "Jugador 2" : nombre2.trim());
        gestor = new GestorPartida(catalogo);
        cargarMazo(jugador, mazoJugador1);
        cargarMazo(rival, mazoJugador2);
        jugador.robarCartasIniciales(4);
        rival.robarCartasIniciales(4);
        mostrarSeleccionInicial(jugador);
    }

    private Jugador crearJugador(String nombre) {
        return new Jugador(nombre, new Pila(30));
    }

    private void cargarMazo(Jugador destino, List<String> mazoElegido) {
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

    private boolean mazoValido(List<String> mazo) {
        return mazo.size() == 15 && contarPokemones(mazo) >= 4;
    }

    private void mostrarSeleccionInicial(Jugador jugadorActual) {
        jugadorSeleccion = jugadorActual;
        indiceSeleccionado = -1;
        VBox raiz = new VBox(14);
        raiz.setPadding(new Insets(24));
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.setStyle("-fx-background-color: #183642;");
        Label instruccion = new Label(jugadorActual.getNombre()
                + ": elige un Pokemon activo y hasta 3 Pokemon para tu banca.");
        instruccion.setStyle("-fx-text-fill: #f4f1de; -fx-font-size: 18px;");
        ListView<String> mano = new ListView<>();
        mano.setPrefHeight(260);
        mano.getSelectionModel().selectedIndexProperty().addListener((obs, anterior, nuevo) -> {
            indiceSeleccionado = nuevo.intValue();
        });
        Button activo = new Button("Poner como activo");
        Button banca = new Button("Poner en banca");
        Button terminar = new Button("Terminar seleccion");
        Label resumen = new Label();
        activo.setOnAction(event -> ponerSeleccionado(mano, resumen, true));
        banca.setOnAction(event -> ponerSeleccionado(mano, resumen, false));
        terminar.setOnAction(event -> terminarSeleccionInicial());
        raiz.getChildren().addAll(titulo("Preparar campo"), instruccion, mano,
                new HBox(8, activo, banca, terminar), resumen);
        actualizarManoSeleccion(mano, resumen);
        stage.setScene(new Scene(raiz, 900, 650));
    }

    private void actualizarManoSeleccion(ListView<String> mano, Label resumen) {
        mano.getItems().clear();
        for (int i = 0; i < jugadorSeleccion.getTamanoMano(); i++) {
            Carta carta = jugadorSeleccion.obtenerCartaDeMano(i);
            mano.getItems().add((i + 1) + ". Carta " + carta.getNombre()
                    + (carta.esPokemon() ? " | " + carta.getTipo() : " | Objeto"));
        }
        resumen.setText("Activo: " + nombreCarta(jugadorSeleccion.getActivo())
                + " | Banca: " + contarBanca(jugadorSeleccion) + "/3");
    }

    private void ponerSeleccionado(ListView<String> mano, Label resumen, boolean activo) {
        if (indiceSeleccionado < 0) return;
        Carta carta = jugadorSeleccion.obtenerCartaDeMano(indiceSeleccionado);
        if (carta == null || !carta.esPokemon() || carta.getFase() != 1) {
            resumen.setText("Solo puedes elegir Pokemon basicos.");
            return;
        }
        if (activo && jugadorSeleccion.getActivo() != null) {
            resumen.setText("Ya tienes un Pokemon activo.");
            return;
        }
        if (!activo && contarBanca(jugadorSeleccion) >= 3) {
            resumen.setText("La banca ya esta llena.");
            return;
        }
        carta = jugadorSeleccion.sacarCartaDeMano(indiceSeleccionado);
        carta.prepararParaTablero(1);
        boolean colocado = activo ? jugadorSeleccion.ponerCartaEnActivo(carta)
                : colocarEnPrimeraBancaLibre(carta);
        if (!colocado) {
            jugadorSeleccion.getMazo().apilar(carta);
            resumen.setText(activo ? "Ya tienes un activo." : "La banca esta llena.");
        }
        indiceSeleccionado = -1;
        actualizarManoSeleccion(mano, resumen);
    }

    private boolean colocarEnPrimeraBancaLibre(Carta carta) {
        for (int i = 0; i < 3; i++) if (jugadorSeleccion.getPokemonBanca(i) == null) {
            return jugadorSeleccion.ponerCartaEnBanca(carta, i);
        }
        return false;
    }

    private void terminarSeleccionInicial() {
        if (jugadorSeleccion.getActivo() == null) {
            alerta("Debes elegir un Pokemon activo.");
            return;
        }
        if (jugadorSeleccion == jugador) {
            mostrarSeleccionInicial(rival);
        } else {
            turno = jugador;
            defensor = rival;
            indiceManoSeleccionada = -1;
            numeroTurno = 1;
            energiaUsada = false;
            energiaDisponible = 1;
            ataqueUsado = false;
            construirTablero();
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
            + " | Energia: " + energiaDisponible
                + " | Ataque: " + (ataqueUsado ? "usado" : "disponible"));
        actualizarEnergiaVisual();
    }

    private VBox crearZonaJugador(Jugador jugadorVista, boolean esJugador) {
        VBox zona = new VBox(6);
        zona.setPadding(new Insets(8));
        zona.setStyle(esJugador ? "-fx-background-color: #2f6690; -fx-background-radius: 8;"
                : "-fx-background-color: #7f5539; -fx-background-radius: 8;");
        Label nombre = new Label(jugadorVista.getNombre() + " | Puntos: " + jugadorVista.getPuntos());
        nombre.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        HBox banca = new HBox(14);
        banca.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) banca.getChildren().add(crearCartaTablero(jugadorVista.getPokemonBanca(i)));
        HBox activo = new HBox(crearEspacioActivo(jugadorVista));
        activo.setAlignment(Pos.CENTER);
        zona.getChildren().addAll(nombre, new Label("BANCA"), banca, new Label("ACTIVO"), activo);
        if (esJugador) zona.getChildren().add(crearMano());
        return zona;
    }

    private VBox crearEspacioActivo(Jugador propietario) {
        Carta carta = propietario.getActivo();
        VBox espacio = new VBox(4);
        espacio.setAlignment(Pos.CENTER);
        if (carta != null) espacio.getChildren().add(crearSprite(carta));
        espacio.getChildren().add(cartaVisual(carta, 190, 170));
        Label etiqueta = new Label("ACTIVO");
        etiqueta.setStyle("-fx-text-fill: #ffd166; -fx-font-weight: bold;");
        espacio.getChildren().add(0, etiqueta);
        if (carta != null) espacio.setOnMouseClicked(event -> mostrarDetalle(carta));
        return espacio;
    }

    private StackPane crearSprite(Carta carta) {
        StackPane contenedor = new StackPane();
        contenedor.setPrefSize(180, 125);
        String ruta = "/imagenes/sprites/" + carta.getNombre().toLowerCase() + ".png";
        java.io.InputStream recurso = getClass().getResourceAsStream(ruta);
        if (recurso == null) {
            Label sustituto = new Label("SPRITE\n" + carta.getNombre());
            sustituto.setAlignment(Pos.CENTER);
            sustituto.setStyle("-fx-text-fill: #f4f1de; -fx-font-size: 16px; -fx-font-weight: bold;"
                    + "-fx-background-color: #355070; -fx-background-radius: 10;");
            sustituto.setPrefSize(160, 105);
            contenedor.getChildren().add(sustituto);
            return contenedor;
        }
        ImageView imagen = new ImageView(new Image(recurso));
        imagen.setFitWidth(175);
        imagen.setFitHeight(120);
        imagen.setPreserveRatio(true);
        contenedor.getChildren().add(imagen);
        return contenedor;
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
            final int indice = i;
            Carta carta = turno.obtenerCartaDeMano(i);
            VBox visual = cartaVisual(carta, 125, 115);
            if (indice == indiceManoSeleccionada) {
                visual.setStyle(visual.getStyle() + "-fx-border-color: #ffd166; -fx-border-width: 4;");
            }
            visual.setOnMouseClicked(event -> {
                indiceManoSeleccionada = indice;
                mostrarDetalle(carta);
                refrescar();
            });
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
        Button energiaActivo = new Button("Energia al activo", crearIconoEnergia(24));
        Button energiaBanca = new Button("Energia a banca 1", crearIconoEnergia(24));
        Button jugar = new Button("Jugar carta seleccionada");
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
        energiaVisual = new HBox(6);
        energiaVisual.setAlignment(Pos.CENTER);
        VBox inferior = new VBox(8, new Label("Reserva de energia del turno:"), energiaVisual, botones);
        inferior.setAlignment(Pos.CENTER);
        inferior.setPadding(new Insets(10, 0, 0, 0));
        return inferior;
    }

    private void actualizarEnergiaVisual() {
        if (energiaVisual == null) return;
        energiaVisual.getChildren().clear();
        for (int i = 0; i < energiaDisponible; i++) {
            energiaVisual.getChildren().add(crearIconoEnergia(32));
        }
        if (energiaDisponible == 0) energiaVisual.getChildren().add(new Label("Sin energia disponible"));
    }

    private ImageView crearIconoEnergia(double tamano) {
        java.io.InputStream recurso = getClass().getResourceAsStream("/imagenes/energia.png");
        ImageView icono;
        if (recurso != null) {
            icono = new ImageView(new Image(recurso));
            icono.setPreserveRatio(true);
            icono.setFitWidth(tamano);
            icono.setFitHeight(tamano);
        } else {
            icono = new ImageView();
            icono.setFitWidth(tamano);
            icono.setFitHeight(tamano);
            icono.setStyle("-fx-background-color: #f4f1de; -fx-border-color: #283044;"
                    + "-fx-border-radius: 50; -fx-background-radius: 50;");
        }
        return icono;
    }

    private void asignarEnergia(int posicion) {
        if (energiaDisponible <= 0 || !turno.asignarEnergia(posicion)) {
            estado.setText("No se puede asignar energia en ese espacio.");
            return;
        }
        energiaUsada = true;
        energiaDisponible--;
        refrescar();
    }

    private void jugarPrimeraCarta() {
        if (indiceManoSeleccionada < 0 || indiceManoSeleccionada >= turno.getTamanoMano()) {
            estado.setText("Selecciona una carta de tu mano primero.");
            return;
        }
        int indice = indiceManoSeleccionada;
        Carta carta = turno.obtenerCartaDeMano(indice);
        try {
            if (carta.esPokemon() && carta.getFase() == 1 && turno.ponerPokemonEnTablero(carta, numeroTurno)) {
                turno.sacarCartaDeMano(indice);
                estado.setText(carta.getNombre() + " entro al tablero.");
            } else if (carta.esCarta("Pocion") && turno.usarPocionEnActivo(indice)) {
                estado.setText("Pocion usada.");
            } else if (carta.esCarta("Superpocion") && turno.usarSuperPocion(indice, random)) {
                estado.setText("Superpocion usada.");
            } else if (carta.esCarta("Pokeball") && turno.usarPokeball(indice)) {
                estado.setText("Pokeball usada.");
            } else {
                estado.setText("La primera carta no se puede jugar ahora.");
            }
        } catch (ListaVaciaException exception) {
            estado.setText(exception.getMessage());
        }
        indiceManoSeleccionada = -1;
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
        atacante.quitarEnergia(costo);
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
        energiaDisponible = 1;
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

    private void actualizarListaMazo(ListView<String> lista, List<String> mazo) {
        lista.getItems().setAll(mazo);
    }

    private int contarPokemones(List<String> mazo) {
        int total = 0;
        for (String nombre : mazo) if (cartaPorNombre(nombre).esPokemon()) total++;
        return total;
    }

    private String nombreCarta(Carta carta) {
        return carta == null ? "ninguno" : "Carta " + carta.getNombre();
    }

    private int contarBanca(Jugador propietario) {
        int total = 0;
        for (int i = 0; i < 3; i++) if (propietario.getPokemonBanca(i) != null) total++;
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
