package com.kotlin.widget.utils;

import android.content.Context;

public class SizeUtils {

    public static float Dp2Px(Context context, float value) {
        float scale = context.getResources().getDisplayMetrics().density;
        return value * scale ;
    }

    public static float Sp2Px(Context context , float value){
        float scale = context.getResources().getDisplayMetrics().density;
        return value * scale;
    }
}
