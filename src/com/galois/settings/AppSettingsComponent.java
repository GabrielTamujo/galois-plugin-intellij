package com.galois.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AppSettingsComponent {

  private final JPanel myMainPanel;
  private final JBTextField galoisApiUrl = new JBTextField();

  public AppSettingsComponent() {
    myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Galois Autocompleter API URL (Reload Required): "), galoisApiUrl, 1, false)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
  }

  public JPanel getPanel() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return galoisApiUrl;
  }

  @NotNull
  public String getGaloisApiUrl() {
    return galoisApiUrl.getText();
  }

  public void setGaloisApiUrl(@NotNull String newText) {
    galoisApiUrl.setText(newText);
  }

}
