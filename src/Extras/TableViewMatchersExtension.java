package CarnivAPP.Extras;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.GeneralMatchers;
import org.testfx.service.finder.NodeFinder;
import org.testfx.service.query.NodeQuery;

import java.util.*;
import java.util.stream.Collectors;


public class TableViewMatchersExtension {
    public static final int REPLACEMENT_VALUE = -1; //Αντικαθιστά το NULL στο getRowValues.

    private TableViewMatchersExtension() {
    }

    @Factory
    public static Matcher<TableView> hasTableCell(final Object value) {
        String descriptionText = "Έχει κελί πίνακα \"" + value + "\"";
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText,
                (tableView) -> toText(tableView) + "\nτο οποίο δεν περιέχει κάποιο κελί με την συγκεκριμένη τιμή",
                (node) -> hasTableCell(node, value));
    }

    private static boolean hasTableCell(final TableView tableView, final Object value) {
        NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
        NodeQuery nodeQuery = nodeFinder.from(new Node[]{tableView});
        return nodeQuery.lookup(".table-cell").match((cell) -> hasCellValue((Cell) cell, value)).tryQuery().isPresent();
    }

    @Factory
    public static Matcher<TableView> hasNoTableCell(final Object value) {
        final String descriptionText = "Δεν έχει κελί πίνακα \"" + value + "\"";
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText,
                (tableView) -> toText(tableView) + "\nτο οποίο περιέχει κάποιο κελί με την συγκεκριμένη τιμή",
                (node) -> hasNoTableCell(node, value));
    }

    private static boolean hasNoTableCell(final TableView tableView, final Object value) {
        final NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
        final NodeQuery nodeQuery = nodeFinder.from(new Node[]{tableView});
        return nodeQuery.lookup(".table-cell").match((cell) -> !hasCellValue((Cell) cell, value)).tryQuery().isPresent();
    }

    @Factory
    public static Matcher<TableView> containsRow(final Object... row) {
        final String descriptionText = "Έχει γραμμή: " + Arrays.toString(row);
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText, TableViewMatchersExtension::toText, (node) -> containsRow(node, row));
    }

    private static <T> boolean containsRow(final TableView<T> tableView, final Object... row) {
        if (tableView.getItems().isEmpty()) {
            return false;
        }

        final Map<Integer, List<ObservableValue<?>>> rowValuesMap = new HashMap<>(tableView.getColumns().size());

        List rowValues;
        for (int j = 0; j < tableView.getItems().size(); ++j) {
            rowValues = getRowValues(tableView, j);
            rowValuesMap.put(j, rowValues);
        }

        final List<List<Object>> testList = new ArrayList<>();
        for (final Map.Entry<Integer, List<ObservableValue<?>>> value : rowValuesMap.entrySet()) {
            final List<Object> entry = new ArrayList<>();
            for (final ObservableValue<?> actualValue : value.getValue()) {
                entry.add(actualValue.getValue());
            }
            testList.add(entry);
        }
        final List<Object> entryRow = Arrays.asList(row);
        return testList.contains(entryRow);
    }

    @Factory
    public static Matcher<TableView> hasColumnWithID(final String columnId) {
        final String descriptionText = "Τίτλος Στήλης Με Κωδικό(id): " + columnId;
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText, (node) -> hasColumnWithID(node, columnId));
    }

    private static <T> boolean hasColumnWithID(final TableView<T> tableView, final String columnId) {
        for (final TableColumn<?, ?> column : tableView.getColumns()) {
            if (column.getId().equals(columnId)) return true;
        }
        return false;
    }

    @Factory
    public static Matcher<TableView> hasNoColumnWithID(final String columnId) {
        final String descriptionText = "Τίτλος Στήλης: " + columnId;
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText, (node) -> hasNoColumnWithID(node, columnId));
    }

    private static <T> boolean hasNoColumnWithID(final TableView<T> tableView, final String columnId) {
        for (final TableColumn<?, ?> column : tableView.getColumns()) {
            if (column.getId().equals(columnId)) return false;
        }
        return true;
    }

    private static List<ObservableValue<?>> getRowValues(final TableView<?> tableView, final int rowIndex) {
        final Object rowObject = tableView.getItems().get(rowIndex);
        final List<ObservableValue<?>> rowValues = new ArrayList(tableView.getColumns().size());

        for (int i = 0; i < tableView.getColumns().size(); ++i) {
            final TableColumn<?, ?> column = tableView.getColumns().get(i);
            final CellDataFeatures cellDataFeatures = new CellDataFeatures(tableView, column, rowObject);
            try {
                rowValues.add(i, column.getCellValueFactory().call(cellDataFeatures));
            } catch (NullPointerException ex) {
                final ObservableValue<Integer> replacement = new SimpleIntegerProperty(REPLACEMENT_VALUE).asObject();
                rowValues.add(i, replacement);
            }
        }

        return rowValues;
    }

    private static boolean hasCellValue(final Cell cell, final Object value) {
        return !cell.isEmpty() && hasItemValue(cell.getText(), value);
    }

    private static boolean hasItemValue(final Object item, final Object value) {
        if (item == null && value == null) {
            return true;
        } else if (item != null && value != null) {
            return Objects.equals(item, value) || Objects.equals(item.toString(), value) || value.toString() != null && Objects.equals(item.toString(), value.toString());
        } else {
            return false;
        }
    }


    private static String toText(final TableView<?> tableView) {
        final StringJoiner joiner = new StringJoiner(", ", "[", "]");

        for (int rowIndex = 0; rowIndex < tableView.getItems().size(); ++rowIndex) {
            joiner.add(toText(tableView, rowIndex));
        }

        return joiner.toString();
    }

    private static String toText(final TableView<?> tableView, final int rowIndex) {
        return '[' + getRowValues(tableView, rowIndex).stream().map((observableValue) ->
                observableValue.getValue() == null ? "null" : observableValue.getValue().toString()).collect(Collectors.joining(", ")) + ']';
    }
}
