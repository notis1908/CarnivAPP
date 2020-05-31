package CarnivAPP.GUI;

import CarnivAPP.DataBase.DataBaseCursorHolder;
import CarnivAPP.DataBase.DataBaseUtils;
import CarnivAPP.Users.Administrator;
import CarnivAPP.Users.Client;
import CarnivAPP.Users.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.table.TableFilter;
import org.controlsfx.control.table.TableRowExpanderColumn;
import org.controlsfx.tools.Utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static CarnivAPP.GUI.GuiWindowConsts.WINDOW_HEIGHT;
import static CarnivAPP.GUI.GuiWindowConsts.WINDOW_WIDTH;

public class ControlUsersScene {
    private static final String USER_NAME_COLUMN = "User name";
    private static final String USER_PRIVILEGE_COLUMN = "User privilege";

    public static VBox createControlTable(final List<User> Users, final Administrator admin, final Connection connection) {
        final ObservableList<User> observableUserList = FXCollections.observableArrayList(Users);
        final TableView<User> userTableView = new TableView<>(observableUserList);
        userTableView.setId("Users-table");
        userTableView.setPrefWidth(WINDOW_WIDTH);
        userTableView.setPrefHeight(WINDOW_HEIGHT);

        final VBox controlTableBox = new VBox();
        controlTableBox.setSpacing(5);
        controlTableBox.setPadding(new Insets(5, 5, 5, 5));

        final TableColumn<User, String> userNameColumn = new TableColumn<>(USER_NAME_COLUMN);
        userNameColumn.setId(USER_NAME_COLUMN);
        userNameColumn.setCellValueFactory(item -> new SimpleStringProperty(item.getValue().getUserName()));

        final TableColumn<User, String> userPrivilegeColumn = new TableColumn<>(USER_PRIVILEGE_COLUMN);
        userPrivilegeColumn.setId(USER_PRIVILEGE_COLUMN);
        userPrivilegeColumn.setCellValueFactory(item -> {
            if (item.getValue() instanceof Administrator) return new SimpleStringProperty("True");
            else return new SimpleStringProperty("False");
        });

        userTableView.getColumns().setAll(userNameColumn, userPrivilegeColumn);
        addAdminRowExpander(userTableView, Users, observableUserList, admin, connection);
        userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        controlTableBox.getChildren().setAll(userTableView, createAddUserBox(userNameColumn, Users, observableUserList, admin, connection));

        final TableFilter<User> filter = TableFilter.forTableView(userTableView).lazy(false).apply();

        return controlTableBox;
    }

    private static Button createDeleteButton(final String buttonText, final String notificationTextSuccess, final String notificationTextError,
                                             final List<User> Users, final ObservableList<User> observableUsers, final HBox editor,
                                             final TableRowExpanderColumn.TableRowDataFeatures<User> param, final Administrator admin,
                                             final Connection connection) {
        final Button deleteFromBasket = new Button();
        deleteFromBasket.setText(buttonText);
        deleteFromBasket.setOnMouseClicked(mouseEvent -> {
            try {
                Users.remove(param.getValue());
                observableUsers.remove(param.getValue());

                //Διαγραφή Χρήστη
                admin.deleteUser(connection, param.getValue().getUserName());

                Notifications.create()
                        .darkStyle()
                        .title("Info")
                        .text(notificationTextSuccess)
                        .position(Pos.CENTER)
                        .owner(Utils.getWindow(editor))
                        .hideAfter(Duration.seconds(2))
                        .showConfirm();

            } catch (Exception e) {
                Notifications.create()
                        .darkStyle()
                        .title("Error")
                        .text(notificationTextError)
                        .position(Pos.CENTER)
                        .owner(Utils.getWindow(editor))
                        .hideAfter(Duration.seconds(4))
                        .showError();
                e.printStackTrace();
            }
        });
        return deleteFromBasket;
    }

