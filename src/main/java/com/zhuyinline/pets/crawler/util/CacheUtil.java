package com.zhuyinline.pets.crawler.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Devin Zhang
 * @className CacheUtil
 * @description TODO
 * @date 2020/1/10 10:18
 */

public class CacheUtil {

    private static Map<String, Object> cacheMap;

    public void put(String key, Object value) {
        if (cacheMap == null) {
            cacheMap = new HashMap();
        }
        cacheMap.put(key, value);
    }

    public Object get(String key) {
        if (cacheMap == null) {
            return null;
        }
        return cacheMap.get(key);
    }
}
