package com.crossbowffs.xposedplugin.utils;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public final class XposedIcons {
    private XposedIcons() { }

    private static Icon load(String path) {
        return IconLoader.getIcon(XposedConsts.JAR_RESOURCE_PATH_ICONS + path, XposedIcons.class);
    }

    public static final Icon XPOSED_16x16 = load("Xposed16x16.png");
}
