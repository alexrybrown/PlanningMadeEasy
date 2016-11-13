package com.example.alex.planningmadeeasy.utils;

/**
 * Created by noahblumenfeld on 11/12/16.
 */

public class User {
    public long userID;
    public String F_Name;
    public String L_Name;
    public String Username;
    public String Email;
    public String Password;

    public User(long userID, String F_Name, String L_Name, String Email, String Username, String Password) {
        this.userID = userID;
        this.F_Name = F_Name;
        this.L_Name = L_Name;
        this.Username = Username;
        this.Email = Email;
        this.Password = Password;
    }
}
