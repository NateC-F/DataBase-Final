import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class POSController implements Initializable {
    @FXML
    private ComboBox<Food> foodBox;
    @FXML
    private ComboBox<Integer> roomBoxFood;
    @FXML
    private ComboBox<Integer> roomBoxCheckOut;
    @FXML
    private ComboBox<Integer> roomBoxBook;
    @FXML
    private ComboBox<Book> selectBookBox;
    @FXML
    private Label foodBuying;
    @FXML
    private Label roomTotal;
    @FXML
    private HBox orderFoodScreen;
    @FXML
    private TextField paymentBox;
    @FXML
    private VBox checkOutBox;
    @FXML
    private HBox orderBookBox;
    @FXML
    private Button orderButton;
    private int quantity;
    private double roomCost;


    public void switchToSummary ()
    {
        SceneManager.switchTo(SceneID.ROOM_SUMMARY);
    }
    public void addOne()
    {
        if (foodBox.getValue()!=null) {
            showOrderButton();
            if (quantity==50)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Quantity Below 50");
                alert.setContentText("You Cannot Have A Quantity More Than 50");
                alert.setHeaderText("Please Choose Another Option Or Decrease Quantity");
                alert.showAndWait();
            } else {
                quantity++;
                selectFood();
            }
        }
    }
    public void subOne()
    {
        if (foodBox.getValue()!=null) {
            if (quantity == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Quantity Below 0");
                alert.setContentText("You Cannot Have A Quantity Less Than 0");
                alert.setHeaderText("Please Choose Another Option Or Increase Quantity");
                alert.showAndWait();
            } else {
                quantity--;
                if (quantity == 0)
                    orderButton.setVisible(false);
                selectFood();
            }
        }
    }
    public void showOrderButton()
    {
        if (quantity>0 && foodBox.getValue()!=null && roomBoxFood.getValue()!=null)
            orderButton.setVisible(true);
    }


    public void setFoodBox() {
        String query = "SELECT * FROM food";
        try(Connection connection = JDBC.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next())
            {
                foodBox.getItems().add(new Food(rs.getInt(1),rs.getDouble(3),rs.getString(2)));
            }
        }
        catch (Exception e)
        {

        }
    }

    public void setRoomBoxFood() {
        String query = "SELECT room_id FROM visitors WHERE check_out_time is null ORDER BY room_id";
        try (Connection connection = JDBC.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next())
                roomBoxFood.getItems().add(rs.getInt(1));
        }
        catch (Exception e)
        {

        }

    }
    public void setRoomBoxCheckOut() {
        String query = "SELECT room_id FROM visitors WHERE check_out_time is null ORDER BY room_id";
        try (Connection connection = JDBC.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next())
                roomBoxCheckOut.getItems().add(rs.getInt(1));
        }
        catch (Exception e)
        {

        }

    }
    public void setRoomBoxBook() {
        String query = "SELECT room_id FROM visitors WHERE check_out_time is null ORDER BY room_id";
        try (Connection connection = JDBC.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next())
                roomBoxBook.getItems().add(rs.getInt(1));
        }
        catch (Exception e)
        {

        }

    }


    public void selectFood()
    {
        foodBuying.setText(foodBox.getValue().getName() +" Quantity: " + quantity + "x Subtotal: " + foodBox.getValue().getCost()*quantity);
        showOrderButton();
    }
    public void showOrderFood()
    {
        orderFoodScreen.setVisible(true);
        checkOutBox.setVisible(false);
        orderBookBox.setVisible(false);
    }

    public void orderFood()
    {
        String query = "CALL order_food(?,?,?) ";//room id, food id, quantity
        try(Connection connection = JDBC.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,roomBoxFood.getValue());
            statement.setInt(2,foodBox.getValue().getId());
            statement.setInt(3,quantity);
            statement.executeUpdate();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Made");
            alert.setContentText("A Charge For "+foodBox.getValue().getCost()*quantity +" Has Been Added");
            alert.setHeaderText("Order Successfully Made To Room: "+ roomBoxFood.getValue());
            alert.showAndWait();
            resetAfterOrder();

        }
        catch (Exception e)
        {

        }
    }
    public void showCheckOut()
    {
        orderFoodScreen.setVisible(false);
        checkOutBox.setVisible(true);
        orderBookBox.setVisible(false);
    }

    public void getTotal()
    {
        try(Connection connection = JDBC.getConnection() ){
            String query = "CALL room_total_cost(?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,roomBoxCheckOut.getValue());
            ResultSet rs = statement.executeQuery();
            rs.next();
            roomTotal.setText("Total: $" + rs.getDouble(4));
            roomCost = rs.getDouble(4);
        }
        catch (Exception e) {
        }
    }
    public void selectBook()
    {
        orderFoodScreen.setVisible(false);
        checkOutBox.setVisible(false);
        orderBookBox.setVisible(true);
    }
    public void setSelectBookBox()
    {
        String query = "SELECT * FROM books ORDER BY book_name ASC";
        try(Connection connection = JDBC.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                selectBookBox.getItems().add(new Book(rs.getInt(1),rs.getString(2), rs.getInt(3)));
            }
        }
        catch (Exception e)
        {

        }
    }

    public void orderBook()
    {
        if ( selectBookBox.getValue()==null || roomBoxBook.getValue()==null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Select A Book And A Room");
            alert.setContentText("Select A Book And A Room");
            alert.setHeaderText("Select A Book And A Room");
            alert.showAndWait();
        }
        else
        {
            String query = "CALL order_book(?, ?);"; //Room, Book_ID
            try(Connection connection = JDBC.getConnection())
            {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1,roomBoxBook.getValue());
                statement.setInt(2,selectBookBox.getValue().getId());
                statement.executeUpdate();
                resetAfterOrder();
            }
            catch (SQLException e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Checking Out Book");
                alert.setContentText(e.getMessage());
                alert.setHeaderText("Please Inform The Customer: ");
                alert.showAndWait();
            }

        }
    }

    public void checkOut()
    {
        if (!paymentBox.getText().isEmpty()) {
            if (isValidDouble(paymentBox.getText())) {
                double payment = Double.parseDouble(paymentBox.getText());
                if (payment < roomCost) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Not Enough $ Paid");
                    alert.setContentText("Customer Needs to Pay More ");
                    alert.setHeaderText("Amount Paid Cannot Be Less Than " + roomCost);
                    alert.showAndWait();
                } else {
                    removeRoom();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Payment Successful");
                    alert.setContentText("Change Due: $" + (payment - roomCost));
                    alert.setHeaderText("Payment Successful");
                    alert.showAndWait();
                    resetAfterOrder();
                }
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Enter A Valid Number Please");
                alert.setContentText("Enter A Valid Number Please");
                alert.setHeaderText("Enter A Valid Number Please ");
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Enter A Valid Number Please");
            alert.setContentText("Enter A Valid Number Please");
            alert.setHeaderText("Enter A Valid Number Please ");
            alert.showAndWait();
        }

    }

    public void removeRoom()
    {
        String query = "CALL check_out(?)";
        try (Connection connection = JDBC.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,roomBoxCheckOut.getValue());
            statement.executeUpdate();
        }
        catch (Exception e)
        {

        }
    }
    public boolean isValidDouble(String text) {
        return text.matches("^\\d*\\.?\\d+$");
    }

    private void resetAfterOrder()
    {
        quantity = 0;
        orderFoodScreen.setVisible(false);
        checkOutBox.setVisible(false);
        orderBookBox.setVisible(false);
        roomBoxBook.getItems().clear();
        roomBoxCheckOut.getItems().clear();
        roomBoxFood.getItems().clear();
        setRoomBoxFood();
        setRoomBoxCheckOut();
        setRoomBoxBook();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        quantity = 0;
        orderFoodScreen.setVisible(false);
        checkOutBox.setVisible(false);
        orderBookBox.setVisible(false);
        setFoodBox();
        setSelectBookBox();
        setRoomBoxFood();
        setRoomBoxCheckOut();
        setRoomBoxBook();

    }
}
