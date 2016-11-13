package com.example.alex.planningmadeeasy.utils;

/**
 * Created by noahblumenfeld on 11/12/16.
 */

public class Goal {
    public long goalKey;
    public String message;
    public long parentKey;
    public User user;

    public Goal(long goalKey, String message, long parentKey, User user){
        this.goalKey = goalKey;
        this.message = message;
        this.parentKey = parentKey;
        this.user = user;

    }


}
