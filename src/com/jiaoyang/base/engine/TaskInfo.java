package com.jiaoyang.base.engine;



public class TaskInfo {
	/*
	 * 任务状态值组
	 */
	public static final int WAITING = 0;
	public static final int RUNNING = WAITING+1;
	public static final int PAUSED = RUNNING+1;
	public static final int SUCCESS = PAUSED+1;
	public static final int FAILED = SUCCESS+1;
	public static final int DELETED = FAILED+1;
	
	public static final int READY = DELETED+1;
	
	public static final int DELETE_TASK = READY+1;
	
	public static final int UPDATE_ALL_TASK = 10000;
	public static final int UPDATE_SINGLE_TASK = 10001;
	public static final int ADD_TASK_SUCCESS = 100;
	public static final int ADD_TASK_FAILED = 101;
	public static final int GET_TASKINFO_SUCCESS = 102;
	public static final int GET_TASKINFO_FAILED = 103;
	public static final int PAUSE_TASK_SUCCESS = 104;
	public static final int PAUSE_TASK_FAILED = 105;
	public static final int RESUME_TASK_SUCCESS = 106;
	public static final int RESUME_TASK_FAILED = 107;
	public static final int TASK_STATE_CHANGED_NOTIFY = 108;
	public static final int TASK_ALREADY_EXIST = 102409;
	public static final int FILE_ALERADY_EXIST = 102416;
	public static final int INVALID_TASK_ID = 102434;
	
	public static final int INVALID_FILE_NAME = 102444; //例如文件名超过255个字符，包含非法字符等
	
	public static final int INSUFFICIENT_DISK_SPACE = 3173;
	public static final int EACCES_PERMISSION_DENIED= 13;	//Linux错误码，许可拒绝
	
	
	//public static final int	 FILE_TYPE_FLV = 0;
	//public static final int  FILE_TYPE_MP4 = FILE_TYPE_FLV + 1;
	
	public static final int ETT_URL = 0;
	public static final int ETT_LAN = 6;
	/*
	 * 任务ID
	 */
	public int mTaskId;
	
	public int mRowId;
	
	/*
	 * 任务状态
	 */
	public int mTaskState;	
	
	/*
	 * 任务相应的文件名
	 */
	public String mFileName;
	
	/*
	 * 已下载文件大小
	 */
	public long downloadedSize;	
	
	/*
	 * 任务相应的文件大小
	 */
	public long fileSize;
	/*
	 * 实时下载速度
	 */
	public int mDownloadSpeed;
	/*
	 * 下载库一些耗时操作是否正在进行标识
	 */
	public boolean mIsOperating;
	
	public String mUrl;
	
	//任务类型
	public int mType;
	
	//文件cid
	public String mCid;
	
	//文件类型如flv、 mp4等 , 0 flv , 1 mp4
	public int	mFileType;
	
	//bt任务参数
	public String mSeedFileFullPath;
		
	//public int mSeedFilePathLength;
		
	public String mDownloadFileIndexArray;//int[] 串化 比如 “1235”
	
	public String mFilePath;
	
	/*
	 * 下载类型
	 */
	public int mTaskType;
	
	/*
	 * 任务相应的文件大小
	 */
	public long mFileSize;
}
