import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;


public class ReserveRoomController implements Initializable {
    @FXML
    private Label roomNum;
    @FXML
    private TextArea nameBox;
    private int roomNumberInt;
    private SummaryController sC;

    public void setSC(SummaryController controller)
    {
        sC=controller;
    }


    @FXML
    public void setRoomNumber(int roomNumber) {
        roomNumberInt = roomNumber;
        roomNum.setText("Room Number: "+roomNumber);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void saveRoom()
    {
        if (nameBox.getText().isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Empty Name");
            alert.setContentText("There Was No Name Entered");
            alert.setHeaderText("Please Enter A Name");
            alert.showAndWait();
        }
        else {
            try(Connection connection = JDBC.getConnection())
            {
                String query = "INSERT INTO Visitors (visitor_name, room_id) VALUES (?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, nameBox.getText());
                statement.setInt(2, roomNumberInt);
                statement.executeUpdate();
            }
            catch (Exception e)
            {

            }
            sC.getRoomSummary();
            Stage stage = (Stage) roomNum.getScene().getWindow();
            stage.close();

        }
    }

}
