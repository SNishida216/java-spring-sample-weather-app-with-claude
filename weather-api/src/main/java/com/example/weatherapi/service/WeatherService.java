package com.example.weatherapi.service;

import com.example.weatherapi.model.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherResponse fetchWeather(String areaCode) {
        try {
            String url = "https://www.jma.go.jp/bosai/forecast/data/forecast/" + areaCode + ".json";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if (root == null || root.isEmpty()) {
                return new WeatherResponse("データなし", "N/A", "https://cdn.weatherapi.com/weather/64x64/day/248.png");
            }
            
            JsonNode firstNode = root.get(0);
            if (firstNode == null) {
                return new WeatherResponse("データ構造エラー", "N/A", "https://cdn.weatherapi.com/weather/64x64/day/248.png");
            }
            
            JsonNode timeSeriesNode = firstNode.get("timeSeries");
            if (timeSeriesNode == null || timeSeriesNode.isEmpty()) {
                return new WeatherResponse("時系列データなし", "N/A", "https://cdn.weatherapi.com/weather/64x64/day/248.png");
            }
            
            // 天気情報の取得
            String weather = "不明";
            String temp = "N/A";
            
            JsonNode weatherSeriesNode = timeSeriesNode.get(0);
            if (weatherSeriesNode != null && weatherSeriesNode.has("areas")) {
                JsonNode areasNode = weatherSeriesNode.get("areas");
                if (areasNode != null && !areasNode.isEmpty() && areasNode.get(0) != null) {
                    JsonNode weathersNode = areasNode.get(0).get("weathers");
                    if (weathersNode != null && !weathersNode.isEmpty()) {
                        weather = weathersNode.get(0).asText();
                    }
                }
            }
            
            // 気温情報の取得
            if (timeSeriesNode.size() > 1) {
                JsonNode tempSeriesNode = timeSeriesNode.get(1);
                if (tempSeriesNode != null && tempSeriesNode.has("areas")) {
                    JsonNode areasNode = tempSeriesNode.get("areas");
                    if (areasNode != null && !areasNode.isEmpty() && areasNode.get(0) != null) {
                        JsonNode tempsNode = areasNode.get(0).get("temps");
                        if (tempsNode != null && !tempsNode.isEmpty()) {
                            temp = tempsNode.get(0).asText();
                        }
                    }
                }
            }
            
            return new WeatherResponse(weather, temp, getIconUrl(weather));
        } catch (Exception e) {
            e.printStackTrace();
            return new WeatherResponse("データ取得エラー: " + e.getMessage(), "N/A", "https://cdn.weatherapi.com/weather/64x64/day/248.png");
        }
    }

    private String getIconUrl(String weather) {
        if (weather.contains("晴")) return "https://cdn.weatherapi.com/weather/64x64/day/113.png";
        if (weather.contains("雨")) return "https://cdn.weatherapi.com/weather/64x64/day/296.png";
        if (weather.contains("雪")) return "https://cdn.weatherapi.com/weather/64x64/day/326.png";
        if (weather.contains("曇")) return "https://cdn.weatherapi.com/weather/64x64/day/119.png";
        return "https://cdn.weatherapi.com/weather/64x64/day/116.png";
    }
}
