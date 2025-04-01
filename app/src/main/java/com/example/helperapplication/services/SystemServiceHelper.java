package com.example.helperapplication.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SystemServiceHelper extends Service {
    private static final List<String> availableSystemService = List.of(Context.AUDIO_SERVICE,
            Context.CAMERA_SERVICE, Context.CONNECTIVITY_SERVICE, Context.MEDIA_ROUTER_SERVICE,
            Context.NFC_SERVICE, Context.LOCATION_SERVICE, Context.POWER_SERVICE,
            Context.SENSOR_SERVICE, Context.USB_SERVICE, Context.WIFI_SERVICE,
            Context.WINDOW_SERVICE);
    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(String.class, Boolean.class,
            Double.class, Float.class, Integer.class, Long.class);

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static List<String> getAvailableSystemService() {
        return availableSystemService;
    }

    public static List<String> getServiceMethods(Context context, String serviceName) {
        List<String> methodsList = new ArrayList<>();
        try {
            Object service = context.getSystemService(serviceName.toLowerCase(Locale.ROOT));
            if (service != null) {
                Method[] methods = service.getClass().getMethods();
                for (Method method : methods) {
                    if(
                        !hasComplexParameter(method) &&
                        !(method.getName().startsWith("wait"))
                        && !(method.getName().startsWith("notify"))
                        && !(method.getName().startsWith("equals"))) {
                        methodsList.add(method.getName());
                    }
                }
            }else {
                methodsList.add("Error: service not found.");
            }
        } catch (Exception e){
            methodsList.add("Error: " + e.getMessage());
        }
        return methodsList;
    }
    private static boolean hasComplexParameter(Method method) {
        for (Parameter parameter : method.getParameters()) {
            Class<?> paramType = parameter.getType();

            if (isComplexType(paramType)) {
                return true;
            }
            if (List.class.isAssignableFrom(paramType) ||
                    Set.class.isAssignableFrom(paramType) ||
                    Map.class.isAssignableFrom(paramType) ||
                    paramType.isArray()) {

                Type genericType = parameter.getParameterizedType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    for (Type typeArg : parameterizedType.getActualTypeArguments()) {
                        if (typeArg instanceof Class) {
                            Class<?> argClass = (Class<?>) typeArg;
                            if (isComplexType(argClass)) {
                                return true;
                            }
                        }
                    }
                } else if (paramType.isArray()) {
                    Class<?> componentType = paramType.getComponentType();
                    if (isComplexType(componentType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private static boolean isComplexType(Class<?> type) {
        return !(type.isPrimitive() || SIMPLE_TYPES.contains(type));
    }
}