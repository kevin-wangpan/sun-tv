package com.jiaoyang.base.misc;

public class JiaoyangConstants {

    public class PlayMode {
        public static final int PLAY_MODE_IVALID = -1;
        public static final int PLAY_MODE_WEB = PLAY_MODE_IVALID + 1;// 播放网络类型的视频
        public static final int PLAY_MODE_LOCAL = PLAY_MODE_WEB + 1;// 播放本地的视频
    }

    public class PlayProfile {
        public static final int LOW_PROFILE = 0;
        public static final int SMOOTH_PROFILE = LOW_PROFILE + 1;// 流畅 360P
        public static final int BASE_PROFILE = SMOOTH_PROFILE + 1;// 标清 480P
        public static final int HIGH_PROFILE = BASE_PROFILE + 1;// 高清 720P
        public static final int SUPER_PROFILE = HIGH_PROFILE + 1;// 超清 1080P
    }

    public static final int MOVIE_TYPE_DEFAULT_VALUE = 0xffff;

    public class PlayerPlayMode
    {
        public static final int LOCAL_XV_PLAY_MODE = -1;
        public static final int LOCAL_WIFI_PLAY_MODE = 0;
        public static final int LOCAL_DOWNLOAD_PLAY_MODE = LOCAL_WIFI_PLAY_MODE + 1;
        public static final int WEBVIEW_PLAY_MODE = LOCAL_DOWNLOAD_PLAY_MODE + 1;
        public static final int PLAY_RECORD_PLAY_MODE = WEBVIEW_PLAY_MODE + 1;
        public static final int XL_LAN_VIDEO_PLAY_MODE = PLAY_RECORD_PLAY_MODE + 1;

        public static final String FLVPLAYER_WIFIURL = "http://127.0.0.1:26002/localfile?fullpath=";
    }

    public class PlayerLoadInfo
    {
        public static final int LOAD_INFO_SUCCESS = 0;
        public static final int LOAD_INFO_FAILURE = LOAD_INFO_SUCCESS + 1;
    }

    public class PlayerNextVideo
    {
        public static final int HAS_NEXT_VIDEO = 0;
        public static final int DOESNOT_HAVE_NEXT_VIDEO = HAS_NEXT_VIDEO + 1;
    }

    public class PlayerUrlStatus
    {
        public static final int URL_STOP = -2;
        public static final int URL_WAIT = URL_STOP + 1;
        public static final int URL_GETTING = URL_WAIT + 1;
        public static final int URL_READY = URL_GETTING + 1;
    }

    public class PlayerPlayStatus
    {
        public static final int PLAYER_ERROR = -1;
        public static final int PLAYER_IDLE = PLAYER_ERROR + 1;
        public static final int PLAYER_INIT = PLAYER_IDLE + 1;
        public static final int PLAYER_PREPARE = PLAYER_INIT + 1;
        public static final int PLAYER_PREPARED = PLAYER_PREPARE + 1;
        public static final int PLAYER_PLAY = PLAYER_PREPARED + 1;
        public static final int PLAYER_PAUSE = PLAYER_PLAY + 1;
        // //new state
        public static final int PLAYER_BUFFERING = PLAYER_PAUSE + 1;
        public static final int PLAYER_COMPLETION = PLAYER_BUFFERING + 1;
    }

    public class VideoPlayerStatus
    {
        public static final int DEFAULT_VALUE = -100;
        public static final int PLAYER_UNINIT = 1;
        public static final int PLAYER_INIT = 2;
        // public static final int PLAYER_START = PLAYER_INIT + 1;
        public static final int PLAYER_OPENED = 3;
        public static final int PLAYER_PLAY = 4;
        public static final int PLAYER_PAUSE = 5;
        public static final int PLAYER_OPENING = 11;
        public static final int PLAYER_BUFFERIGN = 12;
        public static final int PLAYER_FIRST_PLAY = 13;
        public static final int PLAYER_CLOSING = 14;
        public static final int PLAYER_CLOSED = 16;
        public static final int PLAYER_COMPLETED = 0x0207;
        public static final int PLAYER_FIRST_BUFFERRING = 17;

        public static final int PLAYER_PAUSING = 100;
        public static final int PLAYER_INITING = PLAYER_PAUSING + 1;

        public static final int PLAYER_SWITCH = PLAYER_INITING + 1;
        public static final int PLAYER_UPDATE_PROGRESS = PLAYER_SWITCH + 1;
        public static final int PLAYER_NOTIFY_DURATION = PLAYER_UPDATE_PROGRESS + 1;
        public static final int PLAYER_SEEKTO = PLAYER_NOTIFY_DURATION + 1;
        public static final int PLAYER_SHOW_SCREEN_MODE = PLAYER_SEEKTO + 1;

