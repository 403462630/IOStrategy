package com.baidao.library.iostrategy;

/**
 * @author rjhy
 * @created on 16-10-20
 * @desc desc
 */
public abstract class DefaultIOPackage implements IOPackage {

    private boolean isExpired;
    private int failedCount;

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public boolean isExpired() {
        return isExpired;
    }

    @Override
    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    @Override
    public boolean hasResendHandler() {
        return false;
    }

    @Override
    public int getFailedCount() {
        return failedCount;
    }

    @Override
    public void onSendFailed() {
        failedCount++;
    }

    @Override
    public long timeout() {
        return 20_000;
    }

    @Override
    public boolean hasTimeoutTask() {
        return true;
    }
}
