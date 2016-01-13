package com.crossbowffs.xposedplugin.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class XposedFacetConfiguration implements FacetConfiguration {
    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext facetEditorContext, FacetValidatorsManager facetValidatorsManager) {
        return new FacetEditorTab[0];
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {

    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {

    }
}
