package com.example.epilepsycare;

import java.util.Date;

public class FallEvents {

    String fall_location,fall_date,fall_time;

    public FallEvents(String fall_location, String fall_date, String fall_time) {
        this.fall_location = fall_location;
        this.fall_date = fall_date;
        this.fall_time = fall_time;
    }

    public String getFall_location() {
        return fall_location;
    }

    public void setFall_location(String fall_location) {
        this.fall_location = fall_location;
    }

    public String getFall_date() {
        return fall_date;
    }

    public void setFall_date(String fall_date) {
        this.fall_date = fall_date;
    }

    public String getFall_time() {
        return fall_time;
    }

    public void setFall_time(String fall_time) {
        this.fall_time = fall_time;
    }
}
