package com.baidao.library.iostrategy;

/**
 * @author rjhy
 * @created on 16-8-22
 * @desc desc
 */
public abstract class DefaultIOStrategy implements IOStrategy {

    @Override
    public boolean isheartBeat() {
        return false;
    }

    @Override
    public boolean isTimeoutHandler() {
        return true;
    }

    @Override
    public boolean isResendHandler() {
        return false;
    }

    @Override
    public int maxResendCount() {
        return 3;
    }

    @Override
    public long timeout() {
        return 20_000;
    }
}
