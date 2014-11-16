package com.capstone.potlatch.utils;

import android.content.Context;
import android.content.res.Resources;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ResourcesUtils {

    public @Retention(RetentionPolicy.RUNTIME) @interface DontFill {}

    public static String prefixSeparator = "_";
    public static Class ignoreFilter = DontFill.class;

    /**
     * Fills an object from resource values. The field names and types must match the resource name
     * and type. Only Integer, String and its corresponding arrays are supported.
     * If an object field has the annotation {@link com.capstone.potlatch.utils.ResourcesUtils.DontFill}
     * it will be ignored
     * @param context The context
     * @param object The instance of the object to fill.
     * @param prefix The prefix the resource names have. You can pass multiple prefixes here and they
     *               will be searched in order to find the right value. For example, if the object
     *               passed has the field "public String baseUrl:", and you pass the prefixes
     *               {"production", "staging", ""}, these resources will be searched: "production_baseUrl",
     *               "staging_baseUrl" and "baseUrl". The first one found will be used to fill the field.
     */
    static public void fill(Context context, Object object, String... prefix) {
        fill(context, object.getClass(), object, prefix);
    }

    /**
     * Works in the same way as {@link #fill(android.content.Context, Object, String...)} but it fills
     * the static fields of the class passed.
     */
    static public void fillStatic(Context context, Class klass, String... prefixList) {
        fill(context, klass, null, prefixList);
    }

    @SuppressWarnings("unchecked")
    static public void fill(Context context, Class klass, Object object, String... prefixList){
        try {
            String packageName = context.getPackageName();
            Resources res = context.getResources();

            Field[] fields = klass.getDeclaredFields();
            for( Field field : fields ){

                // Ignore fields annotated with ignoreFilter
                if (ignoreFilter != null && field.getAnnotation(DontFill.class) != null)
                    continue;

                Class fieldClass = field.getType();
                String fieldName = field.getName();

                Object value = null;
                int resId;

                for(String prefix : prefixList) {
                    prefix += prefixSeparator;

                    // Check collection resource types (integer and string arrays)
                    if (Collection.class.isAssignableFrom(fieldClass)) {
                        resId = res.getIdentifier(prefix + fieldName, "array", packageName);
                        if (resId <= 0) continue; // Continue with another prefix

                        Class componentType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                        if (Integer.class.isAssignableFrom(componentType)) {
                            int[] ints = res.getIntArray(resId);
                            value = new ArrayList<Integer>(ints.length);
                            for (int v : ints) ((List<Integer>) value).add(v);
                        } else {
                            value = Arrays.asList(res.getStringArray(resId));
                        }
                    }
                    else if (Integer.class.isAssignableFrom(fieldClass)) {
                        resId = res.getIdentifier(prefix + fieldName, "integer", packageName);
                        if (resId <= 0) continue; // Continue with another prefix

                        value = res.getInteger(resId);
                    }
                    else {
                        resId = res.getIdentifier(prefix + fieldName, "string", packageName);
                        if (resId <= 0) continue; // Continue with another prefix

                        value = res.getString(resId);
                    }
                    break;
                }

                if (value != null) {
                    field.set(object, value);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}