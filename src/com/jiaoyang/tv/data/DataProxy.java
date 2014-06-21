package com.jiaoyang.tv.data;

import java.lang.reflect.Type;
import java.util.Locale;

import android.content.Context;
import android.os.Build;

import com.google.gson.reflect.TypeToken;
import com.jiaoyang.tv.app.JiaoyangTvApplication;

public class DataProxy {

    // 首页接口
    private static final String HOME_PAGE = "http://pad.kankan.com/kktv/index.json";
    // 各个频道分类信息的接口
    private static final String CATEGORY_TEMPLATE_URL = "http://list.pad.kankan.com/common_mobile_list/act,0/type,%s/os,kktv/osver,%s/productver,%s/";
    private static final String VIP_CATEGORY_TEMPLATE_URL = "http://list.pad.kankan.com/vip_mobile_list/act,0/type,%s/os,kktv/osver,%s/productver,%s/";
    // 频道页内容的接口
    public static final String CHANNEL_LIST_TEMPLATE_URL = "http://list.pad.kankan.com/common_mobile_list/act,1/type,%s/os,kktv/osver,%s/productver,%s/";

    private static final String SEARCH_URL = "http://search.pad.kankan.com/search4phone.php";
    private static final String SUGGESTIONS_URL = "http://search.pad.kankan.com/search4tv.php";
    private static final String SEARCH_HOT = "http://search.pad.kankan.com/index.json";
    private static final String SEARCH_RECOMMEND = "http://search.pad.kankan.com/index.json";

    private static final String ORDERS_URL = "http://list.pad.kankan.com/vip_mobile_list/act,2/type,vip/";
    private static final String VIP_URL = "http://shop.vip.kankan.com/userinfo/getBerylAndVipState";

    private static final String UPDATE_URL = "http://list.pad.kankan.com/kktv_check_version/";
    private static final String VIP_CHARGE_URL = "http://auth.vip.kankan.com/vod/auth?rtnformat=1&" +
            "productids=%d&peerid=%s&filtertype=0&refid=androidtv&" +
            "callback=callback&version=2&clientoperationid=%s";

    private static final String CHECK_MODULE_UPDATE_URL = "http://list.pad.kankan.com/android_upgrade/";

    private static final String RELATE_MOVIES_URL = "http://api.pad.kankan.com/api.php";

    private static final String TOPIC_LIST_URL = "http://pad.kankan.com/kktv/topic_list.json";
    private static final String TOPIC_URL = "http://pad.kankan.com/android/topic/%s.json";

    private static final String HOME_CHANNEL_URL = "http://pad.kankan.com/kktv/channel_1_1.json";

    private static final String START_UP_POSTER_URL = "http://pad.kankan.com/kktv/main.json";// 启动时的品宣图和背景图

    private static DataProxy sInstance = null;

    private HomePage mHomePage;
    private Movie[] mTopicList;

    synchronized public static DataProxy getInstance() {
        if (sInstance == null) {
            sInstance = new DataProxy();
        }

        return sInstance;
    }

    synchronized public HomePage getHomePage() {
        return mHomePage;
    }

    synchronized public void setHomePage(HomePage homePage) {
        mHomePage = homePage;
    }

    public static String getCategoryUrl(String type) {
        String url = null;

        if (type.equals(ChannelType.VIP)) {
            url = String.format(Locale.US, VIP_CATEGORY_TEMPLATE_URL, type, Build.VERSION.RELEASE,
                    JiaoyangTvApplication.versionCode);
        } else {
            url = String.format(Locale.US, CATEGORY_TEMPLATE_URL, type, Build.VERSION.RELEASE,
                    JiaoyangTvApplication.versionCode);
        }

        return url;
    }

    public Category getCategory(String url) throws InvalidApiVersionException {
        URLLoader loader = new URLLoader();
        Type t = new TypeToken<Response<Category>>() {
        }.getType();

        return (Category) loader.loadObject(url, t);
    }

    public MovieList getMovies(Category category, int page, String type) throws InvalidApiVersionException {
        return getMovies(category, page, type, 50);
    }

    public MovieList getMovies(Category category, int page, String type, int itemsPerPage)
            throws InvalidApiVersionException {
        String url = String.format(Locale.US, CHANNEL_LIST_TEMPLATE_URL, type, Build.VERSION.RELEASE,
                JiaoyangTvApplication.versionCode);
        URLRequest request = new URLRequest(url, URLRequest.TYPE_EXTRA);
        if (category.orders != null) {
            request.appendQueryParameter(category.orders.name, category.orders.getValue().value);

            if (category.filters != null) {
                for (Filter filter : category.filters) {
                    request.appendQueryParameter(filter.name, filter.getValue().value);
                }
            }

            request.appendQueryParameter("page", page);
        }

        request.appendQueryParameter("pernum", itemsPerPage);
        URLLoader loader = new URLLoader();
        Type t = new TypeToken<Response<MovieList>>() {
        }.getType();

        return (MovieList) loader.loadObject(request, t);
    }

    public MovieList getMovies(String keyword, int page) throws InvalidApiVersionException {
        URLRequest request = new URLRequest(SEARCH_URL);
        request.appendQueryParameter("keyword", keyword);
        request.appendQueryParameter("page", page);

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<Response<MovieList>>() {
        }.getType();

        return (MovieList) loader.loadObject(request, type);
    }

    public Search[] getSearchRecommend() {
        URLRequest request = new URLRequest(SEARCH_RECOMMEND);
        URLLoader loader = new URLLoader();
        Type type = new TypeToken<NewResponse<Search>>() {
        }.getType();

        return (Search[]) loader.newloadObject(request, type);
    }

