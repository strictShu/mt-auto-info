package com.hx.mt;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;


import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MyApp extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, final Throwable e) {
                if (t == Looper.getMainLooper().getThread()) {
                    Toast.makeText(MyApp.this, "主线程出现错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    while (true) {
                        try {
                            Looper.loop();
                        } catch (Throwable e1) {
                            Toast.makeText(MyApp.this, "主线程出现错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyApp.this, "子线程出现错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    public static Context getContext() {
        return context;
    }
}
