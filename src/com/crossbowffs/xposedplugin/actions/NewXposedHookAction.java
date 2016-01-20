package com.crossbowffs.xposedplugin.actions;

import com.crossbowffs.xposedplugin.manager.XposedInitAssetManager;
import com.crossbowffs.xposedplugin.templates.XposedHookTemplates;
import com.crossbowffs.xposedplugin.utils.IdeaUtils;
import com.crossbowffs.xposedplugin.utils.XposedIcons;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class NewXposedHookAction extends JavaCreateTemplateInPackageAction<PsiClass> implements DumbAware {
    public NewXposedHookAction() {
        super("Xposed Hook", "Create a new Xposed hook", XposedIcons.XPOSED_16x16, true);
    }

    @Nullable
    @Override
    protected PsiElement getNavigationElement(@NotNull PsiClass createdClass) {
        return createdClass.getLBrace();
    }

    @Nullable
    @Override
    protected PsiClass doCreate(PsiDirectory directory, String className, String templateName) throws IncorrectOperationException {
        return JavaDirectoryService.getInstance().createClass(directory, className, templateName);
    }

    @Override
    protected void postProcess(PsiClass createdElement, String templateName, Map<String, String> customProperties) {
        Module module = IdeaUtils.getModule(createdElement);
        XposedInitAssetManager.getInstance(module).declareXposedHook(createdElement.getQualifiedName());
        moveCaretToMethodBody(createdElement.getAllMethods()[0]);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
        builder
            .setTitle("New Xposed hook")
            .addKind("Package hook", XposedIcons.XPOSED_16x16, XposedHookTemplates.PACKAGE)
            .addKind("Zygote hook", XposedIcons.XPOSED_16x16, XposedHookTemplates.ZYGOTE)
            .addKind("Resources hook", XposedIcons.XPOSED_16x16, XposedHookTemplates.RESOURCES)
            .setValidator(new InputValidatorEx() {
                @Nullable
                @Override
                public String getErrorText(String s) {
                    if (!AndroidUtils.isIdentifier(s)) {
                        return "Invalid class name";
                    }
                    return null;
                }

                @Override
                public boolean checkInput(String s) {
                    return true;
                }

                @Override
                public boolean canClose(String s) {
                    return getErrorText(s) == null;
                }
            });
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String newName, String templateName) {
        return "Xposed Hook";
    }

    private static void moveCaretToMethodBody(PsiMethod method) {
        Project project = method.getProject();
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            VirtualFile virtualFile = method.getContainingFile().getVirtualFile();
            if (virtualFile != null && FileDocumentManager.getInstance().getDocument(virtualFile) == editor.getDocument()) {
                PsiElement body = method.getBody().getLBrace();
                if (body != null) {
                    editor.getCaretModel().moveToOffset(body.getTextRange().getEndOffset() + 1);
                }
            }
        }
    }
}
