package com.byron.recorderclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.byron.recorderclient.interfaces.RecordAudioCallback;
import com.byron.recorderclient.interfaces.SendAudioCallback;
import com.byron.recorderclient.utils.IPUtil;
import com.byron.recorderclient.utils.ThreadPoolUtil;

/**
 * Created by Jie.Chen on 2017/10/18.
 */

public class RecorderService extends Service {
    private RecorderBinder mBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new RecorderBinder();
        }
        return mBinder;
    }

    public class RecorderBinder extends Binder {
        /**
         * 发送音频
         *
         * @param ip            服务端IP
         * @param port          端口
         * @param audioCallback 回调
         */
        public void sendAudio(String ip, String port, String audio, SendAudioCallback audioCallback) {
            ThreadPoolUtil.getInstance().sendAudio(ip, port, audio, audioCallback);
        }

        /**
         * 获取本机IP
         *
         * @return
         */
        public String getLocalIPAddress() {
            return IPUtil.getLocalIPAddress(true);
        }

        /**
         * 开启录音机录制音频
         */
        public void recordAudio(RecordAudioCallback callback) {
            ThreadPoolUtil.getInstance().startRecord(callback);
        }
    }
}
