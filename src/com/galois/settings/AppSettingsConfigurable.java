package com.galois.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppSettingsConfigurable implements Configurable {

  private AppSettingsComponent mySettingsComponent;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "Galois Autocompleter";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return mySettingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    mySettingsComponent = new AppSettingsComponent();
    return mySettingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    AppSettingsState settings = AppSettingsState.getInstance();
    return !mySettingsComponent.getGaloisApiUrl().equals(settings.galoisApiUrl);
  }

  @Override
  public void apply() {
    AppSettingsState settings = AppSettingsState.getInstance();
    settings.galoisApiUrl = mySettingsComponent.getGaloisApiUrl();
  }

  @Override
  public void reset() {
    AppSettingsState settings = AppSettingsState.getInstance();
    mySettingsComponent.setGaloisApiUrl(settings.galoisApiUrl);
  }

  @Override
  public void disposeUIResources() {
    mySettingsComponent = null;
  }

}