    public MovieList getSuggestions(String keyword, int page) {
        URLRequest request = new URLRequest(SUGGESTIONS_URL);
        request.appendQueryParameter("keyword", keyword);
        request.appendQueryParameter("page", page);

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<Response<MovieList>>() {
        }.getType();

        return (MovieList) loader.loadObject(request, type);
    }

    public Search[] getSearchHot() throws InvalidApiVersionException {
        URLRequest request = new URLRequest(SEARCH_HOT);

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<NewResponse<Search>>() {
        }.getType();

        return (Search[]) loader.newloadObject(request, type);
    }

    public Movie getMovieDetail(int type, int id, boolean isVip) {
        URLLoader loader = new URLLoader();
        String url = Movie.getDetailUrlFromId(type, id, isVip);
        return loader.loadObject(url, Movie.class);
    }

    public EpisodeList getMovieEpisodes(int type, int id, boolean isVip) {
        URLLoader loader = new URLLoader();
        String url = Movie.getEpisodesUrlFromId(type, id, isVip);
        return loader.loadObject(url, EpisodeList.class);
    }

    public OrderList getOrders(String userId) {
        URLRequest request = new URLRequest(ORDERS_URL, URLRequest.TYPE_EXTRA);
        request.appendQueryParameter("rand", String.valueOf(System.currentTimeMillis() / 1000 / 60));
        request.appendQueryParameter("uid", userId);

        Type type = new TypeToken<Response<OrderList>>() {
        }.getType();

        return (OrderList) (new URLLoader()).loadObject(request, type);
    }

    public VipState getVipState(String userId) {
        URLRequest request = new URLRequest(VIP_URL);
        request.appendQueryParameter("userid", userId);
        URLLoader loader = new URLLoader();

        return loader.loadObject(request, VipState.class);
    }

    public UpdateInfo getUpdateInfo(String version, String osVersion) {
        URLRequest request = new URLRequest(UPDATE_URL, URLRequest.TYPE_EXTRA);
        request.appendQueryParameter("ver", version);
        request.appendQueryParameter("os", osVersion);

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<Response<UpdateInfo>>() {
        }.getType();

        return (UpdateInfo) loader.loadObject(request, type);
    }

    public synchronized void loadHomePage() throws InvalidApiVersionException {
        if (mHomePage != null) {
            return;
        }

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<Response<HomePage>>() {
        }.getType();

        mHomePage = (HomePage) loader.loadObject(HOME_PAGE, type);

    }

    public ModuleUpdateInfo getModuleUpdateInfo(String sysVer, String soVer, String appVer, int partnerId) {
        URLRequest request = new URLRequest(CHECK_MODULE_UPDATE_URL, URLRequest.TYPE_EXTRA);
        request.appendQueryParameter("sys_version", sysVer);
        request.appendQueryParameter("downloadengine_version", soVer);
        request.appendQueryParameter("app_version", appVer);
        request.appendQueryParameter("partnerId", "0x" + Integer.toHexString(partnerId));

        return new URLLoader().loadObject(request, ModuleUpdateInfo.class);
    }

    public MovieRelateList getRelateLongMoviesByGson(int movieid, int page, int perpage)
            throws InvalidApiVersionException {
        URLRequest request = new URLRequest(RELATE_MOVIES_URL);
        request.appendQueryParameter("mod", "relate");
        request.appendQueryParameter("osver", "0.9");
        request.appendQueryParameter("type", "movie");
        request.appendQueryParameter("movieid", movieid);
        request.appendQueryParameter("productver", "0.9");
        request.appendQueryParameter("page", page);
        request.appendQueryParameter("perpage", perpage);

        URLLoader loader = new URLLoader();
        Type type = new TypeToken<Response<MovieRelateList>>() {
        }.getType();

        return (MovieRelateList) loader.loadObject(request, type);
    }

    private HomeChannelList mHomeChannelList;

    public HomeChannelList getHomeChannelList() {
        if (mHomeChannelList == null) {
            URLLoader loader = new URLLoader();
            mHomeChannelList = (HomeChannelList) loader.loadObject(HOME_CHANNEL_URL, HomeChannelList.class);
        }
        return mHomeChannelList;
    }

    public Movie[] getTopicPage() {
        return mTopicList;
    }

    public DataProxy loadTopicPage() {
        URLRequest request = new URLRequest(TOPIC_LIST_URL);
        URLLoader loader = new URLLoader();
        Type type = new TypeToken<NewResponse<Movie>>() {
        }.getType();
        mTopicList = (Movie[]) loader.newloadObject(request, type);
        return this;
    }

    public Topic<Movie> getTopic(int id) {
        String url = String.format(Locale.US, TOPIC_URL, id);
        URLRequest request = new URLRequest(url);
        URLLoader loader = new URLLoader();
        Type type = new TypeToken<Topic<Movie>>() {
        }.getType();
        return (Topic<Movie>) loader.loadTopic(request, type);
    }

    private StartUpPoster mStartUpPoster;

    public StartUpPoster getStartUpPoster() {
        if (mStartUpPoster == null) {
            URLLoader loader = new URLLoader();
            mStartUpPoster = (StartUpPoster) loader.loadObject(START_UP_POSTER_URL, StartUpPoster.class);
        }
        return mStartUpPoster;
    }

    public RecommendResponse getMovieRecommendData(Context context, int movieId, int movieType) {
        URLLoader loader = new URLLoader();
        return loader.loadObject(Movie.getMovieRecommendUrl(context, movieId, movieType), RecommendResponse.class);
    }
}
