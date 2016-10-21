package com.baidao.library.iostrategy;

/**
 * @author rjhy
 * @created on 16-10-21
 * @desc desc
 */
public class IOStrategyManager {
    private IOPackageManager ioPackageManager;
    private IOPackageListener ioPackageListener;

    public IOStrategyManager() {
        ioPackageManager = new IOPackageManager();
        init();
    }

    public void setIoPackageListener(IOPackageListener ioPackageListener) {
        this.ioPackageListener = ioPackageListener;
    }

    private void init() {
        ioPackageManager.setIoStrategyManager(this);
    }

    public void setIOStrategy(IOStrategy ioStrategy) {
        ioPackageManager.setIoStrategy(ioStrategy);
    }

    public void setIOTransport(IOTransport ioTransport) {
        ioPackageManager.setTransport(ioTransport);
    }

    public void start() {
        ioPackageManager.start();
    }

    public void stop() {
        ioPackageManager.stop();
    }

    public void clearup() {
        ioPackageManager.clearup();
    }

    public void sendIOPackage(IOPackage ioPackage) {
        ioPackageManager.sendIOPackage(ioPackage);
    }

    public void receiveIOPackage(IOPackage ioPackage) {
        ioPackageManager.handleReceiveIOPackage(ioPackage);
    }

    void handleFailedIOPackage(IOPackage ioPackage) {
        if (ioPackageListener != null) {
            ioPackageListener.onSendFailedIOPackage(ioPackage);
        }
    }

    void handleReceiveIOPackage(IOPackage ioPackage) {
        if (ioPackageListener != null) {
            ioPackageListener.onReceiveIOPackage(ioPackage);
        }
    }
}
