package com.ltz.download.db;

import com.ltz.download.domain.ThreadInfo;

import java.util.List;

/**
 * 数据访问接口
 * Created by Explorer on 2016/6/8.
 */
public interface ThreadDAO {

	/**
	 * 插入线程信息
	 */
	void insertThread(ThreadInfo threadInfo);

	void deleteThread(String url);

	void updateThread(String url, int thread_id, int finished);

	List<ThreadInfo> getThread(String url);

	/**
	 * 判断是否存在
	 */
	boolean isExists(String url, int thread_id);
}
