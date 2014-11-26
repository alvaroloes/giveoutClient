package com.capstone.potlatch.net.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Especial thanks to: https://github.com/smanikandan14/Volley-demo/blob/master/src/com/mani/volleydemo/toolbox/MultiPartRequest.java
 */

public class AuthMultiPartRequest<T> extends AuthRequest<T> {
    private HttpEntity cachedEntity = null;

	/* To hold the parameter name and the File to upload */
    private Map<String,File> fileUploads = new HashMap<>();
	
	/* To hold the parameter name and the string content to upload */
	private Map<String,String> stringUploads = new HashMap<>();

    public AuthMultiPartRequest(int method, String url, TypeReference<T> resultTypeReference, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, resultTypeReference, listener, errorListener);
    }

    public AuthMultiPartRequest(int method, String url, JavaType resultJavaType, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, resultJavaType, listener, errorListener);
    }

    public AuthMultiPartRequest(int method, String url, Class<T> resultClass, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, resultClass, listener, errorListener);
    }


    public void addFileUpload(String param,File file) {
    	fileUploads.put(param,file);
    }
    
    public void addStringUpload(String param,String content) {
    	stringUploads.put(param,content);
    }
    
    public Map<String,File> getFileUploads() {
    	return fileUploads;
    }
    
    public Map<String,String> getStringUploads() {
    	return stringUploads;
    }

    @Override
    public String getBodyContentType() {
        return cachedEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            cachedEntity = null;
            getMultipartEntity().writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException in MultiPartRequest.getBody writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    protected HttpEntity getMultipartEntity() {
        if (cachedEntity != null) {
            return cachedEntity;
        }

        MultipartEntityBuilder multiEntityBuilder = MultipartEntityBuilder.create();
        multiEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        //Iterate the fileUploads
        for (Map.Entry<String, File> entry : getFileUploads().entrySet()) {
            File file = entry.getValue();
            multiEntityBuilder.addBinaryBody(entry.getKey(),
                                             file,
                                             ContentType.create(URLConnection.guessContentTypeFromName(file.getName())),
                                             file.getName());
        }

        //Iterate the stringUploads
        for (Map.Entry<String, String> entry : getStringUploads().entrySet()) {
            multiEntityBuilder.addTextBody(entry.getKey(), entry.getValue());
        }

        cachedEntity = multiEntityBuilder.build();
        return cachedEntity;
    }
}