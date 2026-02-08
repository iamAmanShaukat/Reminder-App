package com.example.reminder.utils;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import java.io.Serializable;

public class CompatUtils {

    @SuppressWarnings({ "deprecation", "unchecked" })
    public static <T extends Serializable> T getSerializable(Bundle bundle, String key, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getSerializable(key, clazz);
        } else {
            return (T) bundle.getSerializable(key);
        }
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    public static <T extends Parcelable> T getParcelableExtra(Intent intent, String key, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(key, clazz);
        } else {
            return intent.getParcelableExtra(key);
        }
    }
}
