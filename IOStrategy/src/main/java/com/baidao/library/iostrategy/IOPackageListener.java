package com.baidao.library.iostrategy;

/**
 * @author rjhy
 * @created on 16-10-21
 * @desc desc
 */
public interface IOPackageListener {
    public void onReceiveIOPackage(IOPackage ioPackage);
    public void onSendFailedIOPackage(IOPackage ioPackage);
}
