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

import java.io.File;
import java.util.Set;

/**
 * author: zhoulei date: 15/7/13.
 */
public interface Storage {
  /**
   * put content to storage, if key exist, will override
   *
   * @param key the key of content
   * @param content content to store
   * @return whether put content success
   */
  boolean put(String key, String content);

  /**
   * append content to key content, if key not exist, equal put(key, content)
   *
   * @param key the key of to append
   * @param content content used to append
   * @return whether append content success
   */
  boolean append(String key, String content);

  /**
   * delete key and the content of key, if key not exist, return false
   *
   * @param key the key of content
   * @return whether delete key success
   */
  boolean delete(String key);

  /**
   * rename srcKey to targetKey, like File.renameTo()
   *
   * @param srcKey srcKey
   * @param targetKey targetKey
   * @return is rename success
   */
  boolean rename(String srcKey, String targetKey);

  /**
   * clear storage
   *
   */
  void clear();

  /**
   * if has key, return true, else return false
   *
   * @param key the key of content
   * @return boolean
   */
  boolean has(String key);

  /**
   * get the content of key, if content is null, return null
   *
   * @param key the key of content
   * @return boolean
   */
  String get(String key);

  /**
   * return the byte size of the content of key
   *
   * @param key the key of content
   * @return the byte size of the content
   */
  long size(String key);

  /**
   * get all keys in this storage
   *
   * @return Set<String>
   */
  Set<String> getKeys();

  /**
   * get the version of storage
   *
   * @return version
   */
  int getVersion();

  /**
   * get storage dir
   *
   * @return dir
   */
  File getStorageDir();

  /**
   * backup the content of key
   *
   * @param key the key
   */
  void backup(String key);

  /**
   * remove the backup of the content to key
   *
   * @param key the key
   */
  boolean removeBackup(String key);

  /**
   * recover the content of key
   *
   * @param key the key
   */
  void recover(String key);
}
