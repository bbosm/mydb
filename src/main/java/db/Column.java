package db;

import java.lang.reflect.Constructor;

public class Column {
    String name;
    String className;
    private Class attributeType;
    private Constructor stringConstructor;

    public Column(String name, String className) throws ClassNotFoundException, NoSuchMethodException {
        this.name = name;
        this.className = className;

        this.attributeType = Class.forName(className);
        this.stringConstructor = attributeType.getConstructor(String.class);
    }

    public Column(String s) throws NoSuchMethodException, ClassNotFoundException {
        String[] columnsFields = s.split("\\t");
        this.name = columnsFields[1];
        this.className = columnsFields[2];

        this.attributeType = Class.forName(className);
        this.stringConstructor = attributeType.getConstructor(String.class);
    }

    @Override
    public String toString() {
        return "Column" + '\t' + name + '\t' + className;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className.substring(className.lastIndexOf(".Attribute") + 10);
    }

    public Constructor getStringConstructor() {
        return stringConstructor;
    }
}