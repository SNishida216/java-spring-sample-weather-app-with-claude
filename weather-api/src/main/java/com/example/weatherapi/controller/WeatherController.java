package com.example.weatherapi.controller;

import com.example.weatherapi.model.WeatherResponse;
import com.example.weatherapi.service.AreaCodeMapper;
import com.example.weatherapi.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final AreaCodeMapper areaCodeMapper;
    private final WeatherService weatherService;

    @GetMapping("/prefectures")
    public List<String> getPrefectures() {
        return new ArrayList<>(areaCodeMapper.getAllPrefectures());
    }

    @GetMapping
    public ResponseEntity<?> getWeather(@RequestParam String prefecture) {
        String code = areaCodeMapper.getAreaCode(prefecture);
        if (code == null) {
            return ResponseEntity.badRequest().body("Invalid prefecture");
        }
        WeatherResponse res = weatherService.fetchWeather(code);
        return ResponseEntity.ok(res);
    }
}
