package com.j180.erp.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j180.erp.common.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * JSON 工具类（敏感字段脱敏后序列化）
 */
@Slf4j
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

    /** 需要脱敏的属性名（不区分大小写包含即命中） */
    private static final Pattern SENSITIVE_KEY = Pattern.compile("\"(password|passwordHash|oldPassword|newPassword)\"\\s*:\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);

    private JsonUtil() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * 序列化为 JSON 字符串，失败返回 null（不影响主流程）
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON序列化失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 序列化为脱敏后的 JSON（密码类字段打码），用于审计快照
     */
    public static String toMaskedJson(Object obj) {
        String json = toJson(obj);
        if (json == null) {
            return null;
        }
        return SENSITIVE_KEY.matcher(json).replaceAll(m -> m.group().replaceAll("\"[^\"]*\"$", "\"***\""));
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new BizException("JSON解析失败: " + e.getMessage());
        }
    }

    public static <T> T convertValue(Object obj, Class<T> clazz) {
        return MAPPER.convertValue(obj, clazz);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        return MAPPER.convertValue(obj, Map.class);
    }
}
