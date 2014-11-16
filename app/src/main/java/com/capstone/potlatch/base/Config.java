package com.capstone.potlatch.base;

import android.content.Context;

import com.capstone.potlatch.utils.ResourcesUtils;

public class Config {
    // Filled from resources: environment dependent
    static public String baseUrl;

    static public void initFor(Context context, String environment){
        ResourcesUtils.fillStatic(context, Config.class,
                environment,
                "");
    }


}