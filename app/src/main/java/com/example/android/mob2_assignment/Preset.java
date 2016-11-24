package com.example.android.mob2_assignment;

import java.util.Date;

public class Preset {
    private String name;
    private int noOfCups;
    private String timestamp;

    public Preset(String name, int noOfCups, String timestamp){
        this.name = name;
        this.noOfCups = noOfCups;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "NAME: " + name + "\nNO. OF CUPS: " + noOfCups + "\nTIME: " + timestamp;
    }
}
