package com.crossbowffs.xposedplugin.manager;

import com.android.tools.idea.gradle.parser.BuildFileKey;
import com.android.tools.idea.gradle.parser.BuildFileStatement;
import com.android.tools.idea.gradle.parser.Dependency;
import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.crossbowffs.xposedplugin.utils.XposedConsts;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.projectRoot.BaseLibrariesConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidRootUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class XposedBridgeDependencyManager {
    private static final Logger LOG = Logger.getInstance(XposedBridgeDependencyManager.class);

    private static boolean addGradleBuildDependency(@NotNull Module module) {
        GradleBuildFile buildFile = GradleBuildFile.get(module);
        if (buildFile == null) {
            LOG.error("Could not find build.gradle");
            return false;
        }

        Dependency xposedDependency = new Dependency(Dependency.Scope.PROVIDED, Dependency.Type.FILES, "libs/" + XposedConsts.XPOSED_BRIDGE_LIB_NAME);
        List<BuildFileStatement> dependencies = buildFile.getDependencies();
        if (!dependencies.contains(xposedDependency)) {
            dependencies.add(xposedDependency);
            buildFile.setValue(BuildFileKey.DEPENDENCIES, dependencies);
        }
        return true;
    }

    private static boolean addIdeaLibraryDependency(@NotNull Module module, @NotNull AndroidFacet androidFacet) {
        Project project = module.getProject();
        BaseLibrariesConfigurable configurable = BaseLibrariesConfigurable.getInstance(project, LibraryTablesRegistrar.PROJECT_LEVEL);
        LibrariesModifiableModel model = configurable.getModelProvider().getModifiableModel();
        VirtualFile libsDir = AndroidRootUtil.getLibsDir(androidFacet);
        if (libsDir == null) {
            LOG.error("Could not find libs directory");
            return false;
        }

        VirtualFile xposedLib = libsDir.findChild(XposedConsts.XPOSED_BRIDGE_LIB_NAME);
        if (xposedLib == null) {
            LOG.error("Could not find Xposed bridge API library JAR");
            return false;
        }

        Library.ModifiableModel libraryModel = model.createLibrary("Xposed").getModifiableModel();
        libraryModel.addJarDirectory(xposedLib, true);
        libraryModel.commit();
        model.commit();
        return true;
    }

    public static boolean ensureXposedBridgeDependency(@NotNull Module module) {
        AndroidFacet androidFacet = AndroidFacet.getInstance(module);
        if (androidFacet == null) {
            LOG.error("No Android facet found in module");
            return false;
        }

        if (androidFacet.isGradleProject()) {
            return addGradleBuildDependency(module);
        } else {
            return addIdeaLibraryDependency(module, androidFacet);
        }
    }
}
