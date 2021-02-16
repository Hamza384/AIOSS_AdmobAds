package com.socialmedia.status.story.video.downloder.MyModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class MyTwitterResponse implements Serializable {

    @SerializedName("videos")
    private ArrayList<MyTwitterResponseModel> videos;

    public ArrayList<MyTwitterResponseModel> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<MyTwitterResponseModel> videos) {
        this.videos = videos;
    }
}
