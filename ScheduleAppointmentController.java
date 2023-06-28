package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ScheduleAppointmentController {
	  @FXML
	    private Button btnDelete;

	    @FXML
	    private Button btnSchedule;

	    @FXML
	    private Button btnUpdate;

	    @FXML
	    private TableColumn<User, String> colDate;

	    @FXML
	    private TableColumn<User, String> colDescription;

	    @FXML
	    private TableColumn<User, String> colEnd;

	    @FXML
	    private TableColumn<User, Integer> colId;

	    @FXML
	    private TableColumn<User, String> colLocation;

	    @FXML
	    private TableColumn<User, String> colName;

	    @FXML
	    private TableColumn<User, String> colStart;

	    @FXML
	    private TableColumn<User, String> colTitle;
	    
	    @FXML
	    private TableColumn<User, String> colEmail;

	    @FXML
	    private Button logout;

	    @FXML
	    private TableView<User> tableOne;

	    @FXML
	    private ComboBox<String>tfDateSc;

	    @FXML
	    private TextField tfEmail;

	    @FXML
	    private ComboBox<String> tfNameSc;

	    @FXML
	    private ComboBox<String> tfStartEndSc;

   
    
    public void userLogOut(ActionEvent event) throws IOException {
        
    	
    	Main m = new Main();
        
       
       
        m.changeScene("sample.fxml");
     

    }
    
    @FXML
    void handleContactSelection(ActionEvent event) {
        // Handle the contact selection event
        String selectedContact = tfNameSc.getValue();
        if (selectedContact != null) {
            ObservableList<String> availableDates = getAvailableDatesFromDatabase(selectedContact);
            tfDateSc.setItems(availableDates);
        }
    }

    @FXML
    void handleDateSelection(ActionEvent event) {
        // Handle the date selection event
        String selectedContact = tfNameSc.getValue();
        String selectedDate = tfDateSc.getValue();
        if (selectedContact != null && selectedDate != null) {
            ObservableList<String> availableTimes = getAvailableTimesFromDatabase(selectedContact);
            tfStartEndSc.setItems(availableTimes);
        }
    }


@FXML
    public void initialize() {
        // Populate the combo box with contact names from the database
        ObservableList<String> contactNames = getContactNamesFromDatabase();
        tfNameSc.setItems(contactNames);
        
        // Initialize the TableView with the data from user_appointments table
        ObservableList<User> appointments = getUserAppointmentsFromDatabase();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableOne.setItems(appointments);
        
       
    }

    private ObservableList<String> getContactNamesFromDatabase() {
        ObservableList<String> contactNames = FXCollections.observableArrayList();
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                    "postgres", "1234");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT DISTINCT name FROM appointments");

            while (resultSet.next()) {
                String contactName = resultSet.getString("name");
                contactNames.add(contactName);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactNames;
    }

    private ObservableList<String> getAvailableDatesFromDatabase(String contactName) {
        ObservableList<String> availableDates = FXCollections.observableArrayList();
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                    "postgres", "1234");
            Statement statement = connection.createStatement();
            String query = "SELECT DISTINCT date FROM appointments WHERE name = '" + contactName + "'";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String date = resultSet.getString("date");
                availableDates.add(date);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availableDates;
    }

    private ObservableList<String> getAvailableTimesFromDatabase(String selectedContact) {
        ObservableList<String> availableTimes = FXCollections.observableArrayList();
        String selectedDate = tfDateSc.getValue();
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                    "postgres", "1234");
            Statement statement = connection.createStatement();
            String query = "SELECT starttime, endtime FROM appointments WHERE name = '" + selectedContact + "' AND date = '" + selectedDate + "'";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String startTime = resultSet.getString("starttime");
                String endTime = resultSet.getString("endtime");
                String timeRange = startTime + " - " + endTime;
                availableTimes.add(timeRange);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availableTimes;
    }

    @FXML
    void handleSchedule(ActionEvent event) {
        String selectedContact = tfNameSc.getValue();
        String selectedDate = tfDateSc.getValue();
        String selectedTimeRange = tfStartEndSc.getValue();
        String email = tfEmail.getText();

        if (selectedContact == null || selectedDate == null || selectedTimeRange == null || email.isEmpty()) {
            // Display an alert if any of the fields are not selected or if email is empty
            showAlert("Error", "Please select contact, date, and time range, and enter email");
            return;
        }

        String[] timeRangeParts = selectedTimeRange.split(" - ");
        String startTime = timeRangeParts[0];
        String endTime = timeRangeParts[1];

        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                    "postgres", "1234");

            // Retrieve the specific ID and name from the appointments table
            String selectQuery = "SELECT id, name FROM appointments WHERE name = ? AND date = ? AND starttime = ? AND endtime = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
            selectStatement.setString(1, selectedContact);
            selectStatement.setString(2, selectedDate);
            selectStatement.setString(3, startTime);
            selectStatement.setString(4, endTime);
            ResultSet resultSet = selectStatement.executeQuery();

            int appointmentId = -1;
            String appointmentName = "";
            if (resultSet.next()) {
                appointmentId = resultSet.getInt("id");
                appointmentName = resultSet.getString("name");
            }

            resultSet.close();
            selectStatement.close();

            if (appointmentId != -1) {
                // Check if the entry already exists in the user_appointments table
                String checkQuery = "SELECT COUNT(*) FROM user_appointments WHERE user_id = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setInt(1, appointmentId);
                ResultSet checkResult = checkStatement.executeQuery();

                int count = 0;
                if (checkResult.next()) {
                    count = checkResult.getInt(1);
                }

                checkResult.close();
                checkStatement.close();

                if (count == 0) {
                    // Insert the data into the user_appointments table
                    String insertQuery = "INSERT INTO user_appointments (user_id, name, email) VALUES (?, ?, ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                    insertStatement.setInt(1, appointmentId);
                    insertStatement.setString(2, appointmentName);
                    insertStatement.setString(3, email);
                    insertStatement.executeUpdate();
                    insertStatement.close();

                    showAlert("Success", "Appointment scheduled successfully!");

                    // Update the table view
                    User user = new User(appointmentId, "", "", "", "", "", "", appointmentName,email);
                    tableOne.getItems().add(user);
                } else {
                    showAlert("Error", "Duplicate entry. This appointment has already been scheduled.");
                }
            } else {
                showAlert("Error", "No matching appointment found");
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while scheduling the appointment");
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ObservableList<User> getUserAppointmentsFromDatabase() {
        ObservableList<User> appointments = FXCollections.observableArrayList();
        try {
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                    "postgres", "1234");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM user_appointments");

            while (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                // Assuming you have appropriate getters in the User class
                User user = new User(id, "", "", "", "", "", "", name, email);
                appointments.add(user);
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointments;
    }

    

    
}