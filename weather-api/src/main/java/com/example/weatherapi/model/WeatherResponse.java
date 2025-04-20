package com.example.weatherapi.model;

import lombok.Data;

@Data
public class WeatherResponse {
    private String weather;
    private String temperature;
    private String iconUrl;
    
    public WeatherResponse(String weather, String temperature, String iconUrl) {
        this.weather = weather;
        this.temperature = temperature;
        this.iconUrl = iconUrl;
    }
}
