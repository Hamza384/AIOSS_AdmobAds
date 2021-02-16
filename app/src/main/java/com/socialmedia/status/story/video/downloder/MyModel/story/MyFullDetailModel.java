package com.socialmedia.status.story.video.downloder.MyModel.story;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MyFullDetailModel implements Serializable {

    @SerializedName("user_detail")
    private MyUserDetailModel user_detail;

    @SerializedName("reel_feed")
    private MyReelFeedModel reel_feed;

    public MyUserDetailModel getUser_detail() {
        return user_detail;
    }

    public void setUser_detail(MyUserDetailModel user_detail) {
        this.user_detail = user_detail;
    }

    public MyReelFeedModel getReel_feed() {
        return reel_feed;
    }

    public void setReel_feed(MyReelFeedModel reel_feed) {
        this.reel_feed = reel_feed;
    }
}
