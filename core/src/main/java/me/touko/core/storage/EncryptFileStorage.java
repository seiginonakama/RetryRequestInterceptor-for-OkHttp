package me.touko.core.storage;

import android.text.TextUtils;

/**
 * author: zhoulei date: 15/7/13.
 */
public abstract class EncryptFileStorage extends FileStorage {
  private static final String LINE_FEED = "\n";
  private final static String STORAGE_FILE_PREFIX = "d_file_storage_";

  public EncryptFileStorage(String storageDirPath, int storageVersion) throws Exception {
    super(storageDirPath, storageVersion);
  }

  @Override
  protected String getStorageFilePrefix() {
    return STORAGE_FILE_PREFIX;
  }

  @Override
  public boolean append(String key, String content) {
    try {
      if (has(key)) {
        return super.append(key, LINE_FEED + encrypt(content));
      } else {
        return super.append(key, encrypt(content));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public String get(String key) {
    String content = super.get(key);
    if (TextUtils.isEmpty(content)) {
      return null;
    }
    String[] encryptContents = content.split(LINE_FEED);
    if (encryptContents.length <= 0) {
      return null;
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (String encryptContent : encryptContents) {
      if (encryptContent.isEmpty()) {
        continue;
      }
      try {
        stringBuilder.append(decrypt(encryptContent));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return stringBuilder.toString();
  }

  protected abstract String encrypt(String origin);

  protected abstract String decrypt(String encrypted);
}