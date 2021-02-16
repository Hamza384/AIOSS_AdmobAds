package com.socialmedia.status.story.video.downloder.MyModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Graphql implements Serializable {

    @SerializedName("shortcode_media")
    private MyShortcodeMedia shortcode_media;

    public MyShortcodeMedia getShortcode_media() {
        return shortcode_media;
    }

    public void setShortcode_media(MyShortcodeMedia shortcode_media) {
        this.shortcode_media = shortcode_media;
    }
}
