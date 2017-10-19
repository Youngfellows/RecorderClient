package com.byron.recorderclient.interfaces;

/**
 * Created by Jie.Chen on 2017/10/19.
 * 获取录音的音频流,用于拓展的处理
 */
public interface RecordStreamListener {
    void recordOfByte(byte[] data, int begin, int end);
}
