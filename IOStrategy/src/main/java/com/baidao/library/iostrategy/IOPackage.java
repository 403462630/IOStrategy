package com.baidao.library.iostrategy;

/**
 * @author rjhy
 * @created on 16-10-20
 * @desc desc
 */
public interface IOPackage {
    /** 最小值 */
    public static final int PRIORITY_MIN = 1;
    /** 最大值 */
    public static final int PRIORITY_MAX = 5;

    /**
     * 优先级
     * @return int
     */
    public int getPriority();

    /**
     * 序列号
     * @return int
     */
    public int getSequence();

    /**
     * 是否过期
     * @return boolean
     */
    public boolean isExpired();

    /**
     * 设置是否过期
     * @param isExpired
     */
    public void setExpired(boolean isExpired);

    /**
     * 是否有重发机制
     * @return boolean
     */
    public boolean hasResendHandler();

    /**
     * 获取发送失败次数
     * @return int
     */
    public int getFailedCount();

    /**
     * 发送失败
     */
    public void onSendFailed();

    /**
     * 超时时长
     * @return long
     */
    public long timeout();

    /**
     * 是否有超时机制
     * @return boolean
     */
    public boolean hasTimeoutTask();
}