        public static final int PLAYER_SHOW_CONTROL_BAR = 40;
        public static final int PLAYER_HIDE_CONTROL_BAR = PLAYER_SHOW_CONTROL_BAR + 1;

        public static final int PLAYER_SHOW_WAITING_BAR = PLAYER_HIDE_CONTROL_BAR + 1;
        public static final int PLAYER_HIDE_WAITING_BAR = PLAYER_SHOW_WAITING_BAR + 1;

        public static final int PLAYER_SHOW_MODE_BAR = PLAYER_HIDE_WAITING_BAR + 1;
        public static final int PLAYER_HIDE_MODE_BAR = PLAYER_SHOW_MODE_BAR + 1;

        public static final int PLAYER_SHOW_SCREEN_BAR = PLAYER_HIDE_MODE_BAR + 1;
        public static final int PLAYER_HIDE_SCREEN_BAR = PLAYER_SHOW_SCREEN_BAR + 1;

        public static final int PLAYER_SET_MOVIE_TITLE = PLAYER_HIDE_SCREEN_BAR + 1;
    }

    public class ScreenMode
    {
        public static final int ORIGIN_SIZE = 0;
        public static final int SUITABLE_SIZE = ORIGIN_SIZE + 1;
        public static final int FILL_SIZE = SUITABLE_SIZE + 1;
    }

    public class URLStatus
    {
        public static final int URL_STOP = -2;
        public static final int URL_WAIT = URL_STOP + 1;
        public static final int URL_GETTING = URL_WAIT + 1;
        public static final int URL_READY = URL_GETTING + 1;
    }

    public class PlayerClosedMode
    {
        public static final int PLAYER_CLOSED_TO_SWITCH_VIDEO = 0;
        public static final int PLAYER_CLOSED_TO_EIXT = PLAYER_CLOSED_TO_SWITCH_VIDEO + 1;
        public static final int PLAYER_CLOSED_TO_PAUSE = PLAYER_CLOSED_TO_EIXT + 1;
    }

    public class PlayerService
    {
        public static final int MSG_SERVICE_NOT_STARTED = -1;
        public static final int MSG_SERVICE_ALREADY_STARTED = MSG_SERVICE_NOT_STARTED + 1;
        public static final int MSG_SERVICE_FIRST_STARTED = MSG_SERVICE_ALREADY_STARTED + 1;

        public static final int MSG_WHAT_START_DOWNLOAD_ENGINE = 1000;
        public static final int MSG_WHAT_STOP_DOWNLOAD_ENGINE = MSG_WHAT_START_DOWNLOAD_ENGINE + 1;

    }

    public static class Time {
        public static final int IMAGEPOSTER_AUTO_PLAY_INTERVAL = 5000;
    }

    public static class IntentDataKey {
        /**
         * Composed with "/", for example: 频道/电影 、 首页/强档热播、搜索/昨日热搜影片
         */
        public static final String INTENT_KEY_FIRE_FROM = "fire_from";
        public static final String INTENT_KEY_MOVIE_DETAIL_URL = "movie_detail_url";
        public static final String INTENT_KEY_MOVIEINFO = "movie_info";
        public static final String INTENT_KEY_CHANNEL_TAB_INFO = "channel_tab_info";
        public static final String TITLE = "TITLE";
        public static final String URL = "URL";
        public static final String CURRENT_INDEX = "index";
        public static final String MOVIE_ID = "MOVIE_ID";
        public static final String PLAY_MODE = "PLAY_MODE";
        public static final String MOVIE_TYPE = "MOVIE_TYPE";
        public static final String fresh = "fresh";
        public static final String UPGRATE_STARTUP_NAME = "IS_STARTUP";
        public static final String FILE_NAME = "FILE_NAME";
        public static final String TASK_ID = "TASK_ID";
        public static final String INTENT_KEY_IP_TYPE = "intent_key_ip_type";
        public static final String PLAY_DEFINITION = "PLAY_DEFINITION";
        public static final String INTENT_KEY_FILE_SIZE = "file_size";
        public static final String INTENT_KEY_FILE_CID = "file_cid";
        public static final String INTENT_KEY_DOWNLOAD_URL = "download_url";
        public static final String INTENT_KEY_IS_DOWNLOAD_SUPPORTED = "is_download_supported";
        public static final String INTENT_KEY_PLAYER_TYPE = "player_type";
        public static final String INTENT_KEY_PLAY_MODE = "play_mode";
        public static final String INTENT_KEY_EPISODE_DETAIL = "episode_detail";
        public static final String INTENT_KEY_MOVIE_TYPE = "movie_type";
        public static final String INTENT_KEY_MOVIE_INFO_DETAIL = "movie_info_detail";
    }

