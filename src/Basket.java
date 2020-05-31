package CarnivAPP;

import CarnivAPP.Exceptions.BasketException;
import CarnivAPP.Interfaces.ProductStorage;
import CarnivAPP.Interfaces.StringFormatter;
import CarnivAPP.Products.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static CarnivAPP.Extras.Utils.iterateSimultaneously;

public class Basket implements ProductStorage {
    private HashMap<Product, Integer> Products;
    private StringFormatter stringFormatter;

    public Basket() {
        this.Products = new HashMap<>();
        this.stringFormatter = () -> {
            final int basketSize = this.Products.size();
            final String itemString = basketSize > 1 ? basketSize + " Προϊόντα." : basketSize + " Προϊόν.";
            return String.format("Το καλάθι έχει %s", itemString);
        };
    }

    public void addProducts(final Product product, final String Size, final int Amount) throws BasketException {
        if (product != null) {
            final int currentAmount = this.Products.getOrDefault(product, 0);
            this.Products.put(product, currentAmount + Amount);
        } else {
            throw new BasketException("Δεν τοποθετήθηκαν προϊόντα στο καλάθι");
        }
    } 

    public void removeProducts(final Product product, final String Size, final int Amount) throws BasketException {
        if (this.Products.get(product) > Amount) {
            this.Products.replace(product, this.Products.get(product) - Amount);
        } else if (this.Products.get(product) == Amount) {
            this.Products.replace(product, 0);
        } else {
            throw new BasketException(String.format("Δεν μπορούν να αφαιρεθούν %d παρουσίες του προϊόντος, διότι υπάρχουν μόνο %d παρουσίες!",
                    Amount, this.Products.get(product)));
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

    public ArrayList<String> toDBFormat() {
       
        final ArrayList<String> result = new ArrayList<>();
        final String Names = this.Products.entrySet().
                parallelStream().
                map(p -> p.getKey().getName()).
                collect(Collectors.joining(","));
        final String Sizes = this.Products.entrySet().
                parallelStream().
                map(p -> p.getKey().getSize()).
                collect(Collectors.joining(","));
        final String Amounts = this.Products.entrySet().
                parallelStream().
                map(p -> p.getValue().toString()).
                collect(Collectors.joining(","));
        result.add(Names);
        result.add(Sizes);
        result.add(Amounts);

        return result;
    }

    @Deprecated
    public void restoreFromDB(final String productsName, /*final String productsSize,*/ final String productsAmount) {
        final List<String> Names = Arrays.asList(productsName.split(","));
        //final List<String> Sizes = Arrays.asList(productsSize.split(","));
        final List<String> Amounts = Arrays.asList(productsAmount.split(","));

        iterateSimultaneously(Names, Amounts, (String Name, String Amount) -> {
            try {
                addProducts(new Product(Name, 0.150, 0.8), Integer.parseInt(Amount));
            } catch (BasketException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public String toString() {
        return stringFormatter.formatToString();
    }		
	}
}