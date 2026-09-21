package com.corpedia.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 高德(AMap)天气工具。通过 @Tool 注解暴露给 LLM，使大模型能按问题自主选择调用，
 * 用于回答"XX 城市今天天气如何"等实时天气类问题（补知识库时效性短板）。
 *
 * 依赖：AMap 开放平台 Web 服务天气 API（v3），需在 application.yml 配置 corpedia.amap.api-key。
 */
@Service
public class AmapWeatherTool {

    private static final Logger log = LoggerFactory.getLogger(AmapWeatherTool.class);

    /** 高德天气实时查询接口（extension=base 仅返回实时天气）。 */
    private static final String WEATHER_URL =
            "https://restapi.amap.com/v3/weather/weatherInfo?key={key}&city={city}&extensions=base&output=json";

    private final RestClient restClient;
    private final String apiKey;

    public AmapWeatherTool(RestClient.Builder restClientBuilder,
                           @Value("${corpedia.amap.api-key:}") String apiKey) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
    }

    /**
     * 查询指定城市当天的实时天气情况。
     *
     * @param city 城市名称，如"北京"、"上海"、"广州"
     * @return 该城市实时天气信息（天气状况、温度、风向风力、湿度、发布时间）；查询失败时返回错误说明
     */
    @Tool(name = "query_weather", description = "查询指定城市当天的实时天气，输入城市名称，例如：北京、上海、广州")
    public String queryWeather(
            @ToolParam(description = "城市名称，如：北京、浙江杭州、上海") String city) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[AmapWeather] 未配置 corpedia.amap.api-key，无法查询天气");
            return "天气服务未配置（缺少高德 API Key），无法查询。";
        }
        try {
            AmapWeatherResponse resp = restClient.get()
                    .uri(WEATHER_URL, apiKey, city)
                    .retrieve()
                    .body(AmapWeatherResponse.class);
            if (resp == null || !"1".equals(resp.status()) || resp.lives() == null || resp.lives().isEmpty()) {
                log.warn("[AmapWeather] 高德返回异常：{} / {}", resp == null ? "null" : resp.status(), resp == null ? "" : resp.info());
                return String.format("未能获取 %s 的天气信息（服务返回码 %s）。",
                        city, resp == null ? "null" : resp.infocode());
            }
            AmapWeatherResponse.Live live = resp.lives().get(0);
            return String.format("%s%s %s 实时天气：%s，气温 %s℃，风向 %s，风力 %s，湿度 %s%%，数据时间 %s",
                    live.province(), live.city(), "今天", live.weather(), live.temperature(),
                    live.winddirection(), live.windpower(), live.humidity(), live.reporttime());
        } catch (Exception e) {
            log.error("[AmapWeather] 查询 {} 天气异常", city, e);
            return String.format("查询 %s 天气失败：%s", city, e.getMessage());
        }
    }

    /** 高德天气接口响应（只取所需字段）。 */
    public record AmapWeatherResponse(String status, String info, String infocode, List<Live> lives) {
        public record Live(String province, String city, String adcode, String weather, String temperature,
                           String winddirection, String windpower, String humidity, String reporttime) {
        }
    }
}