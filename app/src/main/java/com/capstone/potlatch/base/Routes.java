package com.capstone.potlatch.base;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Routes {
    public static String baseUrl = "";

    public static final String TITLE_PARAMETER = "title";
    public static final String PAGE_PARAMETER = "page";
    public static final String LIMIT_PARAMETER = "limit";
    public static final String TOP_KIND_PARAMETER = "kind";
    public static final String REGRET_PARAMETER = "regret";

    public static final String TOKEN_PATH = "/oauth/token";

    public static final String GIFTS_PATH = "/gifts";
    public static final String GIFTS_ID_PATH = GIFTS_PATH + "/{id}";
    public static final String GIFTS_UPDATE_PATH = GIFTS_ID_PATH + "/update";
    public static final String GIFTS_UPDATE_IMAGE_PATH = GIFTS_ID_PATH + "/update_image";
    public static final String MY_GIFTS_PATH = GIFTS_PATH + "/mine";
    public static final String GIFTS_TOUCH_PATH = GIFTS_ID_PATH + "/touch";
    public static final String GIFTS_INAPPROPRIATE_PATH = GIFTS_ID_PATH + "/inappropriate";

    public static final String GIFTS_CHAIN_PATH = GIFTS_PATH + "/chains";

    public static final String USERS_PATH = "/users";
    public static final String CURRENT_USER_PATH = USERS_PATH + "/current";
    public static final String TOP_GIVERS_PATH = USERS_PATH + "/top";

    public static String urlFor(String path, Object... paramsKeyValue) {
        Map<String, String> urlParams = new HashMap<String, String>();

        // First, substitute the path segments
        String url = StringUtils.stripEnd(baseUrl,"/") + "/" + StringUtils.stripStart(path, "/");
        for (int i = 0; i < paramsKeyValue.length; i += 2) {
            if (paramsKeyValue[i] == null || paramsKeyValue[i + 1] == null ) {
                continue;
            }

            String key = String.valueOf(paramsKeyValue[i]);
            String value = String.valueOf(paramsKeyValue[i + 1]);

            String pathSegmentVariable = "{" + key + "}";
            if (path.contains(pathSegmentVariable)) {
                url = url.replace(pathSegmentVariable, value);
            } else {
                urlParams.put(key, value);
            }
        }

        // Then add the rest of the params as url params
        if (urlParams.size() > 0) {
            url += "?";
            for(Map.Entry<String, String> entry : urlParams.entrySet()) {
                try {
                    url += entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8") + "&";
                } catch (UnsupportedEncodingException ignored) {}
            }
        }
        return url;
    }
}