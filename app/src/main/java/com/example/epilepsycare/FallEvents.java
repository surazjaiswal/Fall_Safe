package com.example.epilepsycare;

import java.util.Date;

public class FallEvents {

    String fall_location,fall_date_time;

    public FallEvents(String fall_location, String fall_date_time) {
        this.fall_location = fall_location;
        this.fall_date_time = fall_date_time;
    }

    public String getFall_location() {
        return fall_location;
    }

    public void setFall_location(String fall_location) {
        this.fall_location = fall_location;
    }

    public String getFall_date_time() {
        return fall_date_time;
    }

    public void setFall_date_time(String fall_date_time) {
        this.fall_date_time = fall_date_time;
    }

}
