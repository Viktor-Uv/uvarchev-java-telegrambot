package com.uvarchev.javatelebot.service;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class News {
    @SerializedName("title")
    private String title;

    @SerializedName("url")
    private String url;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("summary")
    private String summary;

    @SerializedName("published_at")
    private String publishedAt;

    @Override
    public String toString() {
        //      Instant view url:
        return "https://t.me/iv?url=" + url + "&rhash=133d3fc26c9f3f" + "\n" +
                title + "\n\n" +
                summary + "\n\n" +
                "Published: " + publishedAt;
    }
}
