package me.touko.okhttp.storage;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.touko.okhttp.utils.FileUtil;
import me.touko.okhttp.utils.IOUtils;
import me.touko.okhttp.utils.NumberParseUtils;

/**
 * author: zhoulei date: 15/7/13.
 */
public class FileStorage implements Storage {
  private File storageDir;
  private ConfigFile configFile;

  private final Map<String, Object> fileLockMap = new HashMap<>();
  private final Map<String, Object> backupFileLockMap = new HashMap<>();

  private final int storageVersion;

  private final static String STORAGE_CONFIG_FILE_NAME = "private_file_storage_config";
  private final static String STORAGE_CONFIG_ITEM_VERSION = "version";
  private final static String STORAGE_FILE_PREFIX = "file_storage_";
  private final static String BACKUP_STORAGE_FILE_PREFIX = "backup_file_storage_";

  public FileStorage(String storageDirPath, int version) {
    storageDir = new File(storageDirPath);
    configFile = new ConfigFile(storageDir + File.separator + STORAGE_CONFIG_FILE_NAME);
    storageVersion = version;

    if (!storageDir.exists()) {
      if (!storageDir.mkdirs()) {
        throw new IllegalStateException(
            FileStorage.class.getSimpleName() + ":can't find or create storage dir");
      }
    }
    if (!checkStorageVersionValid()) {
      FileUtil.clearPath(storageDirPath);
      configFile.putConfigValue(STORAGE_CONFIG_ITEM_VERSION, String.valueOf(version));
    }
  }

  private boolean checkStorageVersionValid() {
    String versionString = configFile.getConfigValue(STORAGE_CONFIG_ITEM_VERSION);
    if (TextUtils.isEmpty(versionString)) {
      return false;
    }
    int version = NumberParseUtils.parseInt(versionString, -1);
    return version == storageVersion;
  }

  protected String getStorageFilePrefix() {
    return STORAGE_FILE_PREFIX;
  }

  protected String getBackupStorageFilePrefix() {
    return BACKUP_STORAGE_FILE_PREFIX;
  }

  @Override
  public boolean put(String key, String content) {
    delete(key);
    return append(key, content);
  }

  @Override
  public boolean append(String key, String content) {
    File file = getStorageFile(key);
    if (file == null) {
      return false;
    }

    synchronized (getFileLock(key)) {
      if (!file.exists()) {
        try {
          if (file.createNewFile()) {
            IOUtils.writeString(content, file);
            return true;
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        try {
          IOUtils.writeString(content, file, true);
          return true;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return false;
  }

  @Override
  public boolean delete(String key) {
    File file = getStorageFile(key);
    if (file == null || !file.exists()) {
      return true;
    }
    synchronized (getFileLock(key)) {
      return file.delete();
    }
  }

  @Override
  public boolean rename(String srcKey, String targetKey) {
    File file = getStorageFile(srcKey);
    if (file == null || !file.exists()) {
      return true;
    }
    File targetFile = getStorageFile(targetKey);
    synchronized (getFileLock(srcKey)) {
      synchronized (getFileLock(targetKey)) {
        targetFile.deleteOnExit();
        return file.renameTo(targetFile);
      }
    }
  }

  @Override
  public void clear() {
    File[] files = storageDir.listFiles();
    if (files.length <= 0) {
      return;
    }
    for (File file : files) {
      String fileName = file.getName();
      if (fileName.startsWith(getStorageFilePrefix())) {
        String key = fileName.substring(getStorageFilePrefix().length());
        synchronized (getFileLock(key)) {
          file.delete();
        }
      }
    }
  }

  @Override
  public boolean has(final String key) {
    File file = getStorageFile(key);
    synchronized (getFileLock(key)) {
      return file != null && file.exists();
    }
  }

  @Override
  public String get(String key) {
    File storageFile = getStorageFile(key);
    synchronized (getFileLock(key)) {
      if (storageFile == null || !storageFile.exists()) {
        return null;
      }
      try {
        return IOUtils.readString(storageFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }
  }

  @Override
  public long size(String key) {
    File file = getStorageFile(key);
    synchronized (getFileLock(key)) {
      if (file != null && file.exists()) {
        return file.length();
      }
    }
    return 0;
  }

  @Override
  public Set<String> getKeys() {
    Set<String> keys = new HashSet<>();
    File[] files = storageDir.listFiles();
    if (files.length <= 0) {
      return keys;
    }
    for (File file : files) {
      String fileName = file.getName();
      if (fileName.startsWith(getStorageFilePrefix())) {
        keys.add(fileName.substring(getStorageFilePrefix().length()));
      }
    }
    return keys;
  }

  @Override
  public int getVersion() {
    return storageVersion;
  }

  @Override
  public File getStorageDir() {
    return storageDir;
  }

  @Override
  public void backup(String key) {
    if(!has(key)) {
      return;
    }
    File srcFile = getStorageFile(key);
    File backupFile = getBackupStorageFile(key);
    synchronized (getBackupFileLock(key)) {
      synchronized (getFileLock(key)) {
        FileUtil.copyFile(srcFile, backupFile);
      }
    }
  }

  @Override
  public boolean removeBackup(String key) {
    File backupFile = getBackupStorageFile(key);
    synchronized (getBackupFileLock(key)) {
      return backupFile.delete();
    }
  }

  @Override
  public void recover(String key) {
    File dstFile = getStorageFile(key);
    File backupFile = getBackupStorageFile(key);
    synchronized (getBackupFileLock(key)) {
      synchronized (getFileLock(key)) {
        FileUtil.copyFile(backupFile, dstFile);
      }
    }
  }

  protected File getStorageFile(final String key) {
    if (TextUtils.isEmpty(key)) {
      return null;
    }
    return new File(storageDir + File.separator + getStorageFilePrefix() + key);
  }

  protected File getBackupStorageFile(final String key) {
    if (TextUtils.isEmpty(key)) {
      return null;
    }
    return new File(storageDir + File.separator + getBackupStorageFilePrefix() + key);
  }

  private Object getFileLock(String key) {
    synchronized (fileLockMap) {
      if (!fileLockMap.containsKey(key)) {
        final Object fileLock = new Object();
        fileLockMap.put(key, fileLock);
        return fileLock;
      }
      return fileLockMap.get(key);
    }
  }

  private Object getBackupFileLock(String key) {
    synchronized (backupFileLockMap) {
      if (!backupFileLockMap.containsKey(key)) {
        final Object fileLock = new Object();
        backupFileLockMap.put(key, fileLock);
        return fileLock;
      }
      return backupFileLockMap.get(key);
    }
  }
}
