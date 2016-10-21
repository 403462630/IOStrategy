package com.baidao.library.iostrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author rjhy
 * @created on 16-10-20
 * @desc desc
 */
public class IOPackageManager {
    private HashMap<Integer, IOPackageTask> map = new HashMap<>();
    private IOWriter ioWriter;
    private IOTransport ioTransport;
    private ExecutorService executorService = Executors.newFixedThreadPool(3);
    private IOStrategy ioStrategy;
    private IOStrategyManager ioStrategyManager;

    public void setIoStrategyManager(IOStrategyManager ioStrategyManager) {
        this.ioStrategyManager = ioStrategyManager;
    }

    public void setIoStrategy(IOStrategy ioStrategy) {
        this.ioStrategy = ioStrategy;
    }

    public IOPackageManager() {
        this.ioWriter = new IOWriter(this);
    }

    public IOTransport getTransport() {
        return ioTransport;
    }

    public void setTransport(IOTransport transport) {
        this.ioTransport = transport;
    }

    public void start() {
        ioWriter.start();
    }

    public void stop() {
        ioWriter.stop();
    }

    public void clearup() {
        ioWriter.clearup();
        map.clear();
    }

    public void sendIOPackage(IOPackage ioPackage) {
        if (ioPackage != null) {
            if (ioStrategy.isTimeoutHandler() && ioPackage.hasTimeoutTask()) {
                stopTimeoutTask(ioPackage);
                startTimeoutTask(ioPackage);
            }
            ioWriter.sendIOPackage(ioPackage);
        }
    }

    private void startTimeoutTask(IOPackage ioPackage) {
        long timeout = ioPackage.timeout();
        if (timeout < 1000) {
            timeout = ioStrategy.timeout();
        }
        IOPackageTask task = new IOPackageTask(this, ioPackage, timeout);
        task.execute();
        putTimeoutTask(task);
    }

    private void stopTimeoutTask(IOPackage message) {
        IOPackageTask task = map.get(message.getSequence());
        if (task != null) {
            removeTimeoutTast(task.getTaskId());
            task.cancel();
        }
    }

    void removeTimeoutTast(int taskId) {
        map.remove(taskId);
    }

    void putTimeoutTask(IOPackageTask task) {
        int key = task.getTaskId();
        if (key != IOPackageTask.INVALID_TASK_ID) {
            map.put(key, task);
        }
    }

    private void resendIOPackage(IOPackage ioPackage) {
        if (ioPackage.getFailedCount() < ioStrategy.maxResendCount()) {
            ioPackage.onSendFailed();
            sendIOPackage(ioPackage);
        } else {
            ioStrategyManager.handleFailedIOPackage(ioPackage);
        }
    }

    void handleTimeOuPackage(IOPackage ioPackage) {
        removeTimeoutTast(ioPackage.getSequence());
        ioStrategyManager.handleFailedIOPackage(ioPackage);
    }

    void handleClearup(ArrayList<IOPackage> list) {
        if (list != null && !list.isEmpty()) {
            for (IOPackage ioPackage : list) {
                stopTimeoutTask(ioPackage);
                if (!ioPackage.isExpired()) {
                    ioStrategyManager.handleFailedIOPackage(ioPackage);
                }
            }
        }
    }

    void handlePushFailed(IOPackage ioPackage) {
        stopTimeoutTask(ioPackage);
        if (!ioPackage.isExpired()) {
            if (ioStrategy.isResendHandler() && ioPackage.hasResendHandler()) {
                resendIOPackage(ioPackage);
            } else {
                ioStrategyManager.handleFailedIOPackage(ioPackage);
            }
        }
    }

    void handleSendError(IOPackage ioPackage) {
        stopTimeoutTask(ioPackage);
        if (!ioPackage.isExpired()) {
            if (ioStrategy.isResendHandler() && ioPackage.hasResendHandler()) {
                resendIOPackage(ioPackage);
            } else {
                ioStrategyManager.handleFailedIOPackage(ioPackage);
            }
        }
    }

    void handleReceiveIOPackage(final IOPackage ioPackage) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                stopTimeoutTask(ioPackage);
                ioStrategyManager.handleReceiveIOPackage(ioPackage);
            }
        });
    }
}
