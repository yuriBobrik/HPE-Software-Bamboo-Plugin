package com.hpe.application.automation.tfs;

import com.hpe.application.automation.tools.common.sdk.Logger;

/**
 * Created by ybobrik on 1/23/2017.
 */
public class RestLogger implements Logger {
    @Override
    public void log(String message) {
        if (message == null || message.isEmpty())
            return;
        System.out.println(message);
    }
}
