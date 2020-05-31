package CarnivAPP.GUI;

import CarnivAPP.Basket;
import CarnivAPP.Inventory;
import CarnivAPP.Order;
import CarnivAPP.Users.Administrator;
import CarnivAPP.Users.Client;
import CarnivAPP.Users.User;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.sql.Connection;
import java.util.List;

import static CarnivAPP.GUI.GuiWindowConsts.*;

public class SceneHolder {

    public static Pane createClientPaneScene(final Connection connection, final Inventory shopInventory, final List<Order> userOrders,
                                             final Basket clientBasket, final User user) {
        //Πρέπει να φορτωθεί το Inventory του Shop.
        GuiDbUtils.loadDataToInventory(connection, shopInventory, user);
        //Πρέπει να φορτωθούν οι παραγγελίες των Χρηστών.
        GuiDbUtils.loadDataToOrders(user, connection, userOrders);
        //Πρέπει να φορτωθεί το καλάθι του Πελάτη αν έχει αποθηκευθεί.
        GuiDbUtils.loadSavedBasket((Client) user, connection, clientBasket);

        //Δημιουργία σκηνής για Πελάτη.
        user.setBasket(clientBasket);

        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab inventoryTab = new Tab();
        inventoryTab.setText("Inventory");
        final HBox inventoryBox = new HBox(HBOX_SPACING);
        inventoryBox.setAlignment(Pos.CENTER);
        inventoryBox.getChildren().add(InventoryScene.createMainInventoryBox(shopInventory, user, connection));
        inventoryTab.setContent(inventoryBox);

        final Tab orderHistoryTab = new Tab();
        orderHistoryTab.setText("Order History");
        final HBox orderHistoryBox = new HBox(HBOX_SPACING);
        orderHistoryBox.setAlignment(Pos.CENTER);
        orderHistoryBox.getChildren().add(HistoryScene.syncTablesIntoOneTable(HistoryScene.createOrderTableView(userOrders), HistoryScene.createTotalOrderTableView(userOrders)));
        orderHistoryTab.setContent(orderHistoryBox);

        tabPane.getTabs().addAll(inventoryTab, orderHistoryTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        //Δέσμευση για να πάρει τον διαθέσιμο χώρο.
        borderPane.prefHeightProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_HEIGHT));
        borderPane.prefWidthProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_WIDTH));
        borderPane.setCenter(tabPane);

        return borderPane;
    }

    public static Pane createAdminPaneScene(final Connection connection, final Inventory shopInventory, final List<Order> userOrders,
                                            final List<User> userList, final User user) {
        //Πρέπει να φορτωθεί το Inventory του Shop.
        GuiDbUtils.loadDataToInventory(connection, shopInventory, user);
        //Πρέπει να φορτωθούν οι παραγγελίες των Χρηστών.
        GuiDbUtils.loadDataToOrders(user, connection, userOrders);
        //Πρέπει να φορτωθεί η λίστα Χρηστών.
        GuiDbUtils.loadDataToUserList(connection, userList);

        //Δημιουργία σκηνής για Διαχειριστή.
        final TabPane tabPane = new TabPane();

        final BorderPane borderPane = new BorderPane();

        final Tab inventoryTab = new Tab();
        inventoryTab.setText("Inventory");
        final HBox inventoryBox = new HBox(HBOX_SPACING);
        inventoryBox.setAlignment(Pos.CENTER);
        inventoryBox.getChildren().add(InventoryScene.createMainInventoryBox(shopInventory, user, connection));
        inventoryTab.setContent(inventoryBox);

        final Tab orderHistoryTab = new Tab();
        orderHistoryTab.setText("Order History");
        final HBox orderHistoryBox = new HBox(HBOX_SPACING);
        orderHistoryBox.setAlignment(Pos.CENTER);
        orderHistoryBox.getChildren().add(HistoryScene.syncTablesIntoOneTable(HistoryScene.createOrderTableView(userOrders), HistoryScene.createTotalOrderTableView(userOrders)));
        orderHistoryTab.setContent(orderHistoryBox);

        final Tab controlUsersTab = new Tab();
        controlUsersTab.setText("System Users");
        final HBox controlUsersBox = new HBox(HBOX_SPACING);
        controlUsersBox.setAlignment(Pos.CENTER);
        controlUsersBox.getChildren().add(ControlUsersScene.createControlTable(userList, (Administrator) user, connection));
        controlUsersTab.setContent(controlUsersBox);

        tabPane.getTabs().addAll(inventoryTab, orderHistoryTab, controlUsersTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        //Δέσμευση για να πάρει τον διαθέσιμο χώρο.
        borderPane.prefHeightProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_HEIGHT));
        borderPane.prefWidthProperty().bind(new ReadOnlyDoubleWrapper(WINDOW_WIDTH));
        borderPane.setCenter(tabPane);

        return borderPane;
    }
}
