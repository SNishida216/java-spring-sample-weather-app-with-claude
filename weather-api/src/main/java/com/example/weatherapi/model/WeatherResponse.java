package com.example.weatherapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherResponse {
    private String weather;
    private String temperature;
    private String iconUrl;
    
    // 追加情報
    private String weatherCode;
    private String wind;
    private String wave;
    private List<String> pops;
    private List<String> timeDefines;
    private String minTemp;
    private String maxTemp;
    private String reliability;
    
    public WeatherResponse(String weather, String temperature, String iconUrl) {
        this.weather = weather;
        this.temperature = temperature;
        this.iconUrl = iconUrl;
    }
    
    public WeatherResponse(String weather, String temperature, String iconUrl,
                          String weatherCode, String wind, String wave,
                          List<String> pops, List<String> timeDefines,
                          String minTemp, String maxTemp, String reliability) {
        this.weather = weather;
        this.temperature = temperature;
        this.iconUrl = iconUrl;
        this.weatherCode = weatherCode;
        this.wind = wind;
        this.wave = wave;
        this.pops = pops;
        this.timeDefines = timeDefines;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.reliability = reliability;
    }
}
