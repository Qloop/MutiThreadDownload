package com.ltz.download;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ltz.download.domain.FileInfo;
import com.ltz.download.service.DownLoadService;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Qloop on 2016/6/12.
 */
public class DowmloadListAdapter extends BaseAdapter {
    private Context mContext;
    private List<FileInfo> mFileInfo;

    public DowmloadListAdapter(Context mContext, List<FileInfo> mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
    }

    @Override
    public int getCount() {
        return mFileInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FileInfo fileInfo = mFileInfo.get(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_download_listview, null);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            holder.pbProgress = (ProgressBar) convertView.findViewById(R.id.pb_download_progress);
            holder.btnStart = (Button) convertView.findViewById(R.id.btn_start);
            holder.btnStop = (Button) convertView.findViewById(R.id.btn_pause);

            holder.tvName.setText(fileInfo.getFileName());
            holder.pbProgress.setMax(100);
            holder.btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DownLoadService.class);
                    intent.setAction(DownLoadService.ACTION_START);
                    intent.putExtra("fileinfo", fileInfo);
                    mContext.startService(intent);
                }
            });
            holder.btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DownLoadService.class);
                    intent.setAction(DownLoadService.ACTION_STOP);
                    intent.putExtra("fileinfo", fileInfo);
                    mContext.startService(intent);
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //填充数据
        holder.pbProgress.setProgress(fileInfo.getFinished());
        return convertView;
    }

    /**
     * 更新下载进度
     *
     * @param id
     * @param progress
     */
    public void updateProgress(int id, int progress) {
        FileInfo fileInfo = mFileInfo.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView tvName;
        ProgressBar pbProgress;
        Button btnStart;
        Button btnStop;
    }
}
