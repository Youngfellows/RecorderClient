package com.byron.recorderclient.utils;

import android.util.Log;

import com.byron.recorderclient.interfaces.RecordAudioCallback;
import com.byron.recorderclient.interfaces.RecordStartListener;
import com.byron.recorderclient.interfaces.RecordStreamListener;
import com.byron.recorderclient.interfaces.SendAudioCallback;
import com.byron.recorderclient.record.AudioRecorder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Jie.Chen on 2017/10/17.
 */

public class ThreadPoolUtil {
    private String TAG = this.getClass().getSimpleName();
    private ExecutorService mExecutor;
    AudioRecorder mAudioRecorder;

    private ThreadPoolUtil() {
        this.mExecutor = Executors.newCachedThreadPool();
        this.mAudioRecorder = AudioRecorder.getInstance();
    }

    private static ThreadPoolUtil instance;

    public static ThreadPoolUtil getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolUtil.class) {
                if (instance == null) {
                    instance = new ThreadPoolUtil();
                }
            }
        }
        return instance;
    }


    /**
     * 发送音频到TV端
     *
     * @param ip            tv IP地址
     * @param port          端口
     * @param voiceCallback 是否发送成功
     */
    public void sendAudio(final String ip, final String port, final byte[] data, final int begin, final int end, final SendAudioCallback voiceCallback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isSuccess = SocketUtil.getInstance().sendVideo(ip, port, data,begin,end);
                if (voiceCallback != null) {
                    voiceCallback.sendResult(isSuccess);
                }
            }
        });
    }

    /**
     * 模拟开启录音机获取录音
     *
     * @param callback 录音结果回调
     */
    public void startRecordTest(final RecordAudioCallback callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //TODO: 2017/10/17 模拟数据
                if (callback != null) {
                    callback.recordAudio(recordTest());
                }
            }
        });
    }

    /**
     * 模拟录音数据
     *
     * @return
     */
    private String recordTest() {
        StringBuilder reqData = new StringBuilder();
        for (int i = 0; i < 22; i++) {
            reqData.append("0003961000510110199201209222240000020120922000069347814303000700000813``中国联通交费充值`为号码18655228826交费充值100.00元`UDP1209222238312219411`10000```20120922`chinaunicom-payFeeOnline`UTF-8`20120922223831`MD5`20120922020103806276`1`02`10000`20120922223954`20120922`BOCO_B2C```http://192.168.20.2:5545/ecpay/pay/elecChnlFrontPayRspBackAction.action`1`立即支付,交易成功`");
        }
        //UTF-8编码的，上面的reqData循环后的字符串长度就是8712，再加上报文协议中表示完整报文长度的前六个字节就是8718
        reqData.insert(0, "008718");
        return reqData.toString();
    }


    /**
     * 1、 开启录音机获取录音
     * 2、录一段传一段出去
     *
     * @param listener 录音结果回调
     */
    public void startRecord(final RecordStreamListener listener) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                String fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                Log.i(TAG, "fileName: " + fileName);
                mAudioRecorder.createDefaultAudio(fileName);
                mAudioRecorder.startRecord(listener);
            }
        });
    }

    public ExecutorService getExecutor() {
        return mExecutor;
    }

    /**
     * 停止录音
     */
    public void stopRecord(RecordStartListener listener) {
        mAudioRecorder.stopRecord(listener);
    }
}
