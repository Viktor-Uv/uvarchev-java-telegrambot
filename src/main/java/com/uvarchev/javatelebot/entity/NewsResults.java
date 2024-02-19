package com.uvarchev.javatelebot.entity;

import com.google.gson.annotations.SerializedName;
import com.uvarchev.javatelebot.entity.News;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class NewsResults {
    @SerializedName("results")
    private List<News> results;
}
