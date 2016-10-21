package com.baidao.library.iostrategy;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author rjhy
 * @created on 16-10-20
 * @desc desc
 */
public class IOWriter {
    private static final Logger logger = Logger.getLogger("io.socket-IOWriter");
    private BlockingQueue<IOPackage> queue1 = new ArrayBlockingQueue<IOPackage>(50, true);
    private BlockingQueue<IOPackage> queue2 = new ArrayBlockingQueue<IOPackage>(50, true);
    private BlockingQueue<IOPackage> queue3 = new ArrayBlockingQueue<IOPackage>(50, true);
    private BlockingQueue<IOPackage> queue4 = new ArrayBlockingQueue<IOPackage>(500, true);
    private BlockingQueue<IOPackage> queue5 = new ArrayBlockingQueue<IOPackage>(50, true);

    private Object startLock = new Object();
    private Object clearLock = new Object();
    private static AtomicInteger integer = new AtomicInteger();
    private Thread writerThread;
    private Thread clearExpiredThread;
    private boolean isStop = false;
    private IOPackageManager ioPackageManager;

    public IOWriter(IOPackageManager ioPackageManager) {
        this.ioPackageManager = ioPackageManager;
    }

    private IOTransport getIOTransport() {
        return ioPackageManager.getTransport();
    }

    private synchronized ArrayList<IOPackage> nextIOPackageList(BlockingQueue<IOPackage> queue) {
        ArrayList<IOPackage> list = new ArrayList<>();
        queue.drainTo(list);
        return list;
    }

    public void clearup() {
        ArrayList<IOPackage> list1 = nextIOPackageList(queue1);
        ArrayList<IOPackage> list2 = nextIOPackageList(queue2);
        ArrayList<IOPackage> list3 = nextIOPackageList(queue3);
        ArrayList<IOPackage> list4 = nextIOPackageList(queue4);
        ArrayList<IOPackage> list5 = nextIOPackageList(queue5);
        ioPackageManager.handleClearup(list1);
        ioPackageManager.handleClearup(list2);
        ioPackageManager.handleClearup(list3);
        ioPackageManager.handleClearup(list4);
        ioPackageManager.handleClearup(list5);
        notifyQueue();
    }

    public void stop() {
        if (!isStop) {
            isStop = true;
            notifyQueue();
        }
    }

    public void start() {
        synchronized (startLock) {
            isStop = false;
            //if (transportManager.getTransport() == null) {
            //    throw new RuntimeException("transport is null");
            //}
            if (writerThread != null && writerThread.isAlive()) {
                logger.info("IOWriterThread: " + writerThread.getName() + " is alive");
                notifyQueue();
                return;
            }

            writerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    write();
                }
            });
            writerThread.setName("IOWriter thread_" + integer.incrementAndGet());
            writerThread.setDaemon(true);
            writerThread.start();
            logger.info("start IOWriterThread: " + writerThread.getName());
        }
    }

    void notifyQueue() {
        synchronized (queue4) {
            queue4.notifyAll();
        }
    }

    private void startClearInvalidPacket() {
        synchronized (clearLock) {
            if (clearExpiredThread != null && clearExpiredThread.isAlive()) {
                logger.info("clearExpiredThread: " + clearExpiredThread.getName() + " is alive");
                return ;
            }

            clearExpiredThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    clearInvalidPacket(queue1);
                    clearInvalidPacket(queue2);
                    clearInvalidPacket(queue3);
                    clearInvalidPacket(queue4);
                    clearInvalidPacket(queue5);
                }
            });
            clearExpiredThread.setName("Clear Expired Packet thread_" + integer.incrementAndGet());
            clearExpiredThread.setDaemon(true);
            clearExpiredThread.start();
            logger.info("start clearExpiredThread: " + writerThread.getName());
        }
    }

    private synchronized void clearInvalidPacket(BlockingQueue<IOPackage> queue) {
        IOPackage ioPackage = queue.peek();
        while(ioPackage != null && ioPackage.isExpired()) {
            queue.poll();
            logger.info("clearInvalidPacket--sequence: " + ioPackage.getSequence());
            ioPackage = queue.peek();
        }
    }

    public void sendIOPackage(IOPackage ioPackage) {
        if (ioPackage == null || ioPackage.isExpired()) {
            return ;
        }
        try {
            switch (ioPackage.getPriority()) {
                case 1:
                    if (queue1.size() > 25) {
                        startClearInvalidPacket();
                    }
                    queue1.put(ioPackage);
                    break;
                case 2:
                    if (queue2.size() > 25) {
                        startClearInvalidPacket();
                    }
                    queue2.put(ioPackage);
                    break;
                case 3:
                    if (queue3.size() > 25) {
                        startClearInvalidPacket();
                    }
                    queue3.put(ioPackage);
                    break;
                case 4:
                    if (queue4.size() > 250) {
                        startClearInvalidPacket();
                    }
                    queue4.put(ioPackage);
                    break;
                case 5:
                    if (queue5.size() > 25) {
                        startClearInvalidPacket();
                    }
                    queue5.put(ioPackage);
                    break;
                default:
                    if (queue4.size() > 250) {
                        startClearInvalidPacket();
                    }
                    queue4.put(ioPackage);
                    break;
            }
        } catch (InterruptedException e) {
            logger.info("push message to queue Exception: " + e.getMessage());
            ioPackageManager.handlePushFailed(ioPackage);
        }

        if (!isStop) {
            notifyQueue();
        }
    }

    private synchronized IOPackage nextIOPackage() {
        IOPackage ioPackage = null;
        while (!isStop && (
                (ioPackage = queue1.poll()) == null
                        && (ioPackage = queue2.poll()) == null
                        && (ioPackage = queue3.poll()) == null
                        && (ioPackage = queue4.poll()) == null
                        && (ioPackage = queue5.poll()) == null)) {
            try {
                synchronized (queue4) {
                    queue4.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ioPackage;
    }

    private void write() {
        while (!isStop) {
            IOPackage ioPackage = nextIOPackage();
            if (ioPackage != null && !ioPackage.isExpired()) {
                try {
                    getIOTransport().send(ioPackage);
                } catch (Exception e) {
                    logger.info("send Exception: " + e.getMessage());
                    ioPackageManager.handleSendError(ioPackage);
                }
            }

            if (isStop) {
                try {
                    logger.info("IOWriterThread:" + writerThread.getName() + " stop");
                    synchronized (queue4) {
                        queue4.wait(60_000 * 2);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
