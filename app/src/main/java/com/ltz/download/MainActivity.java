package com.ltz.download;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ltz.download.service.DownLoadService;
import com.ltz.download.domain.FileInfo;

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
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownLoadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
//                mProgress.setProgress(finished);
                mAdapter.updateProgress(id, finished);
            } else if (DownLoadService.ACTION_FINISHED.equals(intent.getAction())) {
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                //下载结束后更新进度为0
                mAdapter.updateProgress(fileInfo.getId(), 0);
                Toast.makeText(MainActivity.this, "下载结束", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
