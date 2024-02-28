package com.uvarchev.javatelebot.dto;

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

    @SerializedName("news_site")
    private String provider;

    @Override
    public String toString() {
        return title + "\n\n" +
                summary + "\n\n" +
                "https://t.me/iv?url=" + url + "&rhash=133d3fc26c9f3f" + "\n" + // instant view url
                "Published: " + publishedAt;
    }
}
