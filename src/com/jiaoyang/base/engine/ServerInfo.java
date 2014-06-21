package com.jiaoyang.base.engine;

public class ServerInfo {
	public final static int LANVIDEO_START_SEARCH_SERVER = 200;
	public final static int LANVIDEO_STOP_SEARCH_SERVER = LANVIDEO_START_SEARCH_SERVER + 1;
	public final static int LANVIDEO_RESTART_SEARCH_SERVER = LANVIDEO_STOP_SEARCH_SERVER + 1;
	public final static int LANVIDEO_FIND_SERVER_NOTIFY = LANVIDEO_RESTART_SEARCH_SERVER +1;
	public final static int LANVIDEO_FINISH_FIND_SERVER_NOTIFY = LANVIDEO_FIND_SERVER_NOTIFY + 1;
	
	public final static int LANVIDEO_HTTP_GET_FILE_LIST_XML = LANVIDEO_FINISH_FIND_SERVER_NOTIFY + 1;
	public final static int LANVIDEO_GET_THUMBNAIL = LANVIDEO_HTTP_GET_FILE_LIST_XML + 1;
	public final static int LANVIDEO_GET_FILE_NOTIFY = LANVIDEO_GET_THUMBNAIL + 1;
	public final static int LANVIDEO_LOAD_FILE_LIST_XML = LANVIDEO_GET_FILE_NOTIFY + 1;
	public final static int LANVIDEO_DOWNLOAD_FILE = LANVIDEO_LOAD_FILE_LIST_XML + 1;
	public final static int LANVIDEO_HTTP_GET_FILE = LANVIDEO_DOWNLOAD_FILE + 1;
	
	
	public int server_type;		//0,HTTP;1,FTP(暂不支持，以后扩展用)
	public int ip;
	public int port;
	public String description;
	public int mIsNeedPassword;		//是否需要密码,1为需要，0为不需要
	public int mFileNum;
	public String mIpStr;	
	public String mServerName;
	public String mPassword = "0";
}
