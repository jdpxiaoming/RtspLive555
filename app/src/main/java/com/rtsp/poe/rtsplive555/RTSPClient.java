package com.rtsp.poe.rtsplive555;

import android.text.TextUtils;
import android.util.Log;

public class RTSPClient {

    private static final String TAG = "RTSPClient";
    private  static RTSPVideoListener mVideoListener;
    private  static RTSPInfoListener mInfoListener;
    static {
        System.loadLibrary("rtsp-lib");
    }

    private static native int start(String path);

    private static native void stop();


    public static void Play(String path){
        start(path);
    }

    public static void Stop(){
        stop();
    }

    public void onNativeCallBack(byte[] data, int len) {
        // 获取的数据回调，加入自己的逻辑
        Log.i(TAG,"len: "+len+" byte[]: "+data.toString());
        if(null != mVideoListener) mVideoListener.videoCallBack(data,len);
    }

    public  void onNativeInfo(String errorMsg) {
        if (TextUtils.isEmpty(errorMsg)) {
            return;
        }
        Log.d("RTSPClient", errorMsg);
    }


    public static void setRTSPVideoListener(RTSPVideoListener listener) {
        mVideoListener = listener;
    }

    public static void setRTSPInfoListener(RTSPInfoListener listener) {
        mInfoListener = listener;
    }

    public interface RTSPVideoListener {
        void videoCallBack(byte[] data, int len);
    }

    public interface RTSPInfoListener {
        void infoCallBack(String msg);
    }
}
