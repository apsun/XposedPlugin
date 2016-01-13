package com.crossbowffs.xposedplugin.manager;

import com.crossbowffs.xposedplugin.utils.IOUtils;
import com.crossbowffs.xposedplugin.utils.XposedConsts;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidRootUtil;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XposedBridgeJarManager {
    private static final Logger LOG = Logger.getInstance(XposedBridgeJarManager.class);

    private static void copyXposedBridgeJarInternal(@NotNull Project project, @NotNull VirtualFile libsDir) throws IOException {
        String jarName = XposedConsts.XPOSED_BRIDGE_LIB_NAME;
        if (libsDir.findChild(jarName) != null) {
            LOG.debug("Xposed bridge API library already exists");
            return;
        }

        String resourcePath = XposedConsts.JAR_RESOURCE_PATH_LIBS + jarName;
        InputStream in = XposedBridgeJarManager.class.getResourceAsStream(resourcePath);
        VirtualFile jarFile = libsDir.createChildData(project, jarName);
        try {
            OutputStream out = jarFile.getOutputStream(project);
            try {
                IOUtils.copyStreams(in, out);
            } finally {
                IOUtils.silentlyClose(out);
            }
        } finally {
            IOUtils.silentlyClose(in);
        }
    }

    public static void copyXposedBridgeJarToModule(@NotNull Module module) {
        AndroidFacet androidFacet = AndroidFacet.getInstance(module);
        if (androidFacet == null) {
            throw new XposedPluginException("No Android facet found in module");
        }

        VirtualFile rootDir = AndroidRootUtil.getMainContentRoot(androidFacet);
        if (rootDir == null) {
            throw new XposedPluginException("Failed to find main content root");
        }

        Project project = module.getProject();
        VirtualFile libsDir;
        try {
            libsDir = AndroidUtils.createChildDirectoryIfNotExist(project, rootDir, "libs");
        } catch (IOException e) {
            throw new XposedPluginException("Failed to create libs directory", e);
        }

        try {
            copyXposedBridgeJarInternal(project, libsDir);
        } catch (IOException e) {
            throw new XposedPluginException("Failed to copy Xposed bridge API library to module", e);
        }
    }
}
