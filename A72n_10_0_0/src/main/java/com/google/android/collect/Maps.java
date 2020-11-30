package com.google.android.collect;

import android.annotation.UnsupportedAppUsage;
import android.util.ArrayMap;
import java.util.HashMap;

public class Maps {
    @UnsupportedAppUsage
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    public static <K, V> ArrayMap<K, V> newArrayMap() {
        return new ArrayMap<>();
    }
}
