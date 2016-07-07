/*
 * Copyright 2016 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alessandro Crugnola
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package im.ene.ribbon;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import java.util.HashSet;

public class BadgeProvider {
  private final BottomNavigationView navigation;
  private final HashSet<Integer> map = new HashSet<>();
  private final int badgeSize;

  public BadgeProvider(final BottomNavigationView navigation) {
    this.navigation = navigation;
    this.badgeSize =
        navigation.getContext().getResources().getDimensionPixelSize(R.dimen.bbn_badge_size);
  }

  protected Bundle save() {
    Bundle bundle = new Bundle();
    bundle.putSerializable("map", map);
    return bundle;
  }

  @SuppressWarnings("unchecked") public void restore(final Bundle bundle) {
    HashSet<Integer> set = (HashSet<Integer>) bundle.getSerializable("map");
    if (null != set) {
      map.addAll(set);
    }
  }

  /**
   * Returns if the menu item will require a badge
   *
   * @param itemId the menu item id
   * @return true if the menu item has to draw a badge
   */
  public boolean hasBadge(@IdRes final int itemId) {
    return map.contains(itemId);
  }

  Drawable getBadgeDrawable(@IdRes final int itemId) {
    if (map.contains(itemId)) {
      return newDrawable(itemId, navigation.menu.getBadgeColor());
    }
    return null;
  }

  @SuppressWarnings("unused")
  protected Drawable newDrawable(@IdRes final int itemId, final int preferredColor) {
    return new BadgeDrawable(preferredColor, badgeSize);
  }

  /**
   * Request to display a new badge over the passed menu item id
   *
   * @param itemId the menu item id
   */
  public void show(@IdRes final int itemId) {
    map.add(itemId);
    navigation.invalidateBadge(itemId);
  }

  /**
   * Remove the currently displayed badge
   *
   * @param itemId the menu item id
   */
  public void hide(@IdRes final int itemId) {
    if (map.remove(itemId)) {
      navigation.invalidateBadge(itemId);
    }
  }
}
