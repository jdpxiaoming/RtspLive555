package com.rtsp.poe.rtsplive555

import android.Manifest
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.rtsp.poe.rtsplive555.RTSPClient.RTSPVideoListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    /**
     * 申请权限
     */
    var REQUEST_CODE = 100
    var localFile:File? = null
    var fos:FileOutputStream? = null
    var isrunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        VideoUtils.LocalShow.addView(surface_view);
        btn_play.setOnClickListener {
            Thread{
                localFile = File(Environment.getExternalStorageDirectory().toString()+"/20181031.h264")
                fos = FileOutputStream(localFile)
                isrunning = true
                RTSPClient.Play("${et_url.text}"/*"rtsp://10.5.225.36:8554/sss"*/)
            }.start()
        }

        ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Log.i("poe","h264 file path : "+localFile?.absolutePath)
        btn_stop.setOnClickListener {
            stopClient()
        }

//        val videoListener = RTSPVideoListener { data, len ->  }
        RTSPClient.setRTSPVideoListener(object :RTSPVideoListener{
            override fun videoCallBack(data: ByteArray?, len: Int) {
                Log.i("poe","videoCallBack")
                /*if (localFile != null && fos != null && isrunning) {
                    try {
                        fos?.write(data, 0, len)
                        fos?.flush()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }*/
                VideoUtils.makeSpsPps(data)
            }
        })

        //申请文件读写权限
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ,REQUEST_CODE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopClient()
        VideoUtils.LocalShow.removeView();
    }

    private fun stopClient(){
//        Thread{
        isrunning = false
        fos?.close()
        RTSPClient.Stop()
//        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
             REQUEST_CODE -> {
                //以获取到读写文件的授权
                 Toast.makeText(this,"授权成功！",Toast.LENGTH_SHORT).show()
             }
        }
    }
}
