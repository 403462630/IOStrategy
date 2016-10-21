package com.baidao.library.iostrategy;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * @author rjhy
 * @created on 16-8-18
 * @desc desc
 */
public class IOPackageTask {
    private static final Logger logger = Logger.getLogger("io.socket-IOPacketTask");
    private static final long DEFAULT_TIME_OUT = 20 * 1000;
    public static final int INVALID_TASK_ID = -1111;
    private IOPackage ioPackage;
    private Timer timer = new Timer();
    private IOPackageManager ioPackageManager;
    private long timeout;

    public IOPackageTask(IOPackageManager ioPackageManager, IOPackage ioPackage) {
        this(ioPackageManager, ioPackage, DEFAULT_TIME_OUT);
    }

    public IOPackageTask(IOPackageManager ioPackageManager, IOPackage ioPackage, long timeout) {
        this.timeout = timeout;
        this.ioPackageManager = ioPackageManager;
        this.ioPackage = ioPackage;
    }

    public void execute() {
        if (ioPackage == null) {
            return;
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                logger.info(
                        "send text time out, sequence = " + ioPackage.getSequence());
                ioPackage.setExpired(true);
                ioPackageManager.handleTimeOuPackage(ioPackage);
            }
        };
        timer.schedule(task, timeout);
    }

    public void cancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public int getTaskId() {
        return ioPackage == null ? INVALID_TASK_ID : ioPackage.getSequence();
    }
}
