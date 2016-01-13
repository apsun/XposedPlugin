package com.crossbowffs.xposedplugin.manager;

import com.crossbowffs.xposedplugin.utils.IOUtils;
import com.crossbowffs.xposedplugin.utils.IdeaUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XposedInitAssetManager {
    private static final Logger LOG = Logger.getInstance(XposedInitAssetManager.class);
    private static final Map<Module, XposedInitAssetManager> sInstanceCache = new HashMap<Module, XposedInitAssetManager>(1);

    private final Module mModule;
    private List<String> mDeclaredXposedHooks = new ArrayList<String>(1);
    private VirtualFile mXposedInitFile;
    private long mLastModificationTime = -1;

    private XposedInitAssetManager(@NotNull Module module) {
        mModule = module;
    }

    public boolean isXposedHookDeclared(@NotNull String className) {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                readXposedInitFile();
            }
        });
        return mDeclaredXposedHooks.contains(className);
    }

    public void declareXposedHook(@NotNull final String className) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                writeXposedInitFile(className);
            }
        });
    }

    @NotNull
    private VirtualFile createXposedInitFile() {
        VirtualFile moduleDir = IdeaUtils.getModuleDir(mModule);
        if (moduleDir == null) {
            throw new XposedPluginException("Could not find module root directory");
        }

        VirtualFile assetsDir = moduleDir.findChild("assets");
        if (assetsDir == null) {
            try {
                assetsDir = moduleDir.createChildDirectory(this, "assets");
            } catch (IOException e) {
                throw new XposedPluginException("Could not create assets directory", e);
            }
        }

        VirtualFile xposedInitFile = assetsDir.findChild("xposed_init");
        if (xposedInitFile == null) {
            try {
                xposedInitFile = assetsDir.createChildData(this, "xposed_init");
            } catch (IOException e) {
                throw new XposedPluginException("Could not create assets/xposed_init file", e);
            }
        }

        return xposedInitFile;
    }

    @Nullable
    private VirtualFile findXposedInitFile() {
        VirtualFile moduleDir = IdeaUtils.getModuleDir(mModule);
        if (moduleDir == null) {
            throw new XposedPluginException("Could not find module root directory");
        }

        VirtualFile assetsDir = moduleDir.findChild("assets");
        if (assetsDir == null) {
            LOG.info("Could not find assets directory");
            return null;
        }

        VirtualFile xposedInitFile = assetsDir.findChild("xposed_init");
        if (xposedInitFile == null) {
            LOG.info("Could not find assets/xposed_init file");
            return null;
        }

        return xposedInitFile;
    }

    @NotNull
    private List<String> parseXposedInitFile() {
        List<String> classNames = new ArrayList<String>(1);
        InputStream in = null;
        try {
            in = mXposedInitFile.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, mXposedInitFile.getCharset()));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                classNames.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new XposedPluginException("Failed to read assets/xposed_init file", e);
        } finally {
            IOUtils.silentlyClose(in);
        }
        return classNames;
    }

    private void readXposedInitFile() {
        if (mXposedInitFile == null || !mXposedInitFile.exists()) {
            mXposedInitFile = findXposedInitFile();
            if (mXposedInitFile == null) {
                mDeclaredXposedHooks.clear();
                return;
            }
        }

        if (mLastModificationTime >= mXposedInitFile.getTimeStamp()) {
            LOG.debug("xposed_init file did not change, using cached data");
            return;
        }

        LOG.debug("Refreshing xposed_init file");
        mDeclaredXposedHooks = parseXposedInitFile();
        mLastModificationTime = mXposedInitFile.getTimeStamp();
    }

    private void writeXposedInitFile(@NotNull String newClassName) {
        if (isXposedHookDeclared(newClassName)) {
            return;
        }

        mDeclaredXposedHooks.add(newClassName.trim());

        if (mXposedInitFile == null || !mXposedInitFile.exists()) {
            mXposedInitFile = createXposedInitFile();
        }

        Charset charset = mXposedInitFile.getCharset();
        String lineSep = mXposedInitFile.getDetectedLineSeparator();
        if (lineSep == null) {
            lineSep = LineSeparator.getSystemLineSeparator().getSeparatorString();
        }

        OutputStream out = null;
        OutputStreamWriter writer = null;
        try {
            out = mXposedInitFile.getOutputStream(this);
            writer = new OutputStreamWriter(out, charset);
            for (String hookClassNames : mDeclaredXposedHooks) {
                writer.write(hookClassNames);
                writer.write(lineSep);
            }
        } catch (IOException e) {
            throw new XposedPluginException("Failed to write to assets/xposed_init file", e);
        } finally {
            IOUtils.silentlyClose(writer);
            IOUtils.silentlyClose(out);
        }

        LOG.debug("Successfully wrote xposed_init file");
        mLastModificationTime = mXposedInitFile.getTimeStamp();
    }

    @NotNull
    public static XposedInitAssetManager getInstance(@NotNull Module module) {
        XposedInitAssetManager instance = sInstanceCache.get(module);
        if (instance == null) {
            instance = new XposedInitAssetManager(module);
            sInstanceCache.put(module, instance);
        }
        return instance;
    }
}
