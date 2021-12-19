package com.voxeet.audio.utils;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

public class CheckFields {

    @Nullable
    public static <RETURN_TYPE> RETURN_TYPE getField(@NonNull Object object, @NonNull String name) throws Exception {
        try {
            Field field = object.getClass().getDeclaredField(name);
            field.setAccessible(true);

            return (RETURN_TYPE) field.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new Exception("invalid field requested");
        }
    }
}
