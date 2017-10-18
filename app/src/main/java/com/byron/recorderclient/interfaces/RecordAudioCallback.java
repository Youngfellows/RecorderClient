package com.byron.recorderclient.interfaces;

/**
 * Created by Jie.Chen on 2017/10/17.
 */

public interface RecordAudioCallback {
    public void recordAudio(String audioMessage);//获取录音之转换的数据，可能要改成byte[]
}
