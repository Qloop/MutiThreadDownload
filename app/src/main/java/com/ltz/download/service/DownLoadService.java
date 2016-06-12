package com.ltz.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ltz.download.domain.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Explorer on 2016/6/4.
 */
public class DownLoadService extends Service {

    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_FINISHED = "finished";
    public static final int MSG_INIT = 0;
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() + "/downloads/"
            : Environment.getDownloadCacheDirectory() + "/download/";

    //下载任务的集合
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileinfo = (FileInfo) intent.getSerializableExtra("fileinfo");
            Log.i("fileinfo", "start is" + fileinfo.toString());
            InitThread initThread = new InitThread(fileinfo);
            DownloadTask.sExecutor.execute(initThread);
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileinfo");
            Log.i("fileinfo", "stop is " + fileInfo.toString());

            //暂停下载
            DownloadTask task = mTasks.get(fileInfo.getId());
            if (task != null) {
                task.isPause = true;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    DownloadTask task = new DownloadTask(DownLoadService.this, fileInfo, 3);
                    task.download();
                    //把下载任务添加到集合中
                    mTasks.put(fileInfo.getId(), task);
                    break;
            }

        }
    };

    public class InitThread extends Thread {
        private FileInfo mFileInfo;

        public InitThread(FileInfo fileInfo) {
            mFileInfo = fileInfo;
        }

        @Override
        public void run() {
            Log.i("flag", mFileInfo.toString());
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(mFileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30000);
                conn.setRequestMethod("GET");
                int length = -1;
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    length = conn.getContentLength();
                }
                if (length < 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();   //sendToTarget还是使用的sendMessage()
//				Message message = mHandler.obtainMessage();
//				message.obj = length;
//				message.what = MSG_INIT;
//				mHandler.sendMessage(message);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null && raf != null) {
                    try {
                        raf.close();
                        conn.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
