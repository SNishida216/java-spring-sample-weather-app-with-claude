package com.example.weatherapi.service;

import com.example.weatherapi.model.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

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
            
            // 追加情報の初期化
            String weatherCode = "";
            String wind = "";
            String wave = "";
            List<String> pops = new ArrayList<>();
            List<String> timeDefines = new ArrayList<>();
            String minTemp = "";
            String maxTemp = "";
            String reliability = "";
            
            // 天気、天気コード、風、波の情報取得
            JsonNode weatherSeriesNode = timeSeriesNode.get(0);
            if (weatherSeriesNode != null && weatherSeriesNode.has("areas")) {
                JsonNode areasNode = weatherSeriesNode.get("areas");
                if (areasNode != null && !areasNode.isEmpty() && areasNode.get(0) != null) {
                    JsonNode areaNode = areasNode.get(0);
                    
                    // 天気情報
                    JsonNode weathersNode = areaNode.get("weathers");
                    if (weathersNode != null && !weathersNode.isEmpty()) {
                        weather = weathersNode.get(0).asText();
                    }
                    
                    // 天気コード
                    JsonNode weatherCodesNode = areaNode.get("weatherCodes");
                    if (weatherCodesNode != null && !weatherCodesNode.isEmpty()) {
                        weatherCode = weatherCodesNode.get(0).asText();
                    }
                    
                    // 風の情報
                    JsonNode windsNode = areaNode.get("winds");
                    if (windsNode != null && !windsNode.isEmpty()) {
                        wind = windsNode.get(0).asText();
                    }
                    
                    // 波の情報
                    JsonNode wavesNode = areaNode.get("waves");
                    if (wavesNode != null && !wavesNode.isEmpty()) {
                        wave = wavesNode.get(0).asText();
                    }
                }
            }
            
            // 降水確率と時間帯の取得
            if (timeSeriesNode.size() > 1) {
                JsonNode popSeriesNode = timeSeriesNode.get(1);
                if (popSeriesNode != null) {
                    // 時間帯の定義を取得
                    JsonNode timeDefinesNode = popSeriesNode.get("timeDefines");
                    if (timeDefinesNode != null && timeDefinesNode.isArray()) {
                        for (JsonNode timeNode : timeDefinesNode) {
                            timeDefines.add(timeNode.asText());
                        }
                    }
                    
                    // 降水確率を取得
                    if (popSeriesNode.has("areas")) {
                        JsonNode areasNode = popSeriesNode.get("areas");
                        if (areasNode != null && !areasNode.isEmpty() && areasNode.get(0) != null) {
                            JsonNode popsNode = areasNode.get(0).get("pops");
                            if (popsNode != null && popsNode.isArray()) {
                                for (JsonNode popNode : popsNode) {
                                    pops.add(popNode.asText());
                                }
                            }
                        }
                    }
                }
                
                // 気温情報の取得
                JsonNode tempSeriesNode = timeSeriesNode.get(2);
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
            
            // 週間予報から最高/最低気温と信頼度を取得
            if (root.size() > 1) {
                JsonNode weeklyNode = root.get(1);
                if (weeklyNode != null && weeklyNode.has("timeSeries")) {
                    JsonNode weeklyTimeSeriesNode = weeklyNode.get("timeSeries");
                    
                    // 信頼度の取得
                    if (weeklyTimeSeriesNode.size() > 0) {
                        JsonNode reliabilitySeriesNode = weeklyTimeSeriesNode.get(0);
                        if (reliabilitySeriesNode != null && reliabilitySeriesNode.has("areas")) {
                            JsonNode areasNode = reliabilitySeriesNode.get("areas");
                            if (areasNode != null && !areasNode.isEmpty()) {
                                for (JsonNode areaNode : areasNode) {
                                    if (areaNode.has("area") && areaNode.get("area").has("name") 
                                        && areaNode.get("area").get("name").asText().contains("東京")) {
                                        
                                        JsonNode reliabilitiesNode = areaNode.get("reliabilities");
                                        if (reliabilitiesNode != null && reliabilitiesNode.size() > 1) {
                                            reliability = reliabilitiesNode.get(1).asText();
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    // 最高/最低気温の取得
                    if (weeklyTimeSeriesNode.size() > 1) {
                        JsonNode tempSeriesNode = weeklyTimeSeriesNode.get(1);
                        if (tempSeriesNode != null && tempSeriesNode.has("areas")) {
                            JsonNode areasNode = tempSeriesNode.get("areas");
                            if (areasNode != null && !areasNode.isEmpty()) {
                                for (JsonNode areaNode : areasNode) {
                                    if (areaNode.has("area") && areaNode.get("area").has("name") 
                                        && areaNode.get("area").get("name").asText().contains("東京")) {
                                        
                                        if (areaNode.has("tempsMin") && areaNode.get("tempsMin").size() > 1) {
                                            minTemp = areaNode.get("tempsMin").get(1).asText();
                                        }
                                        
                                        if (areaNode.has("tempsMax") && areaNode.get("tempsMax").size() > 1) {
                                            maxTemp = areaNode.get("tempsMax").get(1).asText();
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            return new WeatherResponse(
                weather, temp, getIconUrl(weather),
                weatherCode, wind, wave, pops, timeDefines,
                minTemp, maxTemp, reliability
            );
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
