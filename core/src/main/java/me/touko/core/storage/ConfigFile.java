/*
 *  Copyright (C) 2017 seiginonakama (https://github.com/seiginonakama).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.touko.core.storage;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

import me.touko.core.utils.IOUtils;

/**
 * author: zhoulei date: 15/8/27.
 */
public class ConfigFile {
  private final static String CONFIG_KEY_VALUE_DIVIDER = ":";
  private final static String CONFIG_ITEM_DIVIDER = "\n";

  File configFile;

  public ConfigFile(String filePath) {
    this(new File(filePath));
  }

  public ConfigFile(File configFile) {
    this.configFile = configFile;
  }

  public synchronized String getConfigValue(final String configName) {
    if (TextUtils.isEmpty(configName)) {
      return null;
    }

    if (!configFile.exists()) {
      return null;
    }
    try {
      String configContent = IOUtils.readString(configFile);
      String[] configItems = configContent.split("\n");
      if (configItems.length <= 0) {
        return null;
      }
      for (String configItem : configItems) {
        ConfigItemModel configItemModel = ConfigItemModel.createFromString(configItem);
        if (configItemModel == null || TextUtils.isEmpty(configItemModel.name)) {
          continue;
        }
        if (configItemModel.name.equals(configName)) {
          return configItemModel.value;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public synchronized boolean putConfigValue(final String configName, final String value) {
    if (TextUtils.isEmpty(configName) || value == null) {
      return false;
    }

    if (!configFile.exists()) {
      try {
        if (!configFile.createNewFile()) {
          return false;
        }
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }

    if (getConfigValue(configName) != null) {
      if (!removeConfig(configName)) {
        return false;
      }
    }

    try {
      IOUtils.writeString((configFile.length() <= 0 ? "" : CONFIG_ITEM_DIVIDER) +
          new ConfigItemModel(configName, value).toString(), configFile, true);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public synchronized boolean removeConfig(String configName) {
    if (!configFile.exists()) {
      return false;
    }
    try {
      String configContent = IOUtils.readString(configFile);
      String[] configItems = configContent.split("\n");
      if (configItems.length <= 0) {
        return false;
      }
      StringBuilder restoreContent = new StringBuilder();
      for (String configItem : configItems) {
        ConfigItemModel configItemModel = ConfigItemModel.createFromString(configItem);
        if (configItemModel == null || TextUtils.isEmpty(configItemModel.name)) {
          continue;
        }
        if (!configItemModel.name.equals(configName)) {
          restoreContent.append(configItemModel.toString());
          if (!configItem.equals(configItems[configItems.length - 1])) {
            restoreContent.append(CONFIG_ITEM_DIVIDER);
          }
        }
      }
      if (configFile.delete()) {
        IOUtils.writeString(restoreContent.toString(), configFile, false);
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private static class ConfigItemModel {
    private String name;
    private String value;

    public ConfigItemModel(String name, String value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public String toString() {
      return name + CONFIG_KEY_VALUE_DIVIDER + value;
    }

    public static ConfigItemModel createFromString(String content) {
      if (TextUtils.isEmpty(content)) {
        return null;
      }
      String[] keyAndValue = content.split(CONFIG_KEY_VALUE_DIVIDER);
      if (keyAndValue.length < 1) {
        return null;
      }
      return new ConfigItemModel(keyAndValue[0], keyAndValue[1]);
    }
  }
}
