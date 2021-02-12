package com.example.chronopassbluetoothterminal.model;

public class Command {
    public static final String TABLE_NAME = "tb_command";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_MATRIX_LED = "matrix_led";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private long id;
    private String name;
    private String value;
    private String matrixLed;
    private int color;
    private String timestamp;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_VALUE + " TEXT,"
                    + COLUMN_MATRIX_LED + " TEXT,"
                    + COLUMN_COLOR + " INTEGER,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    public Command() {
    }

    public Command(long id, String name, String value, String matrixLed, int color, String timestamp) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.matrixLed = matrixLed;
        this.color = color;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMatrixLed() {
        return matrixLed;
    }

    public void setMatrixLed(String matrixLed) {
        this.matrixLed = matrixLed;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
