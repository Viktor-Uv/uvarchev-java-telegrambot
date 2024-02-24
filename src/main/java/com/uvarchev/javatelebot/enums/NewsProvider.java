package com.uvarchev.javatelebot.enums;

public enum NewsProvider {
    ARSTECHNICA("Arstechnica"),
    EUROPEAN_SPACEFLIGHT("European Spaceflight"),
    NASA("NASA"),
    NASASPACEFLIGHT("NASASpaceflight"),
    SPACENEWS("SpaceNews"),
    SPACEPOLICYONLINE("SpacePolicyOnline.com"),
    SPACE_SCOUT("Space Scout");

    private final String apiName;

    NewsProvider(String apiName) {
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }
}
