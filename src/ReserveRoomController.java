import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;


public class ReserveRoomController implements Initializable {
    @FXML
    private Label roomNum;

    @FXML
    public void setRoomNumber(int roomNumber) {
        roomNum.setText(String.valueOf(roomNumber));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}
