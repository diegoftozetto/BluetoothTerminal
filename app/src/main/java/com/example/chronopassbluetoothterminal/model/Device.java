package com.example.chronopassbluetoothterminal.model;

public class Device {

    private String name;
    private String address;
    private String color;

    public Device() {
    }

    public Device(String name, String address, String color) {
        this.name = name;
        this.address = address;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
