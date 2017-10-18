package com.byron.recorderclient.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Jie.Chen on 2017/10/17.
 */

public class SocketUtil {
    private static SocketUtil instance;
    private String TAG = this.getClass().getSimpleName();

    private SocketUtil() {
    }

    public static SocketUtil getInstance() {
        if (instance == null) {
            synchronized (SocketUtil.class) {
                if (instance == null) {
                    instance = new SocketUtil();
                }
            }
        }
        return instance;
    }

    /**
     * @param ipAddress ip地址
     * @param port      端口号
     * @return 发送是否成功
     */
    public boolean sendVideo(String ipAddress, String port, String message) {
        boolean isSuccess = false;//发送是否成功
        Socket socket = new Socket();
        try {
            socket.setKeepAlive(true);
            socket.setSoTimeout(30000);
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(ipAddress, Integer.parseInt(port)));

            OutputStream out = socket.getOutputStream();//发送TCP请求
            out.write(message.getBytes("UTF-8"));

            isSuccess = true;

            InputStream in = socket.getInputStream();//接收TCP相应
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                bytesOut.write(buffer, 0, len);
            }
            Log.i(TAG, "收到服务器的应答=[" + bytesOut.toString("UTF-8") + "]");

        } catch (IOException e) {
            Log.e(TAG, "请求通信[" + ipAddress + ":" + port + "]时偶遇异常，堆栈轨迹如下：");
            Log.e(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }


}
