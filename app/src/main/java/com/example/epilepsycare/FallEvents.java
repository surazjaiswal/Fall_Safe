package com.example.epilepsycare;

public class FallEvents {

    String fall_locationLink, fall_locationAddress, fall_date_time, fall_note;
    boolean fall;

    public FallEvents(String fall_location, String fall_locationAddress, String fall_date_time, boolean fall, String fall_note) {
        this.fall_locationLink = fall_location;
        this.fall_date_time = fall_date_time;
        this.fall = fall;
        this.fall_note = fall_note;
        this.fall_locationAddress = fall_locationAddress;
    }

    public boolean isFall() {
        return fall;
    }

    public String getFall_locationAddress() {
        return fall_locationAddress;
    }

    public void setFall_locationAddress(String fall_locationAddress) {
        this.fall_locationAddress = fall_locationAddress;
    }

    public void setFall(boolean fall) {
        this.fall = fall;
    }

    public String getFall_note() {
        return fall_note;
    }

    public void setFall_note(String fall_note) {
        this.fall_note = fall_note;
    }

    public String getFall_locationLink() {
        return fall_locationLink;
    }

    public void setFall_locationLink(String fall_locationLink) {
        this.fall_locationLink = fall_locationLink;
    }

    public String getFall_date_time() {
        return fall_date_time;
    }

    public void setFall_date_time(String fall_date_time) {
        this.fall_date_time = fall_date_time;
    }

}
