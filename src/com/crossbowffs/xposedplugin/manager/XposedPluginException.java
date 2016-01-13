package com.crossbowffs.xposedplugin.manager;

public class XposedPluginException extends RuntimeException {
    public XposedPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public XposedPluginException(Throwable cause) {
        super(cause);
    }

    public XposedPluginException(String message) {
        super(message);
    }
}
