package CarnivAPP.GUI;

import CarnivAPP.Basket;
import CarnivAPP.Inventory;
import CarnivAPP.Order;
import CarnivAPP.DataBase.DataBaseCursorHolder;
import CarnivAPP.DataBase.DataBaseUtils;
import CarnivAPP.Exceptions.BasketException;
import CarnivAPP.Exceptions.InventoryException;
import CarnivAPP.Products.Product;
import CarnivAPP.Users.Administrator;
import CarnivAPP.Users.Client;
import CarnivAPP.Users.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class GuiDbUtils
{

    public static void loadDataToInventory(final Connection connection, final Inventory inventory, final User user)
    {
        DataBaseCursorHolder cursor;
        try {
            if (user instanceof Client)
            {
                cursor = DataBaseUtils.innerJoinTables(connection, "Προϊόντα", "Απόθεμα", "Κωδικός Προϊόντος",
                        new String[]{"Όνομα Προϊόντος", "Βάρος Προϊόντος", "Τιμή Προϊόντος", "Ποσότητα Προϊόντος"}, new String[]{"Ποσότητα Προϊόντος > 0"});
            }
            else
            {
                cursor = DataBaseUtils.innerJoinTables(connection, "Προϊόντα", "Απόθεμα", "Κωδικός Προϊόντος",
                        new String[]{"Όνομα Προϊόντος", "Βάρος Προϊόντος", "Τιμή Προϊόντος", "Ποσότητα Προϊόντος"}, new String[]{});
            }

            while (cursor.getResults().next())
            {
                inventory.addProducts(new Product(cursor.getResults().getString(1), cursor.getResults().getDouble(2), cursor.getResults().getDouble(3)), cursor.getResults().getInt(4));
            }
            cursor.closeCursor();
        }
        catch (SQLException | InventoryException e)
        {
            e.printStackTrace();
        }
    }

    public static void loadDataToUserList(final Connection connection, final List<User> userList)
    {
        DataBaseCursorHolder cursor;
        try
        {
            cursor = DataBaseUtils.filterFromTable(connection, "Χρήστες", new String[]{"Όνομα Χρήστη", "Κωδικός Χρήστη", "Δικαιώματα"},
                    new String[]{});
            while (cursor.getResults().next())
            {
                if (cursor.getResults().getBoolean(3)) userList.add(new Administrator(cursor.getResults().getString(1),
                        cursor.getResults().getString(2)));
                else userList.add(new Client(cursor.getResults().getString(1), cursor.getResults().getString(2)));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void loadDataToOrders(final User user, final Connection connection, final List<Order> orders)
    {
        DataBaseCursorHolder cursor;
        try
        {
            if (user instanceof Administrator) cursor = DataBaseUtils.innerJoinTables(connection, "Καλάθια", "Παραγγελίες", "Κωδικός Καλαθιού", new String[]{"Όνομα Προϊόντος", "Ποσότητα Προϊόντος", "Διεύθυνση"}, new String[]{});
            else cursor = DataBaseUtils.innerJoinTables(connection, "Καλάθια", "Παραγγελίες", "Κωδικός Καλαθιού", new String[]{"Όνομα Προϊόντος", "Ποσότητα Προϊόντος", "Διεύθυνση"}, new String[]{String.format("Ιδιοκτήτης Καλαθιού='%s'", user.getUserName())});
            while (cursor.getResults().next())
            {
                final Basket orderBasket = new Basket();
                constructBasketFromDB(connection, cursor.getResults(), orderBasket);
                orders.add(new Order(orderBasket, cursor.getResults().getString(3)));
            }
            cursor.closeCursor();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void loadSavedBasket(final Client client, final Connection connection, final Basket basket)
    {
        DataBaseCursorHolder cursor;
        try
        {
            cursor = DataBaseUtils.filterFromTable(connection, "Καλάθια", new String[]{"Όνομα Προϊόντος", "Ποσότητα Προϊόντος", "Κωδικός Καλαιού"},
                    new String[]{String.format("Ιδιοκτήτης Καλαθιού='%s'", client.getUserName()), "Και", "Επεξεργάστηκε='f'"});
            while (cursor.getResults().next())
            {
                client.setRetrievedBasketId(cursor.getResults().getInt(3));
                constructBasketFromDB(connection, cursor.getResults(), basket);
            }
            cursor.closeCursor();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static void constructBasketFromDB(final Connection connection, final ResultSet Products, final Basket basketToConstruct)
    {
        try
        {
            final List<String> Names = Arrays.asList(Products.getString(1).split(","));
            final List<String> Amounts = Arrays.asList(Products.getString(2).split(","));

            Product restoredProduct;
            int counter = 0;

            for (final String productName : Names)
            {
                final DataBaseCursorHolder productDetails = DataBaseUtils.filterFromTable(connection, "Προϊόντα", new String[]{"Βάρος Προϊόντος", "Τιμή Προϊόντος"}, new String[]{String.format("Όνομα Προϊόντος='%s'", productName)});
                while (productDetails.getResults().next())
                {
                    restoredProduct = new Product(productName, productDetails.getResults().getDouble(1), productDetails.getResults().getDouble(2));
                    basketToConstruct.addProducts(restoredProduct, Integer.parseInt(Amounts.get(counter)));
                }
                productDetails.closeCursor();
                counter++;
            }
        }
        catch (SQLException | BasketException e)
        {
            e.printStackTrace();
        }
    }

}
