package CarnivAPP;

import CarnivAPP.Exceptions.InventoryException;
import CarnivAPP.Interfaces.ProductStorage;
import CarnivAPP.Interfaces.StringFormatter;
import CarnivAPP.Products.Product;

import java.text.DecimalFormat;
import java.util.HashMap;

public class Inventory implements ProductStorage {
    private HashMap<Product, Integer> Products;
    private StringFormatter stringFormatter;

    public Inventory() {
        this.Products = new HashMap<>();
        this.stringFormatter = () -> {
            final int productsSize = this.Products.size();
            final String itemString = productsSize > 1 ? productsSize + " Προϊόντα" : productsSize + " Προϊόν";
            final DecimalFormat total = new DecimalFormat("####0.0");
            return String.format(" %s, συνολική τιμή του αποθέματος: %s", itemString, total.format(calculateTotal()));
        };
    }

    public void addProducts(final Product product, final int amount) throws InventoryException {
        if (product != null) {
            final int currentAmount = this.Products.getOrDefault(product, 0);
            this.Products.put(product, currentAmount + amount);
        } else {
            throw new InventoryException("Δεν μπορείς να προσθέσεις Null προϊόντα στο Inventory!");
        }
    }

    public void removeProducts(final Product product, final int amount) throws InventoryException {
        if (this.Products.get(product) > amount) {
            this.Products.replace(product, this.Products.get(product) - amount);
        } else if (this.Products.get(product) == amount) {
            this.Products.replace(product, 0);
        } else {
            throw new InventoryException(String.format("Δεν γίνεται διαγραφή %d κομματιών" +
                    " αφού υπάρχουν %d κομμάτια!", amount, this.Products.get(product)));
        }
    }

    public HashMap<Product, Integer> getProducts() {
        return Products;
    }

    public double calculateTotal() {
        return this.Products.entrySet().
                parallelStream().
                mapToDouble(product -> product.getKey().getPrice() * product.getValue()).
                sum();
    }

    public void setStringFormatter(final StringFormatter stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    @Override
    public String toString() {
        return this.stringFormatter.formatToString();
    }
}