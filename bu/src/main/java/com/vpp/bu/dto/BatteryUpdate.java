package com.vpp.bu.dto;

import java.io.Serializable;

public class BatteryUpdate implements Serializable {
    private String name;
    private int capacity;

    public BatteryUpdate(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}