package com.adobe.platform.streaming.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public abstract class AbstractStreamingUtils {



    public static int getProperty(Map<String, String> props, String propertyName, int defaultValue) {
        if (props != null) {
            String propertyValue = props.get(propertyName);
            if (StringUtils.isNotBlank(propertyValue)) {
                return Integer.parseInt(propertyValue);
            }
        }

        return defaultValue;
    }

    public static String getProperty(Map<String, String> props, String propertyName, String defaultValue) {
        return props != null ? (StringUtils.isNotBlank(props.get(propertyName)) ? props.get(propertyName) : defaultValue)
                : defaultValue;
    }

    public static int getProperty(Map<String, String> properties, String key, int defaultValue, int multiplier) {
        int propertyValue = getProperty(properties, key, defaultValue) * multiplier;
        if (propertyValue < 1) {
            return defaultValue * multiplier;
        }

        return propertyValue;
    }

}

