package CarnivAPP.GUI;

import CarnivAPP.Basket;
import CarnivAPP.Inventory;
import CarnivAPP.Order;
import CarnivAPP.DataBase.DataBaseCursorHolder;
import CarnivAPP.DataBase.DataBaseUtils;
import CarnivAPP.Users.Administrator;
import CarnivAPP.Users.Client;
import CarnivAPP.Users.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static CarnivAPP.GUI.SceneHolder.createAdminPaneScene;
import static CarnivAPP.GUI.SceneHolder.createClientPaneScene;

public class RegisterButton extends ChangeableScene {

    public static Button createRegisterButton(final Stage primaryStage, final Connection connection, final User user, final Inventory shopInventory,
                                           final Basket clientBasket, final List<Order> userOrders, final List<User> userList) {
        final Button btn = new Button();
        btn.setText("Gain access to the Shop");

        btn.setOnAction(mainEvent -> registerAction(primaryStage, connection, user, shopInventory, clientBasket, userOrders, userList));
        return btn;
    }

    private static void registerAction(final Stage primaryStage, final Connection connection, final User user, final Inventory shopInventory,
                                    final Basket clientBasket, final List<Order> userOrders, final List<User> userList) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Εγγραφή");
        alert.setHeaderText("Διάλογος Εγγραφής");

        final Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Εγγραφή στο CarnivAPP");
        dialog.setContentText("Εισάγετε τα στοιχεία σας: ");
        dialog.initModality(Modality.NONE);

        //Σετάρισμα του register button.
        final ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        //Δημιουργία όλων των απαραίτητων πεδίων.
        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        final TextField UserName = new TextField();
        UserName.setPromptText("e.g. m03j");
        UserName.setId("user-name");

        final PasswordField UserPassword = new PasswordField();
        UserPassword.setPromptText("xxxx");
        UserPassword.setId("user-passwd");

        grid.add(new Label("Username: "), 0, 0);
        grid.add(UserName, 1, 0);
        grid.add(new Label("Password: "), 0, 1);
        grid.add(UserPassword, 1, 1);

        //Ενεργοποίηση ή απενεργοποίηση του register button ανάλογα με το αν έχει δοθεί username.
        final Node registerButton = dialog.getDialogPane().lookupButton(registerButtonType);
        registerButton.setDisable(true);

        //Επαλήθευση εγκυρότητας για τα στοιχεία.
        UserName.textProperty().addListener((observable, oldValue, newValue) -> registerButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> UserName.requestFocus());

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.show();

        registerButton.addEventFilter(EventType.ROOT, e -> {
            try {
                userAuthentication(primaryStage, e, dialog, UserName.getText(), UserPassword.getText(), connection, user, shopInventory,
                        clientBasket, userOrders, userList);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static void userAuthentication(final Stage primaryStage, final Event e, final Dialog dialog, final String UserName,
                                           final String UserPassword, final Connection connection, User user, final Inventory shopInventory,
                                           final Basket clientBasket, final List<Order> userOrders, final List<User> userList) throws SQLException {
        if (e.getEventType().equals(ActionEvent.ACTION)) {
            e.consume();
            if (isUserAllowed(UserName, UserPassword, connection)) {
                if (isUserAdmin(UserName, UserPassword, connection)) {
                    user = new Administrator(UserName, UserPassword);
                    changeScene(primaryStage, createAdminPaneScene(connection, shopInventory, userOrders, userList, user));
                } else {
                    user = new Client(UserName, UserPassword);
                    changeScene(primaryStage, createClientPaneScene(connection, shopInventory, userOrders, clientBasket, user));
                }
                dialog.close();
            } else {
                final ShakeTransition animation = new ShakeTransition(dialog.getDialogPane(), t -> dialog.show());
                animation.playFromStart();
            }
        }
    }

    private static boolean isUserAllowed(final String UserName, final String UserPassword, final Connection connection) throws SQLException {
        final DataBaseCursorHolder cursor = DataBaseUtils.filterFromTable(connection, "Users", new String[]{"user_name"},
                new String[]{String.format("user_name = '%s'", UserName), "AND", String.format("user_password = '%s'", UserPassword)});
        while (cursor.getResults().next()) {
            if (cursor.getResults().getString(1).equals(UserName)) {
                cursor.closeCursor();
                return true;
            } else {
                cursor.closeCursor();
                return false;
            }
        }
        return false;
    }

    private static boolean isUserAdmin(final String UserName, final String UserPassword, final Connection connection) throws SQLException {
        final DataBaseCursorHolder cursor = DataBaseUtils.filterFromTable(connection, "Users", new String[]{"privileges"},
                new String[]{String.format("user_name = '%s'", UserName), "AND", String.format("user_password = '%s'", UserPassword)});
        cursor.getResults().next();
        if (cursor.getResults().getBoolean(1)) {
            cursor.closeCursor();
            return true;
        } else {
            cursor.closeCursor();
            return false;
        }
    }

}
