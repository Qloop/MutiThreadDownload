package com.ltz.download;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ltz.download.service.DownLoadService;
import com.ltz.download.domain.FileInfo;
import com.ltz.download.utils.NotificationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Explorer on 2016/6/4.
 */
public class MainActivity extends Activity {

//    private Button mStart;
//    private Button mPause;
//    private ProgressBar mProgress;
//    private TextView mFileName;

    private ListView mLvFile;
    private List<FileInfo> mFileList;
    private DowmloadListAdapter mAdapter;
    private NotificationUtils mNotificationUtils;
    private Messenger mServiceMessager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initViews();
    }

    private void initViews() {
//        mFileName = (TextView) findViewById(R.id.tv_name);
//        mProgress = (ProgressBar) findViewById(R.id.pb_download_progress);
//        mStart = (Button) findViewById(R.id.btn_start);
//        mPause = (Button) findViewById(R.id.btn_pause);
//
//        mProgress.setMax(100);
//        final FileInfo fileInfo = new FileInfo(0, "http://dldir1.qq.com/qqfile/qq/QQ8.3/18038/QQ8.3.exe", "myQQ", 0, 0);
//        mFileName.setText(fileInfo.getFileName());
//
//        mStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, DownLoadService.class);
//                intent.setAction(DownLoadService.ACTION_START);
//                intent.putExtra("fileinfo", fileInfo);
//                startService(intent);
//            }
//        });
//        mPause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, DownLoadService.class);
//                intent.setAction(DownLoadService.ACTION_STOP);
//                intent.putExtra("fileinfo", fileInfo);
//                startService(intent);
//            }
//        });

        mLvFile = (ListView) findViewById(R.id.lv_download);
        mFileList = new ArrayList<>();
        mNotificationUtils = new NotificationUtils(this);
        FileInfo fileInfo1 = new FileInfo(0, "http://dldir1.qq.com/qqfile/qq/QQ8.3/18038/QQ8.3.exe", "myQQ", 0, 0);
        FileInfo fileInfo2 = new FileInfo(1, "http://www.imooc.com/mobile/imooc.apk", "imooc", 0, 0);
        FileInfo fileInfo3 = new FileInfo(2, "http://dldir1.qq.com/qqfile/qq/QQ8.3/18038/QQ8.3.exe", "myQQ", 0, 0);
        FileInfo fileInfo4 = new FileInfo(3, "http://www.imooc.com/mobile/imooc.apk", "imooc", 0, 0);

        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        mFileList.add(fileInfo4);

        mAdapter = new DowmloadListAdapter(this, mFileList);
        mLvFile.setAdapter(mAdapter);
        //注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownLoadService.ACTION_UPDATE);
        intentFilter.addAction(DownLoadService.ACTION_FINISHED);
        intentFilter.addAction(DownLoadService.ACTION_START);
        registerReceiver(mReceiver, intentFilter);

        Intent intent = new Intent(this,DownLoadService.class);
        bindService(intent,mConnection, Service.BIND_AUTO_CREATE);
    }


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //获得service中的manager
            mServiceMessager = new Messenger(binder);
            //创建Activity中Messager
            Messenger messenger = new Messenger(mHandler);
            //创建消息
            Message msg = new Message();
            msg.what = DownLoadService.MSG_BIND;
            msg.replyTo = messenger;
            //使用service中的Messager发送Activity中的Messager
            try {
                mServiceMessager.send(msg);  //在Service中就可以接收到Activity中的Massager  用于Service与Activity之间的双向通信
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    Handler mHandler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownLoadService.ACTION_UPDATE.equals(intent.getAction())) { //更新进度
                int finished = intent.getIntExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
//                mProgress.setProgress(finished);
                mAdapter.updateProgress(id, finished);
                //更新通知的进度
                Log.i("update ","to update----------------------------");
                mNotificationUtils.updateNotification(id, finished);
            } else if (DownLoadService.ACTION_FINISHED.equals(intent.getAction())) {
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                //下载结束后更新进度为0
                mAdapter.updateProgress(fileInfo.getId(), 0);
                Toast.makeText(MainActivity.this, "下载结束", Toast.LENGTH_SHORT).show();
                mNotificationUtils.cancelNotificaton(fileInfo.getId());//取消通知
            } else if (DownLoadService.ACTION_START.equals(intent.getAction())) {
                Log.i("receiver","hasReceived------------------------");
                mNotificationUtils.showNotification((FileInfo) intent.getSerializableExtra("fileInfo"));
            }
        }
    };
}
