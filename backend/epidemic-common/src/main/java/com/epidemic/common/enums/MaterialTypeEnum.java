package com.epidemic.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 物资类型枚举
 * 统一管理物资类型代码和中文名称的映射
 */
public enum MaterialTypeEnum {

    PROTECTIVE("protective", "防护物资"),
    DISINFECTION("disinfection", "消杀物资"),
    TESTING("testing", "检测物资"),
    EQUIPMENT("equipment", "医疗设备"),
    OTHER("other", "其他物资");

    private final String code;
    private final String name;

    MaterialTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据代码获取中文名称
     *
     * @param code 类型代码
     * @return 中文名称，如果不存在返回原代码
     */
    public static String getNameByCode(String code) {
        for (MaterialTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type.name;
            }
        }
        return code;
    }

    /**
     * 获取所有类型的Map列表
     *
     * @return Map列表，包含code和name
     */
    public static java.util.List<Map<String, String>> toList() {
        java.util.List<Map<String, String>> list = new java.util.ArrayList<>();
        for (MaterialTypeEnum type : values()) {
            Map<String, String> map = new HashMap<>();
            map.put("code", type.code);
            map.put("name", type.name);
            list.add(map);
        }
        return list;
    }

    /**
     * 获取类型名称Map（code -> name）
     *
     * @return 类型名称映射
     */
    public static Map<String, String> toNameMap() {
        Map<String, String> map = new HashMap<>();
        for (MaterialTypeEnum type : values()) {
            map.put(type.code, type.name);
        }
        return map;
    }
}
