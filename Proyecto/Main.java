import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        VistaJuego vista = new VistaJuego();
        vista.mostrar(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}