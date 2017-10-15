package serverSide;

import db.Column;
import db.DataBase;
import db.Row;
import db.Table;
import dbtype.Attribute;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Server {
    // ends with File.separator or empty ("") String
    private static final String dbFolderPath = "";
    private static final String dbFileName = "bd.db";

    private static DataBase dataBase = null;

    public static DataBase getDB() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (null == dataBase)
        {
            dataBase = new DataBase(dbFolderPath + dbFileName);
        }
        return dataBase;
    }

    private static void saveDb() {
        try {
            dataBase.saveToFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void createTable(String tableName, ArrayList<Column> currColumns) {
        String tableFilePath = dbFolderPath + tableName + ".tb";
        Table newTable = new Table(tableFilePath, tableName, currColumns);
        dataBase.getTables().put(tableName, newTable);
        saveDb();
    }

    public static Table search(String tableName, ArrayList<String> fieldsSearch) {
        return dataBase.getTable(tableName).search(fieldsSearch);
    }

    public static void addNewRow(String tableName, ArrayList<Attribute> attributes) {
        dataBase.getTable(tableName).getRows().add(new Row(attributes));
        saveDb();
    }

}
