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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XposedBridgeDependencyManager {
    private static final Logger LOG = Logger.getInstance(XposedBridgeDependencyManager.class);
    private static final Map<Module, XposedBridgeDependencyManager> sInstanceCache = new HashMap<Module, XposedBridgeDependencyManager>(1);

    private final Module mModule;

    private XposedBridgeDependencyManager(@NotNull Module module) {
        mModule = module;
    }

    private boolean addGradleBuildDependency() {
        GradleBuildFile buildFile = GradleBuildFile.get(mModule);
        if (buildFile == null) {
            LOG.error("Could not find build.gradle");
            return false;
        }

        Dependency xposedDependency = new Dependency(Dependency.Scope.PROVIDED, Dependency.Type.FILES, "libs/" + XposedConsts.XPOSED_BRIDGE_LIB_NAME);
        List<BuildFileStatement> dependencies = buildFile.getDependencies();
        if (!dependencies.contains(xposedDependency)) {
            dependencies.add(xposedDependency);
            buildFile.setValue(BuildFileKey.DEPENDENCIES, dependencies);
            LOG.debug("Added XposedBridge to build.gradle dependency list");
        }

        return true;
    }

    private boolean addIdeaLibraryDependency(@NotNull AndroidFacet androidFacet) {
        Project project = mModule.getProject();
        BaseLibrariesConfigurable configurable = BaseLibrariesConfigurable.getInstance(project, LibraryTablesRegistrar.PROJECT_LEVEL);
        LibrariesModifiableModel model = configurable.getModelProvider().getModifiableModel();
        VirtualFile libsDir = AndroidRootUtil.getLibsDir(androidFacet);
        if (libsDir == null) {
            LOG.error("Could not find libs directory");
            return false;
        }

        VirtualFile xposedLib = libsDir.findChild(XposedConsts.XPOSED_BRIDGE_LIB_NAME);
        if (xposedLib == null) {
            LOG.error("Could not find XposedBridge JAR");
            return false;
        }

        Library.ModifiableModel libraryModel = model.createLibrary("Xposed").getModifiableModel();
        libraryModel.addJarDirectory(xposedLib, true);
        libraryModel.commit();
        model.commit();
        return true;
    }

    public boolean ensureXposedBridgeDependency() {
        AndroidFacet androidFacet = AndroidFacet.getInstance(mModule);
        if (androidFacet == null) {
            LOG.error("No Android facet found in module");
            return false;
        }

        if (androidFacet.isGradleProject()) {
            return addGradleBuildDependency();
        } else {
            return addIdeaLibraryDependency(androidFacet);
        }
    }

    public static XposedBridgeDependencyManager getInstance(@NotNull Module module) {
        XposedBridgeDependencyManager instance = sInstanceCache.get(module);
        if (instance == null) {
            instance = new XposedBridgeDependencyManager(module);
            sInstanceCache.put(module, instance);
        }
        return instance;
    }
}
