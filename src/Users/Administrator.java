package CarnivAPP.Users;

import CarnivAPP.DataBase.DataBaseCursorHolder;
import CarnivAPP.DataBase.DataBaseUtils;
import CarnivAPP.Exceptions.InventoryException;
import CarnivAPP.Products.Product;

import java.sql.Connection;
import java.sql.SQLException;

public class Administrator extends User {

    public Administrator(final String Name, final String Password) {
        super(Name, Password);
    }

    public double getTotalPriceOfInventory(final Connection connection) throws SQLException {
        final DataBaseCursorHolder cursor = DataBaseUtils.innerJoinTables(connection, "Προϊόντα", "Απόθεμα", "Ταυτότητα Προϊόντος",
                new String[]{"Τιμή Προϊόντος", "Ποσότητα Προϊόντων"}, new String[]{});
        double total = 0.0;
        while (cursor.getResults().next()) {
            total += cursor.getResults().getDouble(1) * cursor.getResults().getDouble(2);
        }
        return total;
    }

    public double getTotalPriceOfAllOrders(final Connection connection) throws SQLException {
        final DataBaseCursorHolder cursor = DataBaseUtils.filterFromTable(connection, "Παραγγελίες", new String[]{"Συνολικό Κόστος"}, new String[]{});
        double total = 0.0;
        while (cursor.getResults().next()) {
            total += cursor.getResults().getDouble(1);
        }
        return total;
    }

    public void increaseProductAmountInInventory(final Connection connection, final Product product, final int amount)
            throws SQLException {
        final DataBaseCursorHolder cursor = DataBaseUtils.innerJoinTables(connection, "Προϊόντα", "Απόθεμα", "Ταυτότητα Προϊόντος",
                new String[]{"Ταυτότητα Προϊόντος", "Ποσότητα Προϊόντων"}, new String[]{String.format("Όνομα Προϊόντος = '%s'", product.getName())});

        while (cursor.getResults().next()) {
            final int productId = cursor.getResults().getInt(1);
            final int productAmount = cursor.getResults().getInt(2);
            final int newAmount = productAmount + amount;
            DataBaseUtils.updateTable(connection, "Απόθεμα", new String[]{"Ποσότητα Προϊόντων"}, new String[]{Integer.toString(newAmount)},
                    new String[]{String.format("Ταυτότητα Προϊόντος = %d", productId)});

        }
    }

    public void addNewProductToInventory(final Connection connection, final Product product, final int amount) throws SQLException {
        DataBaseUtils.insertSpecificIntoTable(connection, "Products", new String[]{"product_name", "product_weight", "product_price"},
                new String[]{String.format("'%s'", product.getName()), Double.toString(product.getWeight()), Double.toString(product.getPrice())});

        final DataBaseCursorHolder cursor = DataBaseUtils.filterFromTable(connection, "Products", new String[]{"product_id"},
                new String[]{String.format("Όνομα Προϊόντος = '%s'", product.getName())});
        while (cursor.getResults().next()) {
            final int productId = cursor.getResults().getInt(1);
            DataBaseUtils.insertSpecificIntoTable(connection, "Απόθεμα", new String[]{"Ταυτότητα Προϊόντος", "Ποσότητα Προϊόντων"},
                    new String[]{Integer.toString(productId), Integer.toString(amount)});
        }
        cursor.closeCursor();
    }

    public void decreaseProductAmountInInventory(final Connection connection, final Product product, final int amount)
            throws SQLException, InventoryException {
        final DataBaseCursorHolder cursor = DataBaseUtils.innerJoinTables(connection, "Προϊόντα", "Απόθεμα", "Ταυτότητα Προϊόντος",
                new String[]{"Ταυτότητα Προϊόντος", "Ποσότητα Προϊόντων"}, new String[]{String.format("'Ονομα Προϊόντος = '%s'", product.getName())});

        while (cursor.getResults().next()) {
            final int productId = cursor.getResults().getInt(1);
            final int productAmount = cursor.getResults().getInt(2);
            final int newAmount = productAmount - amount;

            if (newAmount >= 0) {
                DataBaseUtils.updateTable(connection, "Απόθεμα", new String[]{"Ποσότητα Προϊόντων"}, new String[]{Integer.toString(newAmount)},
                        new String[]{String.format("Ταυτότητα Προϊόντος = %d", productId)});
            } else {
                throw new InventoryException("Δεν μπορείτε να αφαιρέσετε την συγκεκριμένη ποσότητα προϊόντων");
            }
        }
    }

    public void createUser(final Connection connection, final String UserName, final String UserPassword, final boolean privilege) {
        DataBaseUtils.insertSpecificIntoTable(connection, "Χρήστες", new String[]{"Όνομα Χρήστη", "Κωδικός Χρήστη", "Δικαιώματα"},
                new String[]{String.format("'%s'", UserName), String.format("'%s'", UserPassword), privilege ? "'t'" : "'f'"});
    }

    public void deleteUser(final Connection connection, final String UserName) {
        DataBaseUtils.deleteFromTable(connection, "Χρήστες", new String[]{String.format("Όνομα Χρήστη = '%s'", UserName)});
    }
}
