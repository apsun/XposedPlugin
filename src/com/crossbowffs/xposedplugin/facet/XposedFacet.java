package com.crossbowffs.xposedplugin.facet;

import com.crossbowffs.xposedplugin.manager.AndroidManifestManager;
import com.crossbowffs.xposedplugin.manager.XposedBridgeDependencyManager;
import com.crossbowffs.xposedplugin.manager.XposedBridgeJarManager;
import com.crossbowffs.xposedplugin.manager.XposedInitAssetManager;
import com.crossbowffs.xposedplugin.utils.IdeaUtils;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XposedFacet extends Facet<XposedFacetConfiguration> {
    public static final FacetTypeId<XposedFacet> ID = new FacetTypeId<XposedFacet>("Xposed");

    public XposedFacet(@NotNull Module module, @NotNull String name, @NotNull XposedFacetConfiguration configuration, Facet underlyingFacet) {
        super(getFacetType(), module, name, configuration, underlyingFacet);
    }

    @Override
    public void initFacet() {
        StartupManager.getInstance(getModule().getProject()).runWhenProjectIsInitialized(new Runnable() {
            @Override
            public void run() {
                final Module module = getModule();
                XposedBridgeJarManager.getInstance(module).getOrCopyXposedJar();
                AndroidManifestManager.ensureManifestInfo(module);
                XposedBridgeDependencyManager.getInstance(module).ensureXposedBridgeDependency();
                IdeaUtils.syncGradleIfRequired(module, new Consumer<Boolean>() {
                    @Override
                    public void consume(Boolean success) {
                        if (success) {
                            XposedInitAssetManager.getInstance(module).syncDeclaredXposedHooks();
                        }
                    }
                });
            }
        });
    }

    @NotNull
    public static XposedFacetType getFacetType() {
        return (XposedFacetType)FacetTypeRegistry.getInstance().findFacetType(ID);
    }

    @Nullable
    public static XposedFacet getInstance(@NotNull Module module) {
        return FacetManager.getInstance(module).getFacetByType(ID);
    }
}
