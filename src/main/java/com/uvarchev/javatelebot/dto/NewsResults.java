package com.uvarchev.javatelebot.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Stack;

@Setter
@Getter
@NoArgsConstructor
public class NewsResults {
    @SerializedName("results")
    private Stack<News> results;
}
