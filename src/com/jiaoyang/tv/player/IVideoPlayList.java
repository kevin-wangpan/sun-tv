package com.jiaoyang.tv.player;

public interface IVideoPlayList {

    /**
     * 获取当前正在播放的条目
     * 
     * @return PlayItem
     */
    public IVideoItem getCurrentPlayItem();
    
    /**
     * 将当前播放条目向前一个播放条目移动，并且返回移动后的新条目
     * 
     * @return PlayItem
     */
    public IVideoItem moveToPrevPlayItem();

    /**
     * 将当前条目向下一个播放条目移动，并且返回当前播放条目移动后的新条目
     * 
     * @return PlayItem
     */
    public IVideoItem moveToNextPlayItem();

    public int getCurrentPlayIndex();

    /**
     * 判断是否存在上一集
     * 
     * @return boolean
     */
    public boolean hasPrevVideo();

    /**
     * 判断是否存在下一集
     * 
     * @return
     */
    public boolean hasNextVideo();

    /**
     * 是否是试看影片
     * 
     * @return
     */
    public abstract boolean isTry();

    /**
     * 判断是否是鉴权影片（付费影片的意思）
     * 
     * @return
     */
    public abstract boolean isAuthorityMovie();
    
    /**
     * 
     * 获取Index下的条目
     * @Title: getIndexPlayItem
     * @param index
     * @return
     * @return IVideoItem
     * @date 2013-11-26 下午1:49:12
     */
    public IVideoItem moveToPlayItemByIndex(int index);
    
    /**
     * 
     * 获取总条目数量
     * @Title: getPlayItemSizes
     * @return
     * @return int
     * @date 2013-11-26 下午2:13:03
     */
    public int getPlayItemSizes();
    
}
