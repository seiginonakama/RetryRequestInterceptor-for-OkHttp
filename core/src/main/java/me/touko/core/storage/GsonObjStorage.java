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

import com.google.gson.Gson;

import me.touko.core.utils.GsonFactory;

/**
 * author: zhou date: 2015/12/23.
 */
public class GsonObjStorage<T> extends ObjStorage<T> {
  private static final Gson gson = GsonFactory.getGson();

  public GsonObjStorage(Class<T> tClass, Storage storage) {
    super(tClass, storage);
  }

  @Override
  protected String convertToString(T t) {
    return gson.toJson(t);
  }

  @Override
  protected T convertFromString(String str, Class<T> tClass) {
    return gson.fromJson(str, tClass);
  }
}

