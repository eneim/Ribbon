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

import android.content.Context;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import proguard.annotation.Keep;
import proguard.annotation.KeepClassMembers;

import static android.util.Log.DEBUG;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static im.ene.ribbon.MiscUtils.log;

/**
 * Created by alessandro on 4/10/16 at 2:12 PM.
 * Project: Material-BottomNavigation
 */
@Keep @KeepClassMembers public class TabletBehavior
    extends VerticalScrollingBehavior<BottomNavigationView> {
  private static final String TAG = TabletBehavior.class.getSimpleName();
  private int topInset;
  private boolean enabled;
  private int width;
  private boolean translucentStatus;

  public TabletBehavior(final Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setLayoutValues(final int bottomNavWidth, final int topInset,
      final boolean translucentStatus) {
    log(TAG, INFO, "setLayoutValues(bottomNavWidth: %d, topInset: %d)", bottomNavWidth, topInset);
    this.translucentStatus = translucentStatus;
    log(TAG, DEBUG, "translucentStatus: %b", translucentStatus);
    this.width = bottomNavWidth;
    this.topInset = topInset;
    this.enabled = true;
  }

  @Override
  public boolean layoutDependsOn(final CoordinatorLayout parent, final BottomNavigationView child,
      final View dependency) {
    return AppBarLayout.class.isInstance(dependency);
  }

  @Override public boolean onDependentViewChanged(final CoordinatorLayout parent,
      final BottomNavigationView child, final View dependency) {
    log(TAG, DEBUG, "top: %d, topInset: %d", dependency.getTop(), topInset);

    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();

    final int top = Build.VERSION.SDK_INT > 19 ? topInset : translucentStatus ? topInset : 0;

    params.topMargin =
        Math.max(dependency.getTop() + dependency.getHeight() - top, translucentStatus ? 0 : -top);

    log(TAG, VERBOSE, "dependency.top: %d, dependency.height: %d", dependency.getTop(),
        dependency.getHeight());

    if (translucentStatus) {
      if (params.topMargin < top) {
        child.setPadding(0, top - params.topMargin, 0, 0);
      } else {
        child.setPadding(0, 0, 0, 0);
      }
    }

    child.requestLayout();
    return true;
  }

  @Override
  public void onDependentViewRemoved(final CoordinatorLayout parent, final BottomNavigationView child,
      final View dependency) {
    super.onDependentViewRemoved(parent, child, dependency);
  }

  @Override
  public boolean onLayoutChild(final CoordinatorLayout parent, final BottomNavigationView child,
      final int layoutDirection) {
    return super.onLayoutChild(parent, child, layoutDirection);
  }

  @Override public void onNestedVerticalOverScroll(final CoordinatorLayout coordinatorLayout,
      final BottomNavigationView child, @ScrollDirection final int direction,
      final int currentOverScroll, final int totalOverScroll) {

  }

  @Override public void onDirectionNestedPreScroll(final CoordinatorLayout coordinatorLayout,
      final BottomNavigationView child, final View target, final int dx, final int dy,
      final int[] consumed, @ScrollDirection final int scrollDirection) {

  }

  @Override protected boolean onNestedDirectionFling(final CoordinatorLayout coordinatorLayout,
      final BottomNavigationView child, final View target, final float velocityX, final float velocityY,
      @ScrollDirection final int scrollDirection) {
    return false;
  }
}
