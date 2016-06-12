package com.ltz.download.service;

import android.content.Context;
import android.content.Intent;

import com.ltz.download.db.ThreadDAO;
import com.ltz.download.db.ThreadDAOImpl;
import com.ltz.download.domain.FileInfo;
import com.ltz.download.domain.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 下载任务类
 * Created by Qloop on 2016/6/11.
 */
public class DownloadTask {
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mThreadDAO;
    private int finished = 0;
    public boolean isPause = false;

    private List<DownloadThread> mThreadList;
    private int mThreadCount;

    public static ExecutorService sExecutor = Executors.newCachedThreadPool();

    public DownloadTask(Context mContext, FileInfo mFileInfo, int mThreadCount) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        this.mThreadCount = mThreadCount;
        mThreadDAO = new ThreadDAOImpl(mContext);
    }

    public void download() {
        //从数据库获取下载进度信息
        List<ThreadInfo> threadInfos = mThreadDAO.getThread(mFileInfo.getUrl());
        if (threadInfos.size() == 0) {
            //获取每个线程下载的长度
            int length = mFileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), i * length, (i + 1) * length - 1, 0);
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(length);
                }
                threadInfos.add(threadInfo);
                //向数据库中插入线程信息
                mThreadDAO.insertThread(threadInfo);
            }
        }
        //启动多个线程进行下载
        mThreadList = new ArrayList<>();
        for (ThreadInfo threadInfo : threadInfos) {
            DownloadThread downloadThread = new DownloadThread(threadInfo);
//            downloadThread.start();
            sExecutor.execute(downloadThread);
            mThreadList.add(downloadThread);
        }
    }

    class DownloadThread extends Thread {
        private ThreadInfo threadInfo;
        public boolean isFinished = false;

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(threadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                //设置下载位置
                int start = threadInfo.getStart() + threadInfo.getFinished();
                //设置局部下载
                conn.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
                File file = new File(DownLoadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                Intent intent = new Intent(DownLoadService.ACTION_UPDATE);
                finished += threadInfo.getFinished();

                //开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    //读取数据
                    inputStream = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 7];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        //进度发送给Activity
                        finished += len; //整个文件的下载进度
                        threadInfo.setFinished(threadInfo.getFinished() + len);
                        if (System.currentTimeMillis() - time > 1000) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", finished * 100 / mFileInfo.getLength());
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }
                        //下载暂停时保存下载进度
                        if (isPause) {
                            mThreadDAO.updateThread(threadInfo.getUrl(), threadInfo.getId(), threadInfo.getFinished());
                            return;
                        }
                    }
                }

                isFinished = true;

                //检查是否所有的下载线程都执行完毕
                checkAllThreadFinished();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null && raf != null && inputStream != null) {
                    try {
                        conn.disconnect();
                        raf.close();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private synchronized void checkAllThreadFinished() {
        boolean allFinished = true;
        for (DownloadThread downloadThread : mThreadList) {
            if (!downloadThread.isFinished) {
                allFinished = false;
                break;
            }
        }
        //全部执行完毕之后发送广播更新UI
        if (allFinished) {
            Intent intent = new Intent(DownLoadService.ACTION_FINISHED);
            intent.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent);

            //下载完成之后删除线程信息
            mThreadDAO.deleteThread(mFileInfo.getUrl());
        }
    }
}
