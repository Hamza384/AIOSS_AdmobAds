package com.socialmedia.status.story.video.downloder.MyApi;

import com.socialmedia.status.story.video.downloder.MyModel.MyTiktokModel;
import com.socialmedia.status.story.video.downloder.MyModel.MyTwitterResponse;
import com.socialmedia.status.story.video.downloder.MyModel.story.MyFullDetailModel;
import com.socialmedia.status.story.video.downloder.MyModel.story.MyStoryModel;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface APIServices {
    @GET
    Observable<JsonObject> callResult(@Url String Value, @Header("Cookie") String cookie, @Header("User-Agent") String userAgent);

    @FormUrlEncoded
    @POST
    Observable<MyTwitterResponse> callTwitter(@Url String Url, @Field("id") String id);

    @GET
    Observable<MyTiktokModel> getTiktokData(@Url String Url, @Query("url") String url);

    @GET
    Observable<MyStoryModel> getStoriesApi(@Url String Value, @Header("Cookie") String cookie, @Header("User-Agent") String userAgent);

    @GET
    Observable<MyFullDetailModel> getFullDetailInfoApi(@Url String Value, @Header("Cookie") String cookie, @Header("User-Agent") String userAgent);

    @FormUrlEncoded
    @POST
    Observable<JsonObject> callSnackVideo(@Url String Url, @Field("shortKey") String shortKey, @Field("os") String os, @Field("sig") String sig, @Field("client_key") String client_key);
}