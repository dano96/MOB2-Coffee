package com.example.android.mob2_assignment;

import java.util.Date;

/**
 * Created by Lea on 17-Nov-16.
 */

public class Preset {
    String name;
    int noOfCups;
    java.util.Date timestamp;

    public Preset(String name, int noOfCups, Date timestamp){
        this.name = name;
        this.noOfCups = noOfCups;
        this.timestamp = timestamp;
    }

}
