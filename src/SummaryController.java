import com.sun.javafx.iio.common.PushbroomScaler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class SummaryController implements Initializable {
    @FXML
    private ComboBox<Integer> roomBox;
    @FXML
    private ComboBox<String> authorSort;
    @FXML
    private ComboBox<String> genreSort;
    @FXML
    private Label roomName;
    @FXML
    private Label roomOccupant;
    @FXML
    private Label bookList;
    @FXML
    private Label foodList;
    @FXML
    private Label roomFee;
    @FXML
    private Label bookFee;
    @FXML
    private Label foodFee;
    @FXML
    private Label runningTotal;
    @FXML
    private Text nameText;
    @FXML
    private Button reserveButton;
    @FXML
    private Button reserveIt;
    @FXML
    private TextArea nameBox;
    @FXML
    private VBox totalSummary;
    @FXML
    private TableView<Book> listOfBooks = new TableView<>();
    @FXML
    private TableColumn<Book,String> names = new TableColumn<>();
    @FXML
    private TableColumn<Book,String> genres = new TableColumn<>();
    @FXML
    private TableColumn<Book,String> copies = new TableColumn<>();
    private ObservableList<Book> books = FXCollections.observableArrayList();
    private String query;
    private PreparedStatement statement;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setRoomBox();
        initializeListOfBooks();
        setAuthorSort();
        setGenreSort();
        makeSummaryInvisible();
    }

    public void initializeListOfBooks()
    {
        names.setCellValueFactory(new PropertyValueFactory<>("name"));
        genres.setCellValueFactory(new PropertyValueFactory<>("genres"));
        copies.setCellValueFactory(new PropertyValueFactory<>("copiesLeft"));
        try(Connection connection = JDBC.getConnection()) {
            query = "Select * FROM book_inventory";
            statement =connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            books.clear();
            while(rs.next())
            {
               String name = rs.getString(1);
               int amount = rs.getInt(2);
               String genre = rs.getString(3);
               books.add(new Book(name,genre,amount));
            }

        } catch (Exception e) {

        }

        listOfBooks.setItems(books);
    }
    public void showAuthorSort()
    {
        genreSort.setVisible(false);
        authorSort.setVisible(true);
    }
    public void showGenreSort()
    {
        authorSort.setVisible(false);
        genreSort.setVisible(true);
    }
    public void clearSorting()
    {
        authorSort.setVisible(false);
        genreSort.setVisible(false);
        initializeListOfBooks();
    }
    public void setRoomBox()
    {
        for (int i=1; i <11; i++)
        {
            roomBox.getItems().add(i);
        }
    }
    public void setGenreSort()
    {
        query="SELECT genre_name FROM genre";
        try(Connection connection = JDBC.getConnection())
        {
            statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                genreSort.getItems().add(rs.getString(1));
            }
        }
        catch (Exception e){}
    }
    public void setAuthorSort()
    {
        query="SELECT author_name FROM authors";
        try(Connection connection = JDBC.getConnection())
        {
            statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                authorSort.getItems().add(rs.getString(1));
            }
        }
        catch (Exception e){}
    }
    public void sortByAuthor()
    {
        names.setCellValueFactory(new PropertyValueFactory<>("name"));
        genres.setCellValueFactory(new PropertyValueFactory<>("genres"));
        copies.setCellValueFactory(new PropertyValueFactory<>("copiesLeft"));
        try(Connection connection = JDBC.getConnection())
        {
            query = "Select *" + "FROM book_inventory JOIN book_author ON book_inventory.book_id = book_author.book_id " +
                    "JOIN authors ON book_author.author_id = authors.author_id WHERE author_name = ?";
            statement =connection.prepareStatement(query);
            statement.setString(1,authorSort.getValue());
            ResultSet rs = statement.executeQuery();// 1 2 3
            books.clear();
            while(rs.next())
            {
                String name = rs.getString(1);
                int amount = rs.getInt(2);
                String genre = rs.getString(3);
                books.add(new Book(name,genre,amount));
            }
        listOfBooks.setItems(books);

        }
        catch (Exception e)
        {
        }
    }
    public void sortByGenre()
    {
        names.setCellValueFactory(new PropertyValueFactory<>("name"));
        genres.setCellValueFactory(new PropertyValueFactory<>("genres"));
        copies.setCellValueFactory(new PropertyValueFactory<>("copiesLeft"));
        try(Connection connection = JDBC.getConnection())
        {
            query = "Select * FROM book_inventory WHERE Genres like ?";
            statement =connection.prepareStatement(query);
            statement.setString(1,"%"+genreSort.getValue()+"%");
            ResultSet rs = statement.executeQuery();
            books.clear();
            while(rs.next())
            {
                String name = rs.getString(1);
                int amount = rs.getInt(2);
                String genre = rs.getString(3);
                books.add(new Book(name,genre,amount));
            }
            listOfBooks.setItems(books);

        }
        catch (Exception e)
        {
        }
    }

    public void getRoomTotal(){
        try(Connection connection = JDBC.getConnection() ){
            query = "CALL room_total_cost(?);";
            statement = connection.prepareStatement(query);
            statement.setInt(1,roomBox.getValue());
            ResultSet rs = statement.executeQuery();
            rs.next();
            roomFee.setText("Room Fee: $" + rs.getInt(1));
            bookFee.setText("Book Fee: $" +rs.getInt(2));
            foodFee.setText("Food Fee: $" +rs.getDouble(3));
            runningTotal.setText("Total: $" + rs.getDouble(4));
        }
        catch (Exception e) {
            System.out.println("Not working");
        }
    }
    public void makeSummaryInvisible()
    {
        totalSummary.setVisible(false);
    }

    public void getRoomSummary(){
        if (!totalSummary.isVisible())
            totalSummary.setVisible(true);
        reserveButton.setVisible(false);
        try(Connection connection = JDBC.getConnection() ){
            query = "Select * FROM room_summary WHERE Room_Number =?";
            statement = connection.prepareStatement(query);
            statement.setInt(1,roomBox.getValue());
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                roomName.setText("Room " + rs.getInt(1));
                roomOccupant.setText(rs.getString(2));
                if (rs.getString(3) == null || rs.getString(3).trim().isEmpty()) {
                    bookList.setText("No Books Have Been Checked Out");
                } else {
                    bookList.setText(rs.getString(3));
                }
                if (rs.getString(4) == null || rs.getString(4).trim().isEmpty()) {
                    foodList.setText("No Food Has Been Ordered");
                } else {
                    foodList.setText(rs.getString(4));
                }
                getRoomTotal();
            }
            else
            {
                roomName.setText("Room "+roomBox.getValue());
                roomOccupant.setText("This Room Is Empty");
                reserveButton.setVisible(true);
                bookList.setText("");
                foodList.setText("");
                roomFee.setText("");
                bookFee.setText("");
                foodFee.setText("");
                runningTotal.setText("");
            }

        }
        catch (Exception e)
        {

        }
    }
    public void reserveRoom()
    {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("reserveroom.fxml"));
            Parent root = loader.load();
            ReserveRoomController controller = loader.getController();
            controller.setRoomNumber(roomBox.getValue());
            controller.setSC(this);
            Stage newStage = new Stage();
            newStage.setTitle("Reserve Room");
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToPOS()
    {
        SceneManager.switchTo(SceneID.POS_SCREEN);
    }


}
