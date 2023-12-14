package com.uvarchev.javatelebot.service;

import com.google.gson.annotations.SerializedName;
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
