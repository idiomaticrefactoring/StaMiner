package com.aurora7795;

import java.io.IOException;

/**
 * Created by aurora7795 on 09/02/2017.
 */
public interface ISerialPortWrapper {

    char Read() throws IOException;

    void Write(char request) throws IOException;

    void SetTimeout(int timeout);
}
