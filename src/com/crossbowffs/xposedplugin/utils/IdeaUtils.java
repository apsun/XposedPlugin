package com.crossbowffs.xposedplugin.utils;

import com.android.tools.idea.gradle.GradleSyncState;
import com.android.tools.idea.gradle.project.GradleProjectImporter;
import com.android.tools.idea.gradle.project.GradleSyncListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.util.Consumer;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class IdeaUtils {
    @Nullable
    public static Module getModule(@NotNull Project project, @NotNull VirtualFile file) {
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        return fileIndex.getModuleForFile(file);
    }

    @Nullable
    public static Module getModule(@NotNull PsiFileSystemItem item) {
        Project project = item.getProject();
        VirtualFile file = item.getVirtualFile();
        return getModule(project, file);
    }

    @Nullable
    public static Module getModule(@NotNull PsiElement psiElement) {
        Project project = psiElement.getProject();
        PsiFile psiFile = psiElement.getContainingFile();
        VirtualFile file = psiFile.getVirtualFile();
        return getModule(project, file);
    }

    @Nullable
    public static VirtualFile getModuleDir(@NotNull Module module) {
        String moduleFilePath = module.getModuleFilePath();
        String moduleDirPath = new File(moduleFilePath).getParent();
        if (moduleDirPath == null) {
            return null;
        }
        moduleDirPath = FileUtil.toSystemIndependentName(moduleDirPath);
        return LocalFileSystem.getInstance().findFileByPath(moduleDirPath);
    }

    public static void syncGradleIfRequired(@NotNull Module module, @NotNull final Consumer<Boolean> callback) {
        Project project = module.getProject();
        if (GradleSyncState.getInstance(project).isSyncNeeded() == ThreeState.NO) {
            callback.consume(true);
        }

        GradleProjectImporter.getInstance().requestProjectSync(project, true, true, new GradleSyncListener.Adapter() {
            @Override
            public void syncSucceeded(@NotNull Project project) {
                callback.consume(true);
            }

            @Override
            public void syncFailed(@NotNull Project project, @NotNull String s) {
                callback.consume(false);
            }

            @Override
            public void syncSkipped(@NotNull Project project) {
                callback.consume(false);
            }
        });
    }
}
