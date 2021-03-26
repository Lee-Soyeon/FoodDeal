package com.hankki.fooddeal.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/*
어플리케이션 모두 지우기 기능으로 어플리케이션 종료 시, onDestroy가 작동되지 않기 때문에 이를 탐지하는 서비스
*/
public class ForcedTerminationService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("Error","onTaskRemoved - 강제 종료 " + rootIntent);
        stopSelf();
    }
}