    public class TaskErrorCode
    {
        public static final int ERROR_COED_SUCCESS = 0;
        public static final int ERROR_COED_WRONG_PARAM = ERROR_COED_SUCCESS + 1;
    }

    public class MemberLogin
    {
        public static final int MSG_MEMBER_LOGIN = 3000;
        public static final int ETM_LOGIN_LOGINED_EVENT = 0;
        public static final int ETM_LOGIN_FAILED_EVENT = ETM_LOGIN_LOGINED_EVENT + 1;
        public static final int ETM_UPDATE_PICTURE_EVENT = ETM_LOGIN_FAILED_EVENT + 1;
        public static final int ETM_KICK_OUT_EVENT = ETM_UPDATE_PICTURE_EVENT + 1;
        public static final int ETM_NEED_RELOGIN_EVENT = ETM_KICK_OUT_EVENT + 1;

        public class ErrorCode
        {
            public static final int MEMBER_PROTOCAL_ERROR_LOGIN_ACCOUNT_NOT_EXIST = 2;
            public static final int MEMBER_PROTOCAL_ERROR_LOGIN_PASSWORD_WRONG = 3;
            public static final int MEMBER_PROTOCAL_ERROR_LOGIN_SYSTEM_MAINTENANCE = 6;
            public static final int MEMBER_PROTOCAL_ERROR_LOGIN_ACCOUNT_LOCK = 7;
            public static final int MEMBER_PROTOCAL_ERROR_LOGIN_SERVER_INNER_ERROR = 8;
        }

        public class KeyValue
        {
            public static final String KEY_RESULT = "result";
            public static final String KEY_IS_VIP = "is_vip";
            public static final String KEY_VIP_RANK = "vip_rank";
            public static final String KEY_LEVEL = "level";
            public static final String KEY_USRNAME_IN_NUMBER = "username_in_number";
            public static final String KEY_EXPIRE_DATE = "expire_date";
            public static final String KEY_ERROR_INFO = "error_info";
        }
    }

    public class ChannelType {
        public static final String KEY_MOVIE = "m";
        public static final String KEY_TV = "te";
        public static final String KEY_ART = "t";
        public static final String KEY_ANI = "a";
        public static final String KEY_LISTEN_UP = "f";
        public static final String KEY_RECORD = "d";
        public static final String KEY_MTV = "mtv";
        public static final String KEY_VIDEO = "s";
        public static final String KEY_EDU = "edu";
        public static final String KEY_NEW = "x";
        public static final String KEY_SALON = "k";
        public static final String KEY_DEFAULT = "default";
    }

    public class Filter {
        public static final String AREA = "area";
        public static final String CATEGORY = "category";
        public static final String YEAR = "year";
    }

    public class PlayerLaunchWay {
        public static final int PLAYER_START_FROM_MIDDLE_PAGE = 0;
        public static final int PLAYER_START_FROM_FILE_EXPLORE = PLAYER_START_FROM_MIDDLE_PAGE + 1;
    }

    public static class Cache {
        public static final String IMAGE_CACHE_NAME = "Images";
        public static final int IMAGE_CACHE_SIZE_FACTOR = 4;
    }

    public static class FireClickFrom {
        public static final String HOME_PAGE = "首页";
        public static final String CHANNEL = "频道";
        public static final String RESOURCE = "淘片";
        public static final String LOCAL = "本地";
        public static final String MORE_PAGE = "更多";
        public static final String SEARCH = "搜索";
        public static final String SEARCH_CURRENT = "当前搜索";
        public static final String SEARCH_YESTERDAY_HOT = "昨日热搜";
        public static final String NOTIFIC = "通知栏";
    }

    public static class FileType {
        public static final int FILE_TYPE_FLV = 0;
        public static final int FILE_TYPE_MP4 = FILE_TYPE_FLV + 1;
    }

    public static class LAUNCH_TPEY {
        public static final String LAUNCH_FROM_WHERE = "launch_from_where";
        public static final String LAUNCH_FROM_NOTIFICATION = "launch_from_notification";
        public static final String LAUNCH_TO_MID_PAGE = "launch_to_middle_page";
    }

    public static class PlayerUIMessage {
        public static final int PLAYER_StartPlay = 0;
        public static final int PLAYER_UpdatePlayPosition = 1;
        public static final int PLAYER_ShowControlPanel = 2;
        public static final int PLAYER_HideControlPanel = 3;
        public static final int PLAYER_ScreenSizeModeChanged = 4;
        public static final int PLAYER_ShowVolumePercent = 5;
        public static final int PLAYER_HideVolumePercent = 6;
    }
}
