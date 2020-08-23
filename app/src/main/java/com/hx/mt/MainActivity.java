package com.hx.mt;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hx.mt.common.AccessibilityHelper;
import com.hx.mt.common.MtAppConst;
import com.hx.mt.common.ShopInfo;
import com.hx.mt.mt.R;
import com.hx.mt.util.HttpRequestUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_jump);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               openMeiTuan();
              /*  HttpRequestUtil httpRequestUtil = new HttpRequestUtil(null);
                ShopInfo shopInfo = new ShopInfo();
                shopInfo.setShopName("test");
                shopInfo.setShopSearchAddress("test");
                shopInfo.setShopPhone("123456");
                shopInfo.setShopAreaId("test");
                shopInfo.setShopSales("test");
                shopInfo.setShopStar("test");
                shopInfo.setShopAddress("test");
                httpRequestUtil.insertShopInfo(shopInfo);*/
            }
        });
    }

    private void openMeiTuan() {
        ComponentName name = new ComponentName(MtAppConst.Mt, MtAppConst.SplashScreenActivity);
        try {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(name);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "请安装大众点评", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean serviceRunning = AccessibilityHelper.checkPermission(this);
        if (!serviceRunning) {
            AccessibilityHelper.openAccessibilityServiceSettings(this);
        }
    }
}
