package com.shuishou.digitalmenu.bean;

import java.util.Date;

/**
 * Created by Administrator on 22/05/2018.
 */

public class UserData {
    private int id;

    private String username;

    private Date startTime;//on duty time

    public UserData(){}

    public UserData(int id, String username){
        this.id = id;
        this.username = username;
    }

    public UserData(int id, String username, Date startTime){
        this.id = id;
        this.username = username;
        this.startTime = startTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String name) {
        this.username = name;
    }

    @Override
    public String toString() {
        return "User [username=" + username + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserData other = (UserData) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }
}
