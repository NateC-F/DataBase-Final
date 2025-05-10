import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager
{
    private static Stage stage;
    private static int resX = 1280;
    private static int resY = 720;

    public static void setStage(Stage s)
    {
        stage = s;
    }

    public static void switchTo(SceneID id)
    {
        Scene scene = null;

        try
        {
            switch (id)
            {
                case POS_SCREEN -> {
                        scene = new Scene(loadFXML("pos.fxml"),750,240);
                        stage.setTitle("Point Of Sale");
                }
                case ROOM_SUMMARY -> {
                    scene = new Scene(loadFXML("summary.fxml"),resX,resY);
                    stage.setTitle("Room Summaries");
                }
            }

            if(scene != null)
            {
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Parent loadFXML(String path) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(path));
        return loader.load();
    }
}
