/*
 * Copyright 2017 Eric A. Snell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ealvalog.base;

import ealvalog.Marker;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Basic Marker implementation
 * <p>
 * Created by Eric A. Snell on 2/28/17.
 */
@SuppressWarnings("WeakerAccess")
public class MarkerImpl implements Marker {
  private final String name;
  private final List<Marker> children;

  public MarkerImpl(@NotNull final String name) {
    this.name = name;
    children = new CopyOnWriteArrayList<>();
  }

  @NotNull public String getName() {
    return name;
  }

  public boolean addChild(@NotNull final Marker child) {
    return children.add(child);
  }

  public boolean removeChild(@NotNull final Marker child) {
    return children.remove(child);
  }

  public boolean contains(@NotNull final Marker marker) {
    return this.equals(marker) || children.contains(marker);
  }

  public boolean contains(@NotNull final String markerName) {
    if (this.name.equals(markerName)) {
      return true;
    }
    for (Marker marker : children) {
      if (marker.contains(markerName)) {
        return true;
      }
    }
    return false;
  }

  public Iterator<Marker> iterator() {
    return children.iterator();
  }
}
