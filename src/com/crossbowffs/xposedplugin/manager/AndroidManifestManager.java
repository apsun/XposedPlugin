package com.crossbowffs.xposedplugin.manager;

import com.crossbowffs.xposedplugin.utils.AndroidXmlUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class AndroidManifestManager {
    private static final Logger LOG = Logger.getInstance(AndroidManifestManager.class);
    @NonNls private static final String KEY_XPOSED_MODULE = "xposedmodule";
    @NonNls private static final String KEY_XPOSED_DESCRIPTION = "xposeddescription";
    @NonNls private static final String KEY_XPOSED_MIN_VERSION = "xposedminversion";

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
        Boolean moduleAttrValue = null;
        String descriptionAttrValue = null;
        Integer versionAttrValue = null;
        for (XmlTag metaDataTag : metaDataTags) {
            String metaDataName = AndroidXmlUtils.getAndroidXmlAttribute(metaDataTag, "name");
            String metaDataValue = AndroidXmlUtils.getAndroidXmlAttribute(metaDataTag, "value");
            if (KEY_XPOSED_MODULE.equals(metaDataName)) {
                if (metaDataValue == null) {
                    AndroidXmlUtils.setAndroidXmlAttribute(metaDataTag, "value", "true");
                } else {
                    moduleAttrValue = Boolean.parseBoolean(metaDataValue);
                }
            } else if (KEY_XPOSED_DESCRIPTION.equals(metaDataName)) {
                descriptionAttrValue = metaDataValue;
            } else if (KEY_XPOSED_MIN_VERSION.equals(metaDataName)) {
                if (metaDataValue == null) {
                    AndroidXmlUtils.setAndroidXmlAttribute(metaDataTag, "value", "2");
                } else {
                    versionAttrValue = Integer.parseInt(metaDataValue);
                }
            }
        }

        if (moduleAttrValue == null) createMetaDataTag(applicationTag, KEY_XPOSED_MODULE, "true");
        if (descriptionAttrValue == null) createMetaDataTag(applicationTag, KEY_XPOSED_DESCRIPTION, "Your Xposed module description");
        if (versionAttrValue == null) createMetaDataTag(applicationTag, KEY_XPOSED_MIN_VERSION, "2");

        return true;
    }
}
