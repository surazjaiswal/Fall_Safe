package com.fallsafe.epilepsycare;

public class ContactList {

    String name,message;
    int number;

    public ContactList(String name, String message, int number) {
        this.name = name;
        this.message = message;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
