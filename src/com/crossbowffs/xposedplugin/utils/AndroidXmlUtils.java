package com.crossbowffs.xposedplugin.utils;

import com.android.SdkConstants;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AndroidXmlUtils {
    @Nullable
    public static XmlTag getWritableAndroidManifestXml(@NotNull Module module) {
        AndroidFacet androidFacet = AndroidFacet.getInstance(module);
        if (androidFacet == null) {
            return null;
        }

        Manifest manifest = androidFacet.getManifest();
        if (manifest == null) {
            return null;
        }

        XmlTag manifestTag = manifest.getXmlTag();
        if (manifestTag == null) {
            return null;
        }

        PsiFile manifestFile = manifestTag.getContainingFile();
        if (manifestFile == null) {
            return null;
        }

        VirtualFile vManifestFile = manifestFile.getVirtualFile();
        if (vManifestFile == null) {
            return null;
        }

        if (!ReadonlyStatusHandler.ensureFilesWritable(manifestFile.getProject(), vManifestFile)) {
            return null;
        }

        return manifestTag;
    }

    public static XmlTag findOrCreateTag(@NotNull XmlTag parentTag, @NotNull String childName) {
        XmlTag childTag = parentTag.findFirstSubTag(childName);
        if (childTag == null) {
            childTag = createTag(parentTag, childName);
        }
        return childTag;
    }

    public static XmlTag createTag(@NotNull XmlTag parentTag, @NotNull String childName) {
        XmlTag childTag = parentTag.createChildTag(childName, "", null, false);
        childTag = parentTag.addSubTag(childTag, true);
        return childTag;
    }

    public static String getAndroidXmlAttribute(@NotNull XmlTag tag, @NotNull String key) {
        return tag.getAttributeValue(key, SdkConstants.NS_RESOURCES);
    }

    public static void setAndroidXmlAttribute(@NotNull XmlTag tag, @NotNull String name, @NotNull String value) {
        tag.setAttribute(name, SdkConstants.NS_RESOURCES, value);
    }
}
