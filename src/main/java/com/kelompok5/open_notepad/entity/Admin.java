package com.kelompok5.open_notepad.entity;

public class Admin extends Account {
    public Admin(String username, String hashedPassword, String salt, String email, String firstName, String lastName) {
        super(username, hashedPassword, salt, email, firstName, lastName);
    }
}
