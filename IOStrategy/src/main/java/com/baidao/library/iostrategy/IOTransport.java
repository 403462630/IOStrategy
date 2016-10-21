package com.baidao.library.iostrategy;

/**
 * @author rjhy
 * @created on 16-10-20
 * @desc desc
 */
public interface IOTransport {
    public void send(IOPackage ioPackage) throws Exception;
}
