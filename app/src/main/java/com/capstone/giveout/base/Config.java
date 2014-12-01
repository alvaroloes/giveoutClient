package com.capstone.giveout.base;

import android.content.Context;

import com.capstone.giveout.utils.ResourcesUtils;

public class Config {
    // Filled from resources: environment dependent
    public static String baseUrl;
    public static String basicAuthName;
    public static String basicAuthPass;
    public static Integer pageSize;
    @ResourcesUtils.DontFill
    public static boolean noInappropriateGifts = true;

    static public void initFor(Context context, String environment){
        ResourcesUtils.fillStatic(context, Config.class,
                environment,
                "");
    }


}