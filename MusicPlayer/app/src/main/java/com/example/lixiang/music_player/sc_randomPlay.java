package com.example.lixiang.music_player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class sc_randomPlay extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            //随机播放
            MyApplication.setPlayMode(1);
            //打开服务播放
            Intent intent = new Intent(this, PlayService.class);
            intent.putExtra("ACTION", MyConstant.sc_playAction);
            startService(intent);
            finish();
    }
}
