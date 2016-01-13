package com.crossbowffs.xposedplugin.templates;

import com.crossbowffs.xposedplugin.utils.XposedIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.ide.highlighter.JavaFileType;
import org.jetbrains.annotations.NonNls;

public class XposedHookTemplateFactory implements FileTemplateGroupDescriptorFactory {
    @NonNls
    public static final String[] TEMPLATES = {
        XposedHookTemplates.PACKAGE,
        XposedHookTemplates.ZYGOTE,
        XposedHookTemplates.RESOURCES
    };

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Xposed Hooks", XposedIcons.XPOSED_16x16);
        for (String template : TEMPLATES) {
            group.addTemplate(new FileTemplateDescriptor(template, JavaFileType.INSTANCE.getIcon()));
        }
        return group;
    }
}
