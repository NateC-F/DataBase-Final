import javafx.application.Application;
import javafx.stage.Stage;



public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception
    {
        SceneManager.setStage(stage);
        SceneManager.switchTo(SceneID.ROOM_SUMMARY);
    }

    public static void main(String[] args) {launch(args);}
}
