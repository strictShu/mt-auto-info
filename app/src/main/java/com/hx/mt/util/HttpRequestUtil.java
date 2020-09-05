package com.hx.mt.util;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hx.mt.common.ProcessStatus;
import com.hx.mt.common.ShopInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Arrays;
import java.util.List;

import okhttp3.Call;

public class HttpRequestUtil {
    //    http://erp.shandongyunpin.com:8099/GetADData.aspx
    //   http://erp.shandongyunpin.com:8099/GetSHData.aspx
    //    http://erp.shandongyunpin.com:8099/AddData.aspx?sname=哈哈哈&sprice=4.6&sad=山东省济南市历下区&sphone=1876719781/111677&ssells=326&areaid=2&adc=历下区
    //?sname=test&sprice=4.6&sad=山东省济南市历下区&sphone=1876719781/111677&ssells=326&areaid=2&adc=历下区
    String inserUrl = "http://erp.shandongyunpin.com:8099/AddData.aspx";
    String getAdrUrl = "http://erp.shandongyunpin.com:8099/GetADData.aspx";
    String allSaveAddr = "http://erp.shandongyunpin.com:8099/GetSHData.aspx";
    private OnResultCallback onResultCallback;

    public HttpRequestUtil(OnResultCallback resultCallback) {
        onResultCallback = resultCallback;
    }

    public void insertShopInfo(ShopInfo shopInfo) {
        OkHttpUtils
                .get()
                .url(inserUrl)
                .addParams("sname", shopInfo.getShopName())
                .addParams("ssells", shopInfo.getShopSales())
                .addParams("sprice", shopInfo.getShopStar())
                .addParams("sad", shopInfo.getShopAddress())
                .addParams("sphone", shopInfo.getShopPhone())
                // .addParams("areaid", shopInfo.getShopAreaId())
                .addParams("adc", shopInfo.getShopSearchAddress())
                .build()
                .execute(
                        new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e("process-", "上传数据error", e);
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.e("process-", "上传数据success" + response);
                            }
                        });
    }

    public void getAddress(final AccessibilityNodeInfo nodeInfo, final ProcessStatus processStatus) {
        OkHttpUtils
                .get()
                .url(getAdrUrl)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("process-", "获取地理位置error", e);
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        String[] split = response.split("\\|\\|");
                        Log.e("process-", "获取地理位置success:　" + split[0]  +" " + split[1] + " " + split[2]);
                        onResultCallback.getResult(split, nodeInfo, processStatus, null);
                    }
                });
    }

    public void getAllAddress() {
        OkHttpUtils
                .get()
                .url(allSaveAddr)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("process-", "获取全部商店名称 fail", e);
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        Log.e("process-", "获取全部商店名称 success");
                        String[] split = response.split("\\|\\|");
                        List<String> allArrdressList = Arrays.asList(split);
                        onResultCallback.getResult(null, null, null, allArrdressList);
                    }
                });
    }

    public interface OnResultCallback {
        void getResult(Object t, AccessibilityNodeInfo nodeInfo, ProcessStatus processStatus, List<String> allArrdressList);
    }

}
