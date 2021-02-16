package com.socialmedia.status.story.video.downloder.MyModel.story;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ImageVersionModel implements Serializable {

    @SerializedName("candidates")
    private ArrayList<MyCandidatesModel> candidates;

    public ArrayList<MyCandidatesModel> getCandidates() {
        return candidates;
    }

    public void setCandidates(ArrayList<MyCandidatesModel> candidates) {
        this.candidates = candidates;
    }
}
