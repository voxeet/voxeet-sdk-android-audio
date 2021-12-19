package com.voxeet.audio.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Invoke {
    public static int callReturnIntVoidArg(@Nullable Object object,
                                           @NonNull String methodName,
                                           int defaultValue) {
        try {
            if (null != object) {
                Method method = object.getClass().getDeclaredMethod(methodName);

                Object result = method.invoke(object);
                return (int) result;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static void callVoidIntArg(Object object, String methodName, int parameter) {
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, int.class);

            method.invoke(object, parameter);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
