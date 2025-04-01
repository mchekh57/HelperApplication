package com.example.helperapplication.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ConverterHelperService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

public static String formatResult(Object result) {
    if (result == null) return "No result";

    if (result instanceof CharSequence || result instanceof Number || result instanceof Boolean) {
        return result.toString();
    } else if (result instanceof List<?> list) {
        return list.stream()
                .map(ConverterHelperService::formatResult)
                .collect(Collectors.joining("\n"));
    } else if (result instanceof Map<?, ?> map) {
        return map.entrySet().stream()
                .map(e -> formatResult(e.getKey()) + " -> " + formatResult(e.getValue()))
                .collect(Collectors.joining("\n"));
    } else if (result instanceof Object[] array) {
        return Arrays.stream(array)
                .map(ConverterHelperService::formatResult)
                .collect(Collectors.joining(", "));
    }
    return extractObjectDetails(result);
}

    private static String extractObjectDetails(Object obj) {
        StringBuilder result = new StringBuilder(obj.getClass().getSimpleName()).append(":\n");
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                result.append(field.getName()).append(" = ").append(field.get(obj)).append("\n");
            } catch (Exception e) {
                result.append(field.getName()).append(" = Error: ").append(e.getMessage()).append("\n");
            }
        }
        return formatResult(result);//result.toString();
    }

    public static Object convertParameter(Parameter parameter, String value) {
        Class<?> type = parameter.getType();
        if (type.isArray()) {
            return convertArray(parameter, value);
        } else if (List.class.isAssignableFrom(type)) {
            return convertCollection(parameter, value, Collectors.toList());
        } else if (Set.class.isAssignableFrom(type)) {
            return convertCollection(parameter, value, Collectors.toSet());
        } else if (Map.class.isAssignableFrom(type)) {
            return convertMap(parameter, value);
        }
        return convertSingleValue(type, value);
    }

    private static Object convertArray(Parameter parameter, String value) {
        Class<?> componentType = parameter.getType().getComponentType();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .map(v -> convertSingleValue(componentType, v))
                .toArray();
    }

    private static <T> Object convertCollection(Parameter parameter, String value, Collector<Object, ?, T> collector) {
        Class<?> componentType = getGenericType(parameter);
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .map(v -> convertSingleValue(componentType, v))
                .collect(collector);
    }

    private static Object convertMap(Parameter parameter, String value) {
        Class<?>[] keyValueTypes = getGenericTypes(parameter);
        Map<Object, Object> map = new HashMap<>();
        Arrays.stream(value.split(","))
                .map(String::trim)
                .map(entry -> entry.split("="))
                .filter(keyValue -> keyValue.length == 2)
                .forEach(keyValue -> {
                    Object key = convertSingleValue(keyValueTypes[0], keyValue[0].trim());
                    Object val = convertSingleValue(keyValueTypes[1], keyValue[1].trim());
                    if (key != null && val != null) {
                        map.put(key, val);
                    }
                });
        return map;
    }


    private static Class<?> getGenericType(Parameter parameter) {
        if (parameter.getParameterizedType() instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
        }
        return String.class;
    }

    private static Class<?>[] getGenericTypes(Parameter parameter) {
        if (parameter.getParameterizedType() instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) parameter.getParameterizedType();
            return new Class<?>[]{
                    (Class<?>) type.getActualTypeArguments()[0],
                    (Class<?>) type.getActualTypeArguments()[1]
            };
        }
        return new Class<?>[]{String.class, String.class};
    }

    private static Object convertSingleValue(Class<?> type, String value) {
        return switch (type.getName()) {
            case "java.lang.String" -> value;
            case "int", "java.lang.Integer" -> parseOrDefault(value, Integer::parseInt, 0);
            case "boolean", "java.lang.Boolean" -> Boolean.parseBoolean(value);
            case "float", "java.lang.Float" -> parseOrDefault(value, Float::parseFloat, 0f);
            case "double", "java.lang.Double" -> parseOrDefault(value, Double::parseDouble, 0.0);
            case "long", "java.lang.Long" -> parseOrDefault(value, Long::parseLong, 0L);
            default -> value;
        };
    }

    private static <T> T parseOrDefault(String value, Parser<T> parser, T defaultValue) {
        try {
            return parser.parse(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private interface Parser<T> {
        T parse(String value);
    }
}