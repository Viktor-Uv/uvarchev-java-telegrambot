package com.uvarchev.javatelebot.network;

import com.uvarchev.javatelebot.dto.News;
import com.uvarchev.javatelebot.dto.NewsResults;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Stack;

public class ApiClient {

    // Spaceflight News API
    private final String baseUrl = "https://api.spaceflightnewsapi.net";
    private String newsProviders;
    private String oldestRead;

    public ApiClient(String newsProviders, String oldestRead) {
        this.newsProviders = newsProviders;
        this.oldestRead = oldestRead;
    }

    public Stack<News> getNews() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpaceNewsService service = retrofit.create(SpaceNewsService.class);
        Call<NewsResults> newsCall = service.getNews(
                50,
                newsProviders,
                oldestRead
        );

        try {
            Response<NewsResults> newsResponse = newsCall.execute();
            if (newsResponse.body() != null) {
                return newsResponse.body().getResults();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
