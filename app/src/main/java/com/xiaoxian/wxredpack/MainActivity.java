package com.xiaoxian.wxredpack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Switch swWx;
    TextView tvTime,tvOpenTime,tvDevice;

    /**
     * 等待红包弹出窗时间
     */
    private static final int MAX_WAIT_WINDOW_TIME=2000;


    //在MainActivity.onCreate里初始化
//    Intent upservice = new Intent(this, WXRedPackService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 打开设置辅助功能页面
         */
        swWx = findViewById(R.id.swWx);
        swWx.setOnClickListener((v) -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });

        tvTime=findViewById(R.id.tv_wait_time);
        tvTime.setText(WXRedPackService.waitWindowTime+"ms");

        findViewById(R.id.ll_wait_time).setOnClickListener(v->{
            if(WXRedPackService.needSetTime==0){
                Toast.makeText(MainActivity.this,"当前不可修改",Toast.LENGTH_SHORT).show();
                return;
            }
            if(WXRedPackService.waitWindowTime<MAX_WAIT_WINDOW_TIME/4){
                WXRedPackService.waitWindowTime=WXRedPackService.waitWindowTime+30;
            }else {
                WXRedPackService.waitWindowTime=0;
            }
            tvTime.setText(WXRedPackService.waitWindowTime+"ms");
        });

    }


    private void updateServiceStatus(boolean start){
       /* boolean bRunning = util.isServiceRunning(this, "com.nis.bcreceiver.NeNotificationService");

        //没有Running则启动
        if (start && !bRunning) {
            this.startService(upservice);
        } else if(!start && bRunning) {
            this.stopService(upservice);
        }*/

    }

}