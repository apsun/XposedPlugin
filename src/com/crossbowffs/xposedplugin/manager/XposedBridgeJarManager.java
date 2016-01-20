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
import java.util.HashMap;
import java.util.Map;

public class XposedBridgeJarManager {
    private static final Logger LOG = Logger.getInstance(XposedBridgeJarManager.class);
    private static final Map<Module, XposedBridgeJarManager> sInstanceCache = new HashMap<Module, XposedBridgeJarManager>(1);

    private final Module mModule;
    private VirtualFile mXposedJar;

    private XposedBridgeJarManager(@NotNull Module module) {
        mModule = module;
    }

    private VirtualFile getLibsDir() {
        AndroidFacet androidFacet = AndroidFacet.getInstance(mModule);
        if (androidFacet == null) {
            throw new XposedPluginException("No Android facet found in module");
        }

        VirtualFile rootDir = AndroidRootUtil.getMainContentRoot(androidFacet);
        if (rootDir == null) {
            throw new XposedPluginException("Failed to find main content root");
        }

        Project project = mModule.getProject();
        try {
            return AndroidUtils.createChildDirectoryIfNotExist(project, rootDir, "libs");
        } catch (IOException e) {
            throw new XposedPluginException("Failed to create libs directory", e);
        }
    }

    private VirtualFile copyXposedJarInternal() throws IOException {
        VirtualFile libsDir = getLibsDir();

        String jarName = XposedConsts.XPOSED_BRIDGE_LIB_NAME;
        VirtualFile jarFile = libsDir.findChild(jarName);
        if (jarFile != null) {
            LOG.debug("Xposed bridge API library already exists");
            return jarFile;
        }

        Project project = mModule.getProject();
        String resourcePath = XposedConsts.JAR_RESOURCE_PATH_LIBS + jarName;
        InputStream in = XposedBridgeJarManager.class.getResourceAsStream(resourcePath);
        jarFile = libsDir.createChildData(project, jarName);
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
        return jarFile;
    }

    private VirtualFile copyXposedJar() {
        try {
            return copyXposedJarInternal();
        } catch (IOException e) {
            throw new XposedPluginException("Failed to copy XposedBridge jar to module", e);
        }
    }

    public VirtualFile getOrCopyXposedJar() {
        if (mXposedJar == null || !mXposedJar.exists()) {
            mXposedJar = copyXposedJar();
        }
        return mXposedJar;
    }

    public static XposedBridgeJarManager getInstance(@NotNull Module module) {
        XposedBridgeJarManager instance = sInstanceCache.get(module);
        if (instance == null) {
            instance = new XposedBridgeJarManager(module);
            sInstanceCache.put(module, instance);
        }
        return instance;
    }
}
