package com.byron.recorderclient;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.byron.recorderclient.interfaces.RecordStartListener;
import com.byron.recorderclient.interfaces.RecordStreamListener;
import com.byron.recorderclient.interfaces.SendAudioCallback;
import com.byron.recorderclient.service.RecorderService;
import com.byron.recorderclient.utils.StringValidation;
import com.byron.recorderclient.utils.ThreadPoolUtil;

import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final int REQUEST_CODE = 0; // 请求码
    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static String TAG = "MainActivity";
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
    private void sendAudio(byte[] data, int begin, int end) {
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
            mRecorderBinder.sendAudio(ip, port, data, begin, end, new SendAudioCallback() {
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
                //检查是否有录音权限,开始录音
                chickPermission();
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "按钮弹起逻辑1");
                // TODO: 2017/10/18 停止录音+传输数据
                stopRecord();
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, "按钮弹起逻辑2");
                // TODO: 2017/10/18 停止录音+传输数据
                stopRecord();
                break;
        }
        return false;
    }


    /***
     * 1、查权限是否开启
     * 2、开启录音机获取录音数据并传输
     */
    private void chickPermission() {
        //检查录音权限说
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {//拒绝了权限，或者没有获得权限
            showTipsDialog("录音", Manifest.permission.RECORD_AUDIO, 10086);
        } else {
            //showToast("已经获得‘录音’权限");
            //检查是否开启读取SD卡的权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {//拒绝了权限，或者没有获得权限
                showTipsDialog("读存储卡", Manifest.permission.READ_EXTERNAL_STORAGE, 10010);
            } else {
                //showToast("已经获得‘读存储卡’权限");
                recordAudioAndSend();//开启录音机获取录音数据并传输
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10010:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //获取了权限
                    showToast("已经获得‘读存储卡’权限？？？");
                    recordAudioAndSend();//开启录音机获取录音数据并传输
                } else {
                    //拒绝了权限
                    showToast("拒绝获得‘读存储卡’权限");
                    getAppDetailSettingIntent("读存储卡");
                }
                break;
            case 10086:
                //录音默认询问，请求权限会弹窗
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //获取了权限
                    showToast("已经获得‘录音’权限？？？");
                    //检查是否开启读取SD卡的权限
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {//拒绝了权限，或者没有获得权限
                        showTipsDialog("读存储卡", Manifest.permission.READ_EXTERNAL_STORAGE, 10010);
                    } else {
                        //showToast("已经获得‘读存储卡’权限");
                        recordAudioAndSend();//开启录音机获取录音数据并传输
                    }
                } else {
                    //拒绝了权限
                    showToast("拒绝获得‘录音’权限");
                    getAppDetailSettingIntent("录音");
                }
                break;
        }
    }

    /**
     * 跳转到App详情页
     */
    public void getAppDetailSettingIntent(String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("权限申请提示");
        builder.setMessage("当前应用缺少" + name + "权限。请到应用信息——>权限管理中手动给予权限。");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent localIntent = new Intent();
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    localIntent.setAction(Intent.ACTION_VIEW);
                    localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
                }
                startActivity(localIntent);
            }
        });
        builder.create().show();
    }

    /**
     * 显示对话框，提示用户允许权限
     *
     * @param name
     */
    public void showTipsDialog(String name, final String permission, final int code) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("权限申请提示");
            builder.setMessage("当前应用缺少" + name + "权限。是否立即申请权限？");
            builder.setNegativeButton("取消", null);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            permission}, code);
                }
            });
            builder.create().show();
        } else {
            showToast("我们需要" + name + "权限，给我吧！");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    permission}, code);
        }
    }

    /**
     * 开启录音机获取录音数据并传输
     */
    private void recordAudioAndSend() {
        if (mRecorderBinder != null) {

            //模拟发送录音之后送过来的一段段录音数据
//            mRecorderBinder.recordAudioTest(new RecordAudioCallback() {
//                @Override
//                public void recordAudio(String audioMessage) {
//                    Log.i(TAG, "---录制完成的音频: " + audioMessage);
//                    sendAudio(audioMessage);
//                }
//            });

            // TODO: 2017/10/19 发送按下事件到tv端
            //sendString(mEdtIpAddress.getText().toString(), Integer.parseInt(mEdtPort.getText().toString()), "down");

            //真实的录音数据
            mRecorderBinder.recordAudio(new RecordStreamListener() {
                @Override
                public void recordOfByte(byte[] data, int begin, int end) {
                    //传递出来的一段段的录音数据
                    try {
//                        Log.e(TAG, "recordOfByte: " + new String(data, "UTF-8").toString());
                        Log.e("chenjie", "xxxxxxxxxxxxxxxYYYLSLAF size = " + (data.length) + " size2 = " + (end - begin));
                        sendAudio(data, begin, end);
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    /**
     * 停止录音
     */
    private void stopRecord() {
        if (mRecorderBinder != null) {
            mRecorderBinder.stopRecord(new RecordStartListener() {
                @Override
                public void isStartRecord(boolean isStart) {
                    if (!isStart) {
                        showToast("录音尚未开始");
                    }
                }
            });
        }
        //发送up事件
        //sendString(mEdtIpAddress.getText().toString(), Integer.parseInt(mEdtPort.getText().toString()), "up");

    }

    public static void sendString(final String ipAddress, final int port, final String string) {
        ThreadPoolUtil.getInstance().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ipAddress, port);
                    OutputStream os = socket.getOutputStream();
                    byte[] bytea = string.getBytes("UTF-8");
                    os.write(bytea);

//            PrintWriter pw = new PrintWriter(os);
//            pw.write(string);
//            pw.flush();
//            socket.shutdownOutput();

                    socket.close();
                    os.close();
                    Log.i(TAG, "是否按下: " + string + " lenght = " + bytea.length);
//            pw.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
