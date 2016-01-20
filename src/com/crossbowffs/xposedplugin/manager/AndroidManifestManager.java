package com.crossbowffs.xposedplugin.manager;

import com.crossbowffs.xposedplugin.utils.AndroidXmlUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class AndroidManifestManager {
    private static final Logger LOG = Logger.getInstance(AndroidManifestManager.class);
    @NonNls private static final String KEY_XPOSED_MODULE = "xposedmodule";
    @NonNls private static final String KEY_XPOSED_DESCRIPTION = "xposeddescription";
    @NonNls private static final String KEY_XPOSED_MIN_VERSION = "xposedminversion";
    @Nls private static final String DEFAULT_DESCRIPTION = "Your Xposed module description";

    private static XmlTag createMetaDataTag(XmlTag applicationTag, String name, String value) {
        XmlTag tag = AndroidXmlUtils.createTag(applicationTag, "meta-data");
        AndroidXmlUtils.setAndroidXmlAttribute(tag, "name", name);
        AndroidXmlUtils.setAndroidXmlAttribute(tag, "value", value);
        return tag;
    }

    public static boolean ensureManifestInfo(@NotNull Module module) {
        XmlTag manifestTag = AndroidXmlUtils.getWritableAndroidManifestXml(module);
        if (manifestTag == null) {
            LOG.error("Could not find writable AndroidManifest.xml");
            return false;
        }

        XmlTag applicationTag = AndroidXmlUtils.findOrCreateTag(manifestTag, "application");
        XmlTag[] metaDataTags = applicationTag.findSubTags("meta-data", "");
        boolean hasModuleAttr = false, hasDescriptionAttr = false, hasVersionAttr = false;
        for (XmlTag metaDataTag : metaDataTags) {
            String metaDataName = AndroidXmlUtils.getAndroidXmlAttribute(metaDataTag, "name");
            String metaDataValue = AndroidXmlUtils.getAndroidXmlAttribute(metaDataTag, "value");
            if (KEY_XPOSED_MODULE.equals(metaDataName)) {
                hasModuleAttr = true;
                if (metaDataValue == null) {
                    AndroidXmlUtils.setAndroidXmlAttribute(metaDataTag, "value", "true");
                }
            } else if (KEY_XPOSED_DESCRIPTION.equals(metaDataName)) {
                hasDescriptionAttr = true;
                if (metaDataValue == null) {
                    AndroidXmlUtils.setAndroidXmlAttribute(metaDataTag, "value", DEFAULT_DESCRIPTION);
                }
            } else if (KEY_XPOSED_MIN_VERSION.equals(metaDataName)) {
                hasVersionAttr = true;
                if (metaDataValue == null) {
                    AndroidXmlUtils.setAndroidXmlAttribute(metaDataTag, "value", "2");
                }
            }
        }

        if (!hasModuleAttr) createMetaDataTag(applicationTag, KEY_XPOSED_MODULE, "true");
        if (!hasDescriptionAttr) createMetaDataTag(applicationTag, KEY_XPOSED_DESCRIPTION, DEFAULT_DESCRIPTION);
        if (!hasVersionAttr) createMetaDataTag(applicationTag, KEY_XPOSED_MIN_VERSION, "2");

        return true;
    }
}
