package com.uvarchev.javatelebot.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpaceNewsService {
    @GET("/v4/articles/")
    Call<NewsResults> getNews(
            @Query("limit") int limit,
            @Query("news_site") String newsSite,
            @Query("published_at_gt") String publishedAtGt,
            @Query("ordering") String ordering
    );
}
