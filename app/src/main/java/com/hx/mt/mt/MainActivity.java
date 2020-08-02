package com.hx.mt.mt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_jump);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, InternetActivity.class);
//               // String url = "https://wallet.xiaoying.com/";
//                String url = "https://h5.waimai.meituan.com/";
//                intent.putExtra("url", url);
//                startActivity(intent);
            }
        });
    }

}
