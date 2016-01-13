package com.crossbowffs.xposedplugin.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XposedFacet extends Facet<XposedFacetConfiguration> {
    public static final FacetTypeId<XposedFacet> ID = new FacetTypeId<XposedFacet>("Xposed");

    public XposedFacet(@NotNull Module module, @NotNull String name, @NotNull XposedFacetConfiguration configuration, Facet underlyingFacet) {
        super(getFacetType(), module, name, configuration, underlyingFacet);
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
