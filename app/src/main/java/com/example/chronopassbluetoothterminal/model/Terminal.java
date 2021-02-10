package com.example.chronopassbluetoothterminal.model;

public class Terminal {
    public static final String TABLE_NAME = "tb_terminal";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DEVICE_ADDRESS = "device_address";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_TYPE = "type";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_DEVICE_ADDRESS + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME,"
                    + COLUMN_TEXT + " TEXT,"
                    + COLUMN_TYPE + " INTEGER"
                    + ")";

    private long id;
    private String deviceAddress;
    private String timestamp;
    private String text;
    private int type; //0-System | 1-Send | 2-Receive

    public Terminal() {
    }

    public Terminal(long id, String deviceAddress, String timestamp, String text, int type) {
        this.id = id;
        this.deviceAddress = deviceAddress;
        this.timestamp = timestamp;
        this.text = text;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
