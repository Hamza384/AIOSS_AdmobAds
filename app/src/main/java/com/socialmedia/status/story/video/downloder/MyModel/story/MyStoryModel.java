package com.socialmedia.status.story.video.downloder.MyModel.story;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class MyStoryModel implements Serializable {

    @SerializedName("tray")
    private ArrayList<MyTrayModel> tray;

    @SerializedName("status")
    private String status;

    public ArrayList<MyTrayModel> getTray() {
        return tray;
    }

    public void setTray(ArrayList<MyTrayModel> tray) {
        this.tray = tray;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
