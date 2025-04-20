import React, { useEffect, useState } from "react";
import Select from "react-select";
import axios from "axios";
import "./App.css";

type WeatherResponse = {
  weather: string;
  temperature: string;
  iconUrl: string;
  
  // 追加情報
  weatherCode?: string;
  wind?: string;
  wave?: string;
  pops?: string[];
  timeDefines?: string[];
  minTemp?: string;
  maxTemp?: string;
  reliability?: string;
};

type PrefectureOption = {
  label: string;
  value: string;
};

function App() {
  const [prefectures, setPrefectures] = useState<PrefectureOption[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [weather, setWeather] = useState<WeatherResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // 都道府県一覧を取得
  useEffect(() => {
    const fetchPrefectures = async () => {
      try {
        const response = await axios.get("/api/weather/prefectures");
        const options = response.data.map((p: string) => ({ label: p, value: p }));
        setPrefectures(options);
      } catch (err) {
        console.error("都道府県データの取得に失敗しました", err);
        setError("都道府県データの取得に失敗しました");
      }
    };

    fetchPrefectures();
  }, []);

  // 選択された都道府県の天気情報を取得
  useEffect(() => {
    if (!selected) return;

    const fetchWeather = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await axios.get("/api/weather", {
          params: { prefecture: selected }
        });
        setWeather(response.data);
      } catch (err) {
        console.error("天気データの取得に失敗しました", err);
        setError("天気データの取得に失敗しました");
      } finally {
        setLoading(false);
      }
    };

    fetchWeather();
  }, [selected]);

  return (
    <div className="container">
      <h1>都道府県別 天気情報</h1>
      
      <div className="select-container">
        <Select
          options={prefectures}
          onChange={(option) => setSelected(option?.value ?? null)}
          placeholder="都道府県を選択してください"
          isClearable
          className="prefecture-select"
        />
      </div>

      {loading && <p className="loading">データを読み込み中...</p>}
      
      {error && <p className="error">{error}</p>}
      
      {weather && !loading && (
        <div className="weather-card">
          <h2>{selected}</h2>
          <div className="weather-info">
            <div className="weather-text">
              <p><strong>天気:</strong> {weather.weather}</p>
              <p><strong>気温:</strong> {weather.temperature}℃</p>
              {weather.wind && <p><strong>風:</strong> {weather.wind}</p>}
              {weather.wave && <p><strong>波:</strong> {weather.wave}</p>}
              
              {weather.pops && weather.timeDefines && (
                <div className="precipitation">
                  <p><strong>降水確率:</strong></p>
                  <ul>
                    {weather.timeDefines?.map((time, index) => (
                      weather.pops?.[index] && weather.pops[index] !== "" && (
                        <li key={index}>
                          {new Date(time).toLocaleString('ja-JP', {
                            month: 'numeric',
                            day: 'numeric',
                            hour: 'numeric'
                          })}時: {weather.pops[index]}%
                        </li>
                      )
                    ))}
                  </ul>
                </div>
              )}
              
              {weather.minTemp && weather.maxTemp && (
                <div className="forecast">
                  <p><strong>明日の予想気温:</strong></p>
                  <p>最低: {weather.minTemp}℃ / 最高: {weather.maxTemp}℃</p>
                  {weather.reliability && <p><small>信頼度: {weather.reliability}</small></p>}
                </div>
              )}
            </div>
            <div className="weather-icon">
              <img src={weather.iconUrl} alt="天気アイコン" />
              {weather.weatherCode && <p><small>コード: {weather.weatherCode}</small></p>}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
