# Cline(claude3.7-sonnet)+ChatGPT 4oで作成したアプリケーション
## 作成手順
以下をChatGPTに指示
```
# 都道府県ごとの天気情報を取得し、画面に表示するアプリケーション
## 機能要件
- Webアプリケーション
- ユーザがドロップダウン形式で参照したい都道府県を選択すると、その都道府県の天気、気温、天気アイコンが表示される
  - ドロップダウンはユーザ入力により候補を絞り込める

## 利用技術など
- BE：java+spring boot(MVCモデル)
- FE：react + typescript
- 天気情報を取得するAPIは「気象庁オープンデータ」を利用
  - URL: https://www.data.jma.go.jp/developer/index.html
  - 都道府県とAPIリクエストに必要なエリアコードの対応表はハードコーディングして保持しておく

---

上記アプリケーションを構築するための作業書を作成して。
この作業書はclaudeに読ませて作業する形式にして。
```

以下のプロンプトが生成されるので、clineに読み込ませる
```
以下は、上記要件に基づくアプリケーションを構築するための**作業手順書（作業書）**です。ClaudeなどのAIエージェントが手順に従って実装できるよう、**段階的・明確に**記述しています。

---

# 📘 Webアプリケーション構築作業書  
**アプリ名**：都道府県別天気表示アプリ  
**目的**：ユーザが都道府県を選択し、該当地域の天気情報（天気・気温・天気アイコン）を表示するWebアプリを構築する。  
**構成**：バックエンド(Spring Boot) + フロントエンド(React + TypeScript)  

---

## ✅ 構成一覧
- バックエンド：Java + Spring Boot（MVC構成）
- フロントエンド：React + TypeScript（Vite推奨）
- API：気象庁オープンデータ（https://www.data.jma.go.jp/developer/index.html）
- 都道府県・エリアコード：バックエンドで定数として保持

---

## 1️⃣ バックエンド作業（Spring Boot）

### 1-1. プロジェクト作成
- Spring Initializrでプロジェクトを作成（名前：weather-api）
- 依存関係に以下を追加：
  - Spring Web
  - Spring Boot DevTools
  - Lombok
  - Spring Configuration Processor

---

### 1-2. 都道府県とエリアコードのマッピング定義
- Javaクラスを用意し、都道府県とエリアコードの対応表をハードコーディング

```java
// AreaCodeMapper.java
@Component
public class AreaCodeMapper {
    private static final Map<String, String> PREF_CODE_MAP = Map.of(
        "北海道", "016000", 
        "青森県", "020000",
        "岩手県", "030000"
        // ...省略: 全都道府県分追加する
    );

    public String getAreaCode(String prefectureName) {
        return PREF_CODE_MAP.get(prefectureName);
    }

    public Set<String> getAllPrefectures() {
        return PREF_CODE_MAP.keySet();
    }
}
```

---

### 1-3. コントローラーの実装

```java
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
        if (code == null) return ResponseEntity.badRequest().body("Invalid prefecture");
        WeatherResponse res = weatherService.fetchWeather(code);
        return ResponseEntity.ok(res);
    }
}
```

---

### 1-4. サービス層で天気情報を取得

- 気象庁のオープンデータJSONを取得してパースする
- 気象庁のデータ形式に応じてJSON構造を確認して実装（例：天気予報JSON）

```java
@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherResponse fetchWeather(String areaCode) {
        String url = "https://www.jma.go.jp/bosai/forecast/data/forecast/" + areaCode + ".json";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // JSONパース（Jacksonなど使用）
        // 簡略化のため疑似コード：
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        String weather = root.get(0).get("timeSeries").get(0).get("areas").get(0).get("weathers").get(0).asText();
        String temp = root.get(1).get("timeSeries").get(1).get("areas").get(0).get("temps").get(0).asText();

        return new WeatherResponse(weather, temp, getIconUrl(weather));
    }

    private String getIconUrl(String weather) {
        if (weather.contains("晴")) return "/icons/sunny.png";
        if (weather.contains("雨")) return "/icons/rainy.png";
        return "/icons/cloudy.png";
    }
}
```

---

### 1-5. レスポンス用DTO作成

```java
@Data
@AllArgsConstructor
public class WeatherResponse {
    private String weather;
    private String temperature;
    private String iconUrl;
}
```

---

## 2️⃣ フロントエンド作業（React + TypeScript）

### 2-1. プロジェクト作成

```sh
npm create vite@latest weather-frontend --template react-ts
cd weather-frontend
npm install
```

---

### 2-2. 都道府県選択 + API呼び出しUI

- ユーザーが選択肢を絞り込めるドロップダウン（`react-select` 推奨）

```tsx
// App.tsx
import React, { useEffect, useState } from "react";
import Select from "react-select";
import axios from "axios";

type WeatherResponse = {
  weather: string;
  temperature: string;
  iconUrl: string;
};

function App() {
  const [prefectures, setPrefectures] = useState<{ label: string; value: string }[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [weather, setWeather] = useState<WeatherResponse | null>(null);

  useEffect(() => {
    axios.get("/api/weather/prefectures").then((res) => {
      const options = res.data.map((p: string) => ({ label: p, value: p }));
      setPrefectures(options);
    });
  }, []);

  useEffect(() => {
    if (!selected) return;
    axios.get("/api/weather", { params: { prefecture: selected } }).then((res) => {
      setWeather(res.data);
    });
  }, [selected]);

  return (
    <div className="p-6 max-w-md mx-auto">
      <h1 className="text-xl font-bold mb-4">都道府県別 天気情報</h1>
      <Select options={prefectures} onChange={(e) => setSelected(e?.value ?? null)} />
      {weather && (
        <div className="mt-4 p-4 border rounded shadow">
          <p>天気: {weather.weather}</p>
          <p>気温: {weather.temperature}℃</p>
          <img src={weather.iconUrl} alt="weather icon" width={64} />
        </div>
      )}
    </div>
  );
}

export default App;
```

---

### 2-3. プロキシ設定（`vite.config.ts`）

```ts
server: {
  proxy: {
    "/api": "http://localhost:8080"
  }
}
```

---

## 3️⃣ 実行手順

1. Spring Bootアプリ（`weather-api`）を起動：
   ```sh
   ./mvnw spring-boot:run
   ```

2. Reactアプリ（`weather-frontend`）を起動：
   ```sh
   npm run dev
   ```

3. ブラウザで `http://localhost:5173` にアクセスし、動作確認。

---

## 🔧 今後の拡張案（オプション）
- 天気アイコンをJMAの公式アイコンに切り替える
- 都道府県選択後、他の地域予報も表示（週間予報など）
- 選択履歴保存（localStorage）

---

必要に応じてこの作業書をモジュール単位に分割したり、Claudeでの自動処理用に手順記述をさらに詳細化できます。分割形式で欲しい場合はお知らせください！
```

あとはclineによるコードジェネレートによってAPP完成

## 詰まったところ
- clineが時々止まる
  - 新しいタスク→履歴から開く→resumeで解決
- mvnが使えないでつまづく（入れてあげた）
- 気温がN/Aになる（レスポンス見てみないとなんとも）バグが直っていない
- 別のアプリケーションでポート使われている問題が起きたが、解決は難しそうだった

## かかった費用
$2.3855
デバッグに複数リクエスト取られていたのでもう少し抑える工夫はできそう