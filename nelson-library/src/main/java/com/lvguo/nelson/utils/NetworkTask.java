package com.lvguo.nelson.utils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author nelson(燕鹏)
 */
public class NetworkTask {

    public static void get(String url, Map<String, Object> parms, final ResultCallback callback) {
        NetworkTask.get(url,parms,callback,null);
    }

    public static void get(String url, Map<String, Object> parms, final ResultCallback callback , Map<String,String> headers) {
        RequestParams params = new RequestParams(CV.BASE_URL + url);
        if(parms!=null){
            for (String key : parms.keySet()) {
                params.addParameter(key, parms.get(key));
            }
        }
        if(headers != null){
            for (String key : headers.keySet()) {
                params.addHeader(key, headers.get(key));
            }
        }
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callback.success(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                callback.failed();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {

            }
        });
    }
    public static void post(String url, Map<String, Object> parms, final ResultCallback callback) {
        NetworkTask.post(url,parms,callback,null);
    }

    public static void post(String url, Map<String, Object> parms, final ResultCallback callback, Map<String,String> headers) {
        RequestParams params = new RequestParams(CV.BASE_URL + url);
        if(parms!=null){
            for (String key : parms.keySet()) {
                params.addParameter(key, parms.get(key));
            }
        }
        if(headers != null){
            for (String key : headers.keySet()) {
                params.addHeader(key, headers.get(key));
            }
        }
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if(callback!=null){
                    callback.success(result);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                if(callback!=null){
                    callback.failed();
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {

            }
        });
    }

    public static void uplodFile(List<String> path, Map<String, Object> map, final ResultCallback callback, Map<String,String> headers) {
        RequestParams params = new RequestParams(CV.BASE_URL);
        params.setMultipart(true);
        if(map!=null){
            for (String key : map.keySet()) {
                params.addBodyParameter(key, map.get(key).toString());
            }
        }
        if(headers != null){
            for (String key : headers.keySet()) {
                params.addHeader(key, headers.get(key));
            }
        }
        for (int i = 0; i < path.size(); i++) {
            params.addBodyParameter("uploadfile" + i, new File(path.get(i)));
        }
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callback.success(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                callback.failed();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    public interface ResultCallback {
        void success(String result);
        void failed(String... args);
    }
}
