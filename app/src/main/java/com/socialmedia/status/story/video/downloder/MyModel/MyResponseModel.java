package com.socialmedia.status.story.video.downloder.MyModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MyResponseModel implements Serializable {

    @SerializedName("graphql")
    private Graphql graphql;

    public Graphql getGraphql() {
        return graphql;
    }

    public void setGraphql(Graphql graphql) {
        this.graphql = graphql;
    }
}
