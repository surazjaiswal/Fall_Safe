package com.example.epilepsycare;

import java.util.Date;

public class FallEvents {

    String fall_location,fall_date_time,fall_note;
    boolean fall;
    int img_src;

    public FallEvents(String fall_location, String fall_date_time,boolean fall,String fall_note) {
        this.fall_location = fall_location;
        this.fall_date_time = fall_date_time;
//        this.img_src = img_src;
        this.fall = fall;
        this.fall_note = fall_note;
    }

    public boolean isFall() {
        return fall;
    }

    public void setFall(boolean fall) {
        this.fall = fall;
    }

    public int getImg_src() {
        return img_src;
    }

    public void setImg_src(int img_src) {
        this.img_src = img_src;
    }

    public String getFall_note() {
        return fall_note;
    }

    public void setFall_note(String fall_note) {
        this.fall_note = fall_note;
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
