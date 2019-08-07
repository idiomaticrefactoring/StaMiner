package com.aurora7795;

import junit.framework.TestCase;

/**
 * Created by aurora7795 on 13/02/2017.
 */
public class purejavacommWrapperTest extends TestCase {

    private String comPort = "/dev/tty.usbserial-fd1";
    private int baudRate = 9600;

    public void testRead() throws Exception {
        purejavacommWrapper testWrapper = new purejavacommWrapper(comPort, baudRate);

        testWrapper.Write('x');
      char response = testWrapper.Read();
      assertEquals('x', response);
    }



}