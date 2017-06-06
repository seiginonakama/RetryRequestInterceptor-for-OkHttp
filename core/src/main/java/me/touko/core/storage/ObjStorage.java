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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import me.touko.core.utils.CollectionUtils;

/**
 * author: zhou date: 2015/12/23.
 */
public abstract class ObjStorage<T> {
  public static int GET_ALL_ITEM_COUNT = -1;

  private final Class<T> storeClass;

  private final Storage storage;
  private static final String LINE_FEED = "\n";

  public ObjStorage(Class<T> tClass, Storage storage) {
    if (storage == null) {
      throw new IllegalArgumentException("storage can't be null");
    }
    this.storeClass = tClass;
    this.storage = storage;
  }

  /**
   * put t to storage, if key exist, will override
   *
   * @param key the key
   * @param t the obj
   * @return whether put success
   */
  public boolean put(String key, T t) {
    return storage.delete(key) && append(key, t);
  }

  /**
   * put Collection<T> to storage, if key exist, will override
   *
   * @param key the key
   * @param tCollection the collection of obj
   * @return whether put success
   */
  public boolean put(String key, Collection<T> tCollection) {
    return storage.delete(key) && append(key, tCollection);
  }

  /**
   * append the content of key with t
   *
   * @param key the key
   * @param t the content to append
   * @return whether append success
   */
  public boolean append(String key, T t) {
    if (t == null) {
      return false;
    }

    if (!has(key)) {
      return storage.append(key, convertToString(t));
    } else {
      return storage.append(key, LINE_FEED + convertToString(t));
    }
  }

  /**
   * append the content of key with t
   *
   * @param key the key
   * @param tCollection the content to append
   * @return whether append success
   */
  public boolean append(String key, Collection<T> tCollection) {
    for(T t : tCollection) {
      if(t == null) {
        continue;
      }
      if(!append(key, t)) {
        return false;
      }
    }
    return true;
  }

  /**
   * delete the content of key
   *
   * @param key the key
   * @return whether delete success
   */
  public boolean delete(String key) {
    return storage.delete(key);
  }

  /**
   * rename srcKey to targetKey, same as File.renameTo()
   *
   * @param srcKey the key to rename
   * @param targetKey the key to rename to
   * @return is rename success
   */
  public boolean rename(String srcKey, String targetKey) {
    return storage.rename(srcKey, targetKey);
  }

  /**
   * clear storage, remove all keys and content
   */
  public void clear() {
    storage.clear();
  }

  /**
   * whether has the key
   *
   * @param key the key
   * @return whether has the key
   */
  public boolean has(String key) {
    return storage.has(key);
  }

  /**
   * get objects of key
   *
   * @param key the key
   * @return List
   */
  public List<T> get(String key) {
    List<T> items = new ArrayList<>();
    get(key, items);
    return items;
  }

  /**
   * get objects of key
   *
   * @param key the key
   * @param container the container to receive data
   */
  public void get(String key, Collection<T> container) {
    get(key, container, GET_ALL_ITEM_COUNT);
  }

  /**
   * get objects of key
   *
   * @param key the key
   * @param container the container to receive data
   * @param limit the max item count to get, if limit < 0, get all item
   */
  public void get(String key, Collection<T> container, int limit) {
    if(limit == 0) {
      return;
    }
    String content = storage.get(key);
    if (TextUtils.isEmpty(content)) {
      return;
    }
    String[] itemJsons = content.split(LINE_FEED);
    if (itemJsons.length <= 0) {
      return;
    }
    int count = 0;
    for (String itemJson : itemJsons) {
      try {
        T item = convertFromString(itemJson, storeClass);
        container.add(item);
        count ++;
        if(limit > 0 && count >= limit) {
          break;
        }
      } catch (Throwable t) {
        // catch json format exception etc..
      }
    }
  }

  /**
   * get first object of key
   *
   * @param key the key
   * @return obj
   */
  public T getFirst(String key) {
    List<T> container = new ArrayList<>();
    get(key, container, 1);
    if(container.size() > 0) {
      return container.get(0);
    } else {
      return null;
    }
  }

  /**
   * get objects count of key
   *
   * @param key the key
   * @return objects count
   */
  public int length(String key) {
    List<T> items = new ArrayList<>();
    get(key, items);
    if (CollectionUtils.isEmpty(items)) {
      return 0;
    } else {
      return items.size();
    }
  }

  /**
   * get the byte size of content with the key
   *
   * @param key the key
   * @return byte size of the content with key
   */
  public long size(String key) {
    return storage.size(key);
  }

  /**
   * return all keys in this storage
   *
   * @return keys
   */
  public Set<String> getKeys() {
    return storage.getKeys();
  }

  /**
   * return the storage version
   *
   * @return version
   */
  public int getVersion() {
    return storage.getVersion();
  }

  /**
   * get storage
   *
   * @return Storage
   */
  public Storage getStorage() {
    return storage;
  }

  /**
   * the method for subclass to implement, for convert obj to string to store
   *
   * @param t obj
   * @return the string convert from obj
   */
  abstract protected String convertToString(T t);

  /**
   * the method for subclass to implement, for convert string to obj to get obj from storage
   *
   * @param str the obj string
   * @param tClass the obj class
   * @return the obj convert from string
   */
  abstract protected T convertFromString(String str, Class<T> tClass) throws Throwable;
}
