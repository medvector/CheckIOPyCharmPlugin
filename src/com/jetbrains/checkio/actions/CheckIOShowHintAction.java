package com.jetbrains.checkio.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.jetbrains.checkio.CheckIOBundle;
import com.jetbrains.checkio.CheckIOConnector;
import com.jetbrains.checkio.CheckIOProjectComponent;
import com.jetbrains.checkio.CheckIOUtils;
import com.jetbrains.checkio.ui.CheckIOToolWindow;
import com.jetbrains.edu.courseFormat.Task;
import icons.InteractiveLearningIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class CheckIOShowHintAction extends CheckIOTaskAction {
  public static final String ACTION_ID = "CheckIOShowHintAction";
  public static final String SHORTCUT = "ctrl pressed 7";
  private static final Logger LOG = Logger.getInstance(CheckIOShowHintAction.class);

  public CheckIOShowHintAction() {
    super(CheckIOBundle.message("action.hint.message") +
          " (" +
          KeymapUtil.getShortcutText(new KeyboardShortcut(KeyStroke.getKeyStroke(SHORTCUT), null)) +
          ")",
          CheckIOBundle.message("action.hint.message"),
          InteractiveLearningIcons.ShowHint);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    final Project project = event.getProject();
    if (project != null) {
      final Task task = CheckIOUtils.getTaskFromSelectedEditor(project);
      if (task != null) {
        ProgressManager.getInstance().run(getDownloadHintsTask(project, task));
      }
      else {
        LOG.warn("Task is null");
      }
    }
    else {
      LOG.warn("Project is null");
    }
  }

  private static com.intellij.openapi.progress.Task getDownloadHintsTask(@NotNull final Project project, @NotNull final Task task) {
    return new com.intellij.openapi.progress.Task.Backgroundable(project, "Downloading hints") {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          final CheckIOToolWindow toolWindow = CheckIOProjectComponent.getInstance(project).getToolWindow();
          if (toolWindow.isHintsVisible()) {
            ApplicationManager.getApplication().invokeLater(() -> toolWindow.getHintPanel().showNewHint());
          }
          else {
            final ArrayList<String> hints = CheckIOConnector.getHints(project, task.getName());
            final String forumLink = CheckIOUtils.getForumLink(task, project);
            ApplicationManager.getApplication().invokeLater(() -> toolWindow.setAndShowHintPanel(forumLink, hints));
          }
        }
        catch (IOException e) {
          ApplicationManager.getApplication().invokeLater(() -> CheckIOUtils.showOperationResultPopUp(
            CheckIOBundle.message("project.generation.internet.connection.problems"),
            MessageType.WARNING.getPopupBackground(), project));

          LOG.warn(e.getMessage());
        }
      }
    };
  }
}
