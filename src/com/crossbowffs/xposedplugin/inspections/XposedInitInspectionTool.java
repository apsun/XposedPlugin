package com.crossbowffs.xposedplugin.inspections;

import com.crossbowffs.xposedplugin.manager.XposedInitAssetManager;
import com.crossbowffs.xposedplugin.utils.IdeaUtils;
import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XposedInitInspectionTool extends AbstractBaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance(XposedInitInspectionTool.class);

    private static boolean classExtends(PsiClass aClass, String name) {
        PsiClass[] superTypes = aClass.getSupers();
        for (PsiClass superType : superTypes) {
            if (name.equals(superType.getQualifiedName())) {
                return true;
            }
            if (classExtends(superType, name)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkClass(@NotNull PsiClass aClass, @NotNull InspectionManager manager, boolean isOnTheFly) {
        Module module = IdeaUtils.getModule(aClass);
        if (module == null) {
            return null;
        }

        if (!classExtends(aClass, "de.robv.android.xposed.IXposedMod")) {
            return null;
        }

        String className = aClass.getQualifiedName();
        assert className != null;
        LOG.debug("Found Xposed hook class: " + className);

        XposedInitAssetManager initManager = XposedInitAssetManager.getInstance(module);
        boolean isDeclared = initManager.isXposedHookDeclared(className);
        LOG.debug("Is declared in xposed_init: " + isDeclared);

        if (isDeclared) {
            return null;
        }

        PsiElement psiElement = aClass.getNameIdentifier();
        if (psiElement == null) {
            psiElement = aClass;
        }

        return new ProblemDescriptor[] {
            manager.createProblemDescriptor(psiElement,
                "Xposed hook not declared in xposed_init",
                new XposedInitQuickFix(module, className),
                ProblemHighlightType.WEAK_WARNING,
                isOnTheFly)
        };
    }

    private static class XposedInitQuickFix extends LocalQuickFixBase {
        private Module mModule;
        private String mClassName;

        private XposedInitQuickFix(Module module, String className) {
            super("Add class to xposed_init", "Xposed");
            mModule = module;
            mClassName = className;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
            XposedInitAssetManager initManager = XposedInitAssetManager.getInstance(mModule);
            initManager.declareXposedHook(mClassName);
        }
    }
}
