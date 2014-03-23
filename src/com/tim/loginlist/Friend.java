package com.tim.loginlist;

/**
 * Data structure to hold friend info,
 * acting as list elements for friend list.
 * Created by Tim Sandberg on 3/21/14.
 */
public class Friend {
    private String mName;
    private String mId;
    private String mLocation;

    Friend(String name, String id) {
        this.mName = name;
        this.mId = id;

    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }
}
