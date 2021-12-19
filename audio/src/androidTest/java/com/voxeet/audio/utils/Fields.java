package com.voxeet.audio.utils;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Fields {

    public static void setBuildBrand(@NonNull String new_brand) {
        Field brand = null;
        try {
            brand = Build.class.getField("BRAND");
            //change the visibility to edit it
            brand.setAccessible(true);


            for (Field modifier : Field.class.getDeclaredFields()) {
                Log.d("FIELDS", "setBuildBrand: " + modifier.getName());
            }
            try {
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(brand, brand.getModifiers() & ~Modifier.FINAL);
            }catch (Exception e){
                //nothing
            }

            brand.set(null, new_brand);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
