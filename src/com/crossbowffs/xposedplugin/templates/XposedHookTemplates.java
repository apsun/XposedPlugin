package com.crossbowffs.xposedplugin.templates;

import org.jetbrains.annotations.NonNls;

public final class XposedHookTemplates {
    @NonNls public static final String PACKAGE = "XposedPackageHook.java";
    @NonNls public static final String ZYGOTE = "XposedZygoteHook.java";
    @NonNls public static final String RESOURCES = "XposedResourcesHook.java";

    private XposedHookTemplates() { }
}
