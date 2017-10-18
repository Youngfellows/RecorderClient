package com.byron.recorderclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.byron.recorderclient.interfaces.RecordAudioCallback;
import com.byron.recorderclient.interfaces.SendAudioCallback;
import com.byron.recorderclient.service.RecorderService;
import com.byron.recorderclient.utils.StringValidation;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private String TAG = this.getClass().getSimpleName();
    private TextView mTvIp;
    private Button mBtnSendVoice;
    private EditText mEdtIpAddress;
    private EditText mEdtPort;
    //private String mRecorderMessage = null;//录音数据
    private RecorderService.RecorderBinder mRecorderBinder;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "---------服务绑定成功了-----------");
            mRecorderBinder = (RecorderService.RecorderBinder) iBinder;
            showIpInfo();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "---------服务解绑了-----------");
            mRecorderBinder = null;
        }
    };

    private void showIpInfo() {
        if (mRecorderBinder != null) {
            final String ipAddress = mRecorderBinder.getLocalIPAddress(); //获取本机ip
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvIp.setText(ipAddress);//显示本机IP
                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mTvIp = findView(R.id.tv_ip);//显示本机IP
        mBtnSendVoice = findView(R.id.btn_send_voice);//发送音频
        mEdtIpAddress = findView(R.id.edt_tv_pi);//TV IP地址
        mEdtPort = findView(R.id.edt_tv_port);//TV 端口号
    }

    private void initData() {
        bindService();
    }

    private void bindService() {
        Intent intent = new Intent(this, RecorderService.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void initListener() {
        mBtnSendVoice.setOnTouchListener(this);
    }

    private <T extends View> T findView(int viewId) {
        return (T) findViewById(viewId);
    }

    /**
     * 发送录制完成的音频
     */
    private void sendAudio(String voiceMessage) {
        String ip = mEdtIpAddress.getText().toString();
        String port = mEdtPort.getText().toString();
        // TODO: 2017/10/17 开启录音机进行录音
        boolean validation = StringValidation.checkValidation(ip, port);//检查IP或端口是否合法
        if (!validation) {
            Toast.makeText(this, "请检查ip和端口设置是否正确!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: 2017/10/18 发送音频
        if (mRecorderBinder != null) {
            mRecorderBinder.sendAudio(ip, port, voiceMessage, new SendAudioCallback() {
                @Override
                public void sendResult(boolean isSuccess) {
                    if (isSuccess) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "发送数据成功", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "发送数据失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "按钮按下逻辑");
                // TODO: 2017/10/17 开启录音机，获取录音，并一段一段的传递过去
                recordAudioAndSend();
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "按钮弹起逻辑1");
                // TODO: 2017/10/18 停止录音+传输数据
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, "按钮弹起逻辑2");
                // TODO: 2017/10/18 停止录音+传输数据
                break;
        }
        return false;
    }

    /**
     * 开启录音机获取录音数据并传输
     */
    private void recordAudioAndSend() {
        if (mRecorderBinder != null) {
            mRecorderBinder.recordAudio(new RecordAudioCallback() {
                @Override
                public void recordAudio(String audioMessage) {
                    // TODO: 2017/10/18 发送录音之后送过来的一段段录音数据
                    Log.i(TAG, "---录制完成的音频: " + audioMessage);
                    sendAudio(audioMessage);
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
