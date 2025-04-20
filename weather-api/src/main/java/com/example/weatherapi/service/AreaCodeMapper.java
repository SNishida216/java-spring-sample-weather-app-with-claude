package com.example.weatherapi.service;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class AreaCodeMapper {
    private static final Map<String, String> PREF_CODE_MAP = new HashMap<>() {{
        put("北海道", "016000");
        put("青森県", "020000");
        put("岩手県", "030000");
        put("宮城県", "040000");
        put("秋田県", "050000");
        put("山形県", "060000");
        put("福島県", "070000");
        put("茨城県", "080000");
        put("栃木県", "090000");
        put("群馬県", "100000");
        put("埼玉県", "110000");
        put("千葉県", "120000");
        put("東京都", "130000");
        put("神奈川県", "140000");
        put("新潟県", "150000");
        put("富山県", "160000");
        put("石川県", "170000");
        put("福井県", "180000");
        put("山梨県", "190000");
        put("長野県", "200000");
        put("岐阜県", "210000");
        put("静岡県", "220000");
        put("愛知県", "230000");
        put("三重県", "240000");
        put("滋賀県", "250000");
        put("京都府", "260000");
        put("大阪府", "270000");
        put("兵庫県", "280000");
        put("奈良県", "290000");
        put("和歌山県", "300000");
        put("鳥取県", "310000");
        put("島根県", "320000");
        put("岡山県", "330000");
        put("広島県", "340000");
        put("山口県", "350000");
        put("徳島県", "360000");
        put("香川県", "370000");
        put("愛媛県", "380000");
        put("高知県", "390000");
        put("福岡県", "400000");
        put("佐賀県", "410000");
        put("長崎県", "420000");
        put("熊本県", "430000");
        put("大分県", "440000");
        put("宮崎県", "450000");
        put("鹿児島県", "460100");
        put("沖縄県", "471000");
    }};

    public String getAreaCode(String prefectureName) {
        return PREF_CODE_MAP.get(prefectureName);
    }

    public Set<String> getAllPrefectures() {
        return PREF_CODE_MAP.keySet();
    }
}
