package com.ltz.download.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.ltz.download.MainActivity;
import com.ltz.download.R;
import com.ltz.download.domain.FileInfo;
import com.ltz.download.service.DownLoadService;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知工具类
 * Created by Qloop on 2016/6/13.
 */
public class NotificationUtils {

    private NotificationManager mNotificationManger = null;
    private Map<Integer, Notification> mNotifications = null;
    private Context mContext;

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
        mNotifications = new HashMap<>();
        mNotificationManger = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showNotification(FileInfo fileInfo) {
        //如果没有显示该通知
        if (!mNotifications.containsKey(fileInfo.getId())) {
            Notification notification = new Notification();
            notification.tickerText = fileInfo.getFileName() + "正在下载...";
            notification.when = System.currentTimeMillis();
            notification.icon = R.mipmap.ic_launcher;
            notification.flags = Notification.FLAG_AUTO_CANCEL;//点击后自动消失
            //点击通知栏的操作
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pintent = PendingIntent.getActivity(mContext, 0, intent, 0);
            notification.contentIntent = pintent;
            //创建RemoteView  (通知显示视图)
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_item);
            //设置开始按钮操作
            Intent intentStart = new Intent(mContext, DownLoadService.class);
            intentStart.setAction(DownLoadService.ACTION_START);
            PendingIntent piStart = PendingIntent.getService(mContext, 0, intentStart, 0);
            remoteViews.setOnClickPendingIntent(R.id.btn_notification_start, piStart);
            //设置停止按钮操作
            Intent intentStop = new Intent(mContext, DownLoadService.class);
            intentStop.setAction(DownLoadService.ACTION_STOP);
            PendingIntent piStop = PendingIntent.getService(mContext, 0, intentStart, 0);
            remoteViews.setOnClickPendingIntent(R.id.btn_notification_stop, piStop);
            //设置下载的文件名
            remoteViews.setTextViewText(R.id.tv_name, fileInfo.getFileName());

            notification.contentView = remoteViews;

            //发送通知
            mNotificationManger.notify(fileInfo.getId(), notification);
            Log.i("Notification","show=========================");
            //将该通知添加进集合中 便于进行管理
            mNotifications.put(fileInfo.getId(), notification);
        }
    }

    /**
     * 取消通知
     *
     * @param id
     */
    public void cancelNotificaton(int id) {
        mNotificationManger.cancel(id);
        mNotifications.remove(id);
    }

    /**
     * 更新通知的进度
     *
     * @param id
     * @param progress
     */
    public void updateNotification(int id, int progress) {
        Notification notification = mNotifications.get(id);
        if (notification != null) {
            Log.i("update ","update----------------------------");
            notification.contentView.setProgressBar(R.id.pb_download_progress, 100, progress, false);
            mNotificationManger.notify(id, notification);
        }
    }

}
