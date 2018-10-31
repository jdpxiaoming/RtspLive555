package com.rtsp.poe.rtsplive555;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.easydarwin.blogdemos.H264Decoder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 播放h264数据工具类.
 */
public class VideoUtils {
    private static final String TAG = "VideoUtils";

    public static class LocalShow {
        private static boolean putcamdataflag = true;
        private static H264Decoder sH264Decoder;
        private static SurfaceHolder.Callback callback;
        private static Thread camThread;
        private static boolean isRunning = false;

        public static void addView(final SurfaceView surfaceView) {
            if (surfaceView == null) {
                return;
            }

            isRunning = true;
            if (camThread == null) {
                camThread = new CamThread();
                camThread.start();
            }

            // 新增
            callback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (sH264Decoder != null) {
                            sH264Decoder.DecoderClose();
                            sH264Decoder = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (holder != null) {
                        sH264Decoder = new H264Decoder(holder, "video/avc", 1024, 576, 25);
                    } else {
                        return;
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            };

            surfaceView.getHolder().addCallback(callback);

            putcamdataflag = true;
        }

        public static void removeView() {
            if (sH264Decoder != null) {
                sH264Decoder.DecoderClose();
                sH264Decoder = null;
            }
            callback = null;

            putcamdataflag = false;

            isRunning = false;
            if (camThread != null) {
                camThread.interrupt();
                camThread = null;
            }
        }

        private static void putcamdata(byte[] dataframe) {
            if (!putcamdataflag) {
                return;
            }

            try {
                h264dataQueue.offer(dataframe);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        // 视频保存线程初始化
        private static class CamThread extends Thread {
            @Override
            public void run() {
                super.run();
                while (isRunning) {
                    try {
                        c = h264dataQueue.poll();
                        if (c == null) {
                            Thread.sleep(50);
                            continue;
                        }
                        if (sH264Decoder != null) {
                            if (putcamdataflag && !sH264Decoder.onFrame(c, 0, c.length)) {
                                Thread.sleep(1);
                            }
                        }
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                }
                h264dataQueue.clear();
                c = null;
            }
        }

        static {
            h264dataQueue = new ArrayBlockingQueue<byte[]>(1000);
        }

        private static BlockingQueue<byte[]> h264dataQueue;
        private static byte[] c;
    }

    private static byte[] mPpsSps = new byte[0];

    public static void makeSpsPps(byte[] outData) {
        Log.i(TAG,"makeSpsPps: "+ outData);
        // 记录pps和sps
        if ((outData[0] == 0 && outData[1] == 0 && outData[2] == 1 && (outData[3] & 0x1f) == 7) || (outData[0] == 0
                && outData[1] == 0 && outData[2] == 0 && outData[3] == 1 && (outData[4] & 0x1f) == 7)) {
            mPpsSps = outData;
        } else if ((outData[0] == 0 && outData[1] == 0 && outData[2] == 1 && (outData[3] & 0x1f) == 5)
                || (outData[0] == 0 && outData[1] == 0 && outData[2] == 0 && outData[3] == 1
                && (outData[4] & 0x1f) == 5)) {
            // 在关键帧前面加上pps和sps数据
            byte[] data = new byte[mPpsSps.length + outData.length];
            System.arraycopy(mPpsSps, 0, data, 0, mPpsSps.length);
            System.arraycopy(outData, 0, data, mPpsSps.length, outData.length);
            outData = data;
        }

        // 将数据分发到本地
        LocalShow.putcamdata(outData);
    }
}