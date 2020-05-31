package CarnivAPP.Users;

import CarnivAPP.Basket;
import CarnivAPP.DataBase.DataBaseCursorHolder;
import CarnivAPP.DataBase.DataBaseUtils;
import CarnivAPP.Interfaces.UserInterface;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User implements UserInterface {
    private String UserName;
    private String UserPassword;
    private Basket basket;

    public User(final String Name, final String Password) {
        this.UserName = Name;
        this.UserPassword = Password;
    }

    @Override
    public DataBaseCursorHolder fetchOrders(final Connection connection, final String[] filterArguments) throws SQLException {
        //Έχει το δικαίωμα ο Χρήστης;
        DataBaseCursorHolder cursor = DataBaseUtils.filterFromTable(connection, "Χρήστες", new String[]{"Δικαιώματα"},
                new String[]{String.format("Όνομα Χρήστη = '%s'", UserName)});
        cursor.getResults().next();

        final boolean userPrivilege = cursor.getResults().getBoolean(1);
        cursor.closeCursor();

        if (userPrivilege) {
            cursor = DataBaseUtils.innerJoinTables(connection, "Καλάθια", "Παραγγελίες", "Ταυτότητες Καλαθιών",
                    new String[]{"Ονόματα Προϊόντων", "Ποσότητα Προϊόντων", "Διεύθυνση"}, filterArguments);
            return cursor;
        } else {
            final List<String> nameAndFilterArguments = new ArrayList<>();
            nameAndFilterArguments.add(String.format("Παραγγελία Από τον Χρήστη = '%s'", UserName));
            if (filterArguments.length != 0) {
                nameAndFilterArguments.add("Και");
            }
            nameAndFilterArguments.addAll(Arrays.asList(filterArguments));
            cursor = DataBaseUtils.innerJoinTables(connection, "Καλάθια", "Παραγγελίες", "Ταυτότητες Καλαθιών",
                    new String[]{"Ονόματα Προϊόντων", "Ποσότητα Προϊόντων", "Διεύθυνση"}, nameAndFilterArguments.toArray(new String[0]));
            return cursor;
        }
    }

    @Override
    public DataBaseCursorHolder fetchInventory(final Connection connection, final String[] filterArguments) throws SQLException {
        return DataBaseUtils.innerJoinTables(connection, "Προϊόντα", "Απόθεμα", "Ταυτότητα Προϊόντος", new String[]{"Ταυτότητα Προϊόντος", "Όνομα Προϊόντος", "Βάρος Προϊόντος", "Τιμή Προϊόντος", "Ποσότητα Προϊόντων"},
                filterArguments);
    }

    public String getUserName() {
        return UserName;
    }

    public String getUserPassword() {
        return UserPassword;
    }

    public void setBasket(final Basket basket) {
        this.basket = basket;
    }

    public Basket getBasket() {
        return basket;
    }
}
