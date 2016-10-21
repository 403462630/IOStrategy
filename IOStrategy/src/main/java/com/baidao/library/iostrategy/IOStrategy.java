package com.baidao.library.iostrategy;

import java.util.List;

/**
 * @author rjhy
 * @created on 16-10-20
 * @desc desc
 */
public interface IOStrategy {
    /**
     * 是否有自定义心跳机制
     * @return boolean
     */
    public boolean isheartBeat();

    /**
     * 创建心跳包的IOPackage，返回null代表不需要心跳
     * @return IOPackage
     */
    public IOPackage createHeartbeat();

    /**
     * 是否有超时处理机制
     * @return boolean
     */
    public boolean isTimeoutHandler();

    /**
     * 是否有重发机制
     *
     * @return boolean
     */
    public boolean isResendHandler();

    /**
     * 最大重发次数
     *
     * @return int
     */
    public int maxResendCount();

    /**
     * 超时时长
     *
     * @return long
     */
    public long timeout();

    /**
     * 创建需要优先发送的包，当socket已连接成功之后，就会自动优先发送
     * @return List<IOPackage>
     */
    public List<IOPackage> createAdvanceSendIOMessages();
}
