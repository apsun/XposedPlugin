package com.crossbowffs.xposedplugin.facet;

import com.crossbowffs.xposedplugin.utils.XposedIcons;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class XposedFacetType extends FacetType<XposedFacet, XposedFacetConfiguration> {
    public static final String TYPE_ID = "Xposed";

    public XposedFacetType() {
        super(XposedFacet.ID, TYPE_ID, "Xposed");
    }

    @Override
    public XposedFacetConfiguration createDefaultConfiguration() {
        return new XposedFacetConfiguration();
    }

    @Override
    public XposedFacet createFacet(@NotNull Module module, String name, @NotNull XposedFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new XposedFacet(module, name, configuration, underlyingFacet);
    }

    @Override
    public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof JavaModuleType;
    }

    @Override
    public Icon getIcon() {
        return XposedIcons.XPOSED_16x16;
    }
}