    private static HBox createAddUserBox(final TableColumn userNameColumn, final List<User> Users, final ObservableList items,
                                         final Administrator admin, final Connection connection) {
        final HBox addProductBox = new HBox();
        final TextField addUserName = new TextField();
        addUserName.setPromptText("Εισάγετε UserName");
        addUserName.setId("user-name");
        addUserName.setMaxWidth(userNameColumn.getPrefWidth());

        final TextField addUserPassword = new TextField();
        addUserPassword.setPromptText("Εισάγετε Password");
        addUserPassword.setId("user-password");
        addUserPassword.setMaxWidth(userNameColumn.getPrefWidth());

        final TextField addUserPrivilege = new TextField();
        addUserPrivilege.setPromptText("Εισάγετε Privileges");
        addUserPrivilege.setId("user-privilege");
        addUserPrivilege.setMaxWidth(userNameColumn.getPrefWidth());

        final CheckBox enablePrivilege = new CheckBox("Διαχειριστής;");

        final Button addNewUserButton = new Button("Προσθήκη Χρήστη");
        addNewUserButton.setOnMouseClicked(mouseEvent -> {
            final String newUserName = addUserName.getText();
            final String newUserPassword = addUserPassword.getText();
            if (!newUserName.isEmpty() && !newUserPassword.isEmpty()) {
                //Επιβεβαίωση ότι ο Χρήστης δεν υπάρχει στη βάση δεδομένων.
                try {
                    final DataBaseCursorHolder cursor = DataBaseUtils.filterFromTable(connection, "Users", new String[]{"user_name"},
                            new String[]{String.format("user_name='%s'", newUserName)});
                    if (!cursor.getResults().next()) {
                        items.removeAll(Users);
                        if (enablePrivilege.isSelected()) Users.add(new Administrator(newUserName, newUserPassword));
                        else Users.add(new Client(newUserName, newUserPassword));
                        items.addAll(Users);

                        //Δημιουργία και αποθήκευση νέου Χρήστη.
                        admin.createUser(connection, newUserName, newUserPassword, enablePrivilege.isSelected());

                        addUserName.clear();
                        addUserPassword.clear();
                        addUserPrivilege.clear();
                        enablePrivilege.setSelected(false);
                    } else {
                        Notifications.create()
                                .darkStyle()
                                .title("Σφάλμα")
                                .text("Το UserName είναι πιασμένο. Δοκιμάστε κάποιο άλλο.")
                                .position(Pos.CENTER)
                                .owner(Utils.getWindow(addProductBox))
                                .hideAfter(Duration.seconds(2))
                                .showError();
                    }
                    cursor.closeCursor();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                Notifications.create()
                        .darkStyle()
                        .title("Σφάλμα")
                        .text("Κάποιο από τα πεδία είναι κενά. Σιγουρευτείτε ότι έχετε συμπληρώσει σωστά όλα τα πεδία.")
                        .position(Pos.CENTER)
                        .owner(Utils.getWindow(addProductBox))
                        .hideAfter(Duration.seconds(2))
                        .showError();
            }
        });
        addProductBox.getChildren().addAll(addUserName, addUserPassword, enablePrivilege, addNewUserButton);
        addProductBox.setSpacing(3);

        return addProductBox;
    }

    private static void addAdminRowExpander(final TableView table, final List<User> Users, final ObservableList<User> observableUsers,
                                            final Administrator admin, final Connection connection) {
        final TableRowExpanderColumn<User> expander = new TableRowExpanderColumn<>(param -> {
            final HBox editor = new HBox(10);
            editor.getChildren().addAll(createDeleteButton("Διαγραφή Χρήστη", "Ο Χρήστης διαγράφηκε επιτυχώς",
                    "Κάτι πήγε στραβά κατά την διαγραφή του Χρήστη", Users, observableUsers, editor, param, admin, connection));
            return editor;
        });
        expander.setText("Λεπτομέρειες");
        expander.setId("Λεπτομέρειες");

        table.getColumns().add(expander);
    }
}
