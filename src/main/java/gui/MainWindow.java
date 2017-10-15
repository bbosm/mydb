package gui;

import dbtype.AttributeEnum;
import serverSide.Server;

import db.*;
import dbtype.Attribute;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainWindow extends Application {

    private TabPane tabPane;
    private DataBase currentDB;
    private ArrayList<Column> currColumns = null;


    private final ObservableList<String> availableOptions =
            FXCollections.observableArrayList(
                    "Integer",
                    "Real",
                    "Char",
                    "Enum",
                    "Date",
                    "DateInvl"
            );

    @Override
    public void start(Stage stage) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        StackPane root = new StackPane();
        VBox verticalLayout = new VBox();

        Scene scene = new Scene(root, 600, 480);

        initUI(verticalLayout);

        root.getChildren().add(verticalLayout);

        stage.setTitle("Database Application");
        stage.setScene(scene);
        stage.show();

        showDataBase(null);
    }

    private void initUI(VBox verticalLayout) {
        MenuBar menuBar = new MenuBar();
        Menu menuTable = new Menu("Table");

        MenuItem newTableMenuItem = new MenuItem("Create Table");
        newTableMenuItem.setOnAction(t -> createTable());
        MenuItem addNewRowTableMenuItem = new MenuItem("Add New Row");
        addNewRowTableMenuItem.setOnAction(t -> addNewRow());
        MenuItem searchMenuItem = new MenuItem("Search");
        searchMenuItem.setOnAction(t -> search());

        menuTable.getItems().addAll(
                newTableMenuItem,
                addNewRowTableMenuItem,
                searchMenuItem
        );

        menuBar.getMenus().addAll(menuTable);
        verticalLayout.getChildren().add(menuBar);

        tabPane = new TabPane();
        verticalLayout.getChildren().add(tabPane);
    }

    private void updateDB() {
        try {
            currentDB = Server.getDB();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void removeTabsFromInterface() {
        tabPane.getTabs().clear();
    }

    private void addTableToInterface(Table table) {
        Tab tab = new Tab();
        tab.setText(table.getName());
        TableView tableView = new TableView();
        tab.setContent(tableView);
        tabPane.getTabs().add(tab);

        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        for (int j = 0; j < table.getColumns().size(); j++) {
            TableColumn col = new TableColumn(table.getColumns().get(j).getName());
            final int finalJ = j;
            col.setCellValueFactory(
                    (Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param ->
                            new SimpleStringProperty(param.getValue().get(finalJ).toString())
            );
            col.setCellFactory(TextFieldTableCell.forTableColumn());
//            TODO: edit Table cell
//            col.setOnEditCommit(
//                    (EventHandler<TableColumn.CellEditEvent<ObservableList, String>>) t -> {
//                        String newValue = t.getNewValue();
//                        try {
//                            final int i = t.getTablePosition().getRow();
//                            table.setField(i, finalJ, newValue);
//                        } catch (Exception e) {
//                            showErrorMessage(e.toString());
//                        }
//                    }
//            );
            tableView.getColumns().add(col);
        }

        for (int i = 0; i < table.getRows().size(); i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for(int j = 0; j < table.getRows().get(i).getValues().size(); j++) {
                row.add(table.getRows().get(i).get(j).toString());
            }
            data.add(row);
        }
//        tableView.setEditable(true);
        tableView.setItems(data);
    }

    private void showDataBase(String currentTableName) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        removeTabsFromInterface();
        updateDB();

        HashMap<String, Table> tables = currentDB.getTables();

        String currName = currentTableName;

        for (HashMap.Entry<String, Table> entry : tables.entrySet()) {
            addTableToInterface(entry.getValue());

            if (null == currName) {
                currName = entry.getKey();
            }
        }

        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals(currName)) {
                switchInterfaceToTable(currName);
            }
        }
    }

    private void switchInterfaceToTable(String tabName) {
        Tab tab = null;
        for (Tab tab1 : tabPane.getTabs()) {
            if (tab1.getText().equals(tabName)) {
                tab = tab1;
                break;
            }
        }
        tabPane.getSelectionModel().select(tab);
    }

    private void search() {
        String tableName = tabPane.getSelectionModel().getSelectedItem().getText();

        updateDB();

        Table currTable = currentDB.getTable(tableName);

        HBox columnLayout = new HBox();
        ArrayList<TextField> textFields = new ArrayList<>();

        for (int i = 0; i < currTable.getColumns().size(); ++i)
        {
            VBox tmpLayout = new VBox();
            String name = currTable.getColumns().get(i).getAttributeShortTypeName();
            textFields.add(new TextField());
            tmpLayout.getChildren().addAll(new Label(name), textFields.get(i));
            columnLayout.getChildren().add(tmpLayout);
        }

        Button tmpButton = new Button("Search");

        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(columnLayout, tmpButton);

        Stage tmpWindow = new Stage();
        tmpWindow.setTitle("Search Window");
        tmpWindow.setScene(new Scene(mainLayout));
        tmpWindow.show();

        tmpButton.setOnAction(e -> {
            ArrayList<String> fieldsSearch = new ArrayList<>(currTable.getColumns().size());
            for (int j = 0; j < currTable.getColumns().size(); j++) {
                fieldsSearch.add(textFields.get(j).getText());
            }

            Table searchTable = Server.search(tableName, fieldsSearch);
            tmpWindow.close();

            addTableToInterface(searchTable);
            switchInterfaceToTable(searchTable.getName());
        });
    }

    private boolean addColumnToTable(Object className, String columnName)
    {
        if (className == null) {
            showErrorMessage("Choose the type of the last column");
            return false;
        }
        if (columnName.equals("")) {
            showErrorMessage("Choose the name of the last column");
            return false;
        }
        if (className.toString().equals("Enum")) {

            VBox tmpLayout = new VBox();
            Button createEnumButton = new Button("Create Enum");
            TextField enumValues = new TextField();
            tmpLayout.getChildren().addAll(new Label("Enter space-separated values"), enumValues, createEnumButton);
            Stage tmpWindow = new Stage();
            tmpWindow.setTitle("Enum Values");
            tmpWindow.setScene(new Scene(tmpLayout));
            tmpWindow.show();

            createEnumButton.setOnAction(r -> {
                try {
                    currColumns.add(
                            ColumnFactory.createColumn(
                                    ColumnFactory.makeEnumColumnString(columnName, enumValues.getText())));
                    tmpWindow.close();
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        }
        else
        {
            try {
                currColumns.add(
                        ColumnFactory.createColumn(
                                ColumnFactory.makePlainColumnString(
                                        className.toString(), columnName)));
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private void createTable() {
        currColumns = new ArrayList<>();

        Label secondLabel = new Label("Name of table");
        TextField tableNameTextField = new TextField();

        HBox horizontalLayout = new HBox();
        horizontalLayout.getChildren().addAll(secondLabel, tableNameTextField);

        VBox _verticalLayout = new VBox();

        Button addNewColumnButton = new Button("Add new column");

        Button createNewTableButton = new Button("Create New Table");

        HBox buttonsLayout = new HBox();
        buttonsLayout.getChildren().addAll(addNewColumnButton, createNewTableButton);

        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(horizontalLayout, _verticalLayout, buttonsLayout);

        // New window (Stage)
        Scene secondScene = new Scene(mainLayout);
        Stage newWindow = new Stage();
        newWindow.setTitle("New Table");
        newWindow.setScene(secondScene);

        HBox columnCreationLayout = new HBox();
        ArrayList<ComboBox> comboBoxes = new ArrayList<>();
        ComboBox comboBox = new ComboBox(availableOptions);
        comboBoxes.add(comboBox);
        ArrayList<TextField> textFields = new ArrayList<>();
        TextField columnNameTextField = new TextField();
        textFields.add(columnNameTextField);
        columnCreationLayout.getChildren().addAll(new Label("Column"), comboBox, columnNameTextField);
        _verticalLayout.getChildren().add(columnCreationLayout);


        addNewColumnButton.setOnAction(e -> {
            int curr = comboBoxes.size() - 1;
            boolean isAdded = addColumnToTable(comboBoxes.get(curr).getValue(), textFields.get(curr).getText());

            if (isAdded) {
                HBox _columnCreationLayout = new HBox();
                ComboBox _comboBox = new ComboBox(availableOptions);
                comboBoxes.add(_comboBox);
                TextField _columnNameTextField = new TextField();
                textFields.add(_columnNameTextField);
                _columnCreationLayout.getChildren().addAll(new Label("Column"), _comboBox, _columnNameTextField);
                _verticalLayout.getChildren().add(_columnCreationLayout);
            }
        });

        createNewTableButton.setOnAction(e -> {
            int curr = comboBoxes.size() - 1;
            boolean isAdded = addColumnToTable(comboBoxes.get(curr).getValue(), textFields.get(curr).getText());

            if (isAdded) {
                if (!tableNameTextField.getText().equals("")) {
                    String tableName = tableNameTextField.getText();
                    Server.createTable(tableName, currColumns);
                    newWindow.close();
                    try {
                        showDataBase(tableName);
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    showErrorMessage("Empty table name");
                }
            }
        });

       newWindow.show();
    }




    private void addNewRow() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        String tableName = tab.getText();
        Table table = currentDB.getTable(tableName);

        VBox mainLayout = new VBox();
        ArrayList<TextField> textFields = new ArrayList<>();
        for (int i = 0; i < table.getColumns().size(); ++i) {
            HBox horizontalBoxLayout = new HBox();
            Column col = table.getColumns().get(i);
            Label label = new Label(col.getName());
            TextField valueTextField = new TextField();
            textFields.add(valueTextField);
            horizontalBoxLayout.getChildren().addAll(label, valueTextField);
            mainLayout.getChildren().add(horizontalBoxLayout);
        }

        Button addButton = new Button("Add new row");
        mainLayout.getChildren().add(addButton);

        Scene secondScene = new Scene(mainLayout);

        Stage newWindow = new Stage();
        newWindow.setTitle("About");
        newWindow.setScene(secondScene);

        addButton.setOnAction(e -> {
            boolean allValuesFilled = true;
            ArrayList<Attribute> attributes = new ArrayList<>(textFields.size());
            for (int i = 0; i < textFields.size(); ++i) {
                TextField textField = textFields.get(i);
                if (textField.getText().equals("")) {
                    allValuesFilled = false;
                } else {
                    Column col = table.getColumns().get(i);
                    try {
                        if (col.getAttributeShortTypeName().equals("Enum")) {
                            attributes.add(new AttributeEnum(textField.getText(), col));
                        } else {
                            attributes.add((Attribute) col.getStringConstructor().newInstance(textField.getText()));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println(attributes.size());
                        System.out.println(col.getAttributeShortTypeName());
//                        showErrorMessage(ex.toString());
                    }
                }
            }
            if (!allValuesFilled) {
                showErrorMessage("Not all values filled");
            }
            try {
                Server.addNewRow(tableName, attributes);
                updateDB();
                switchInterfaceToTable(tableName);
            } catch (Exception ex) {
                showErrorMessage(ex.toString());
            }
            newWindow.close();
            try {
                showDataBase(null);
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        newWindow.show();
    }



    private static void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);

        alert.showAndWait();
    }





    public static void main(String[] args) {
        launch(args);
    }
}
