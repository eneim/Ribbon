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

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import static android.view.WindowManager.LayoutParams;

final class MiscUtils {

  private MiscUtils() {
    throw new AssertionError("Illegal initialization!");
  }

  /**
   * Returns if the current theme has the translucent status bar enabled
   *
   * @param activity context
   * @return true if the current theme has the translucent status bar
   */
  static boolean hasTranslucentStatusBar(@Nullable final Activity activity) {
    return activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && //
        ((activity.getWindow().getAttributes().flags & LayoutParams.FLAG_TRANSLUCENT_STATUS)
            == LayoutParams.FLAG_TRANSLUCENT_STATUS);
  }

  /**
   * Returns true if the current theme has declared the botton navigation as translucent
   *
   * @param activity context
   * @return true if the activity has the translucent navigation enabled
   */
  static boolean hasTranslucentNavigationBar(@Nullable final Activity activity) {
    return activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
        ((activity.getWindow().getAttributes().flags & LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            == LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
  }

  /**
   * Returns the current theme defined color
   */
  static int getColor(Context context, @AttrRes int color) {
    TypedValue tv = new TypedValue();
    context.getTheme().resolveAttribute(color, tv, true);
    return tv.data;
  }

  static void setDrawableColor(@NonNull final Drawable drawable, final int color) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && drawable instanceof RippleDrawable) {
      ((RippleDrawable) drawable).setColor(ColorStateList.valueOf(color));
    } else {
      DrawableCompat.setTint(drawable, color);
    }
  }

  @SuppressLint("RtlHardcoded") static boolean isGravityLeft(final int gravity) {
    return gravity == Gravity.LEFT;
  }

  @SuppressLint("RtlHardcoded") static boolean isGravityRight(final int gravity) {
    return gravity == Gravity.RIGHT;
  }

  static boolean isGravityBottom(final int gravity) {
    return gravity == Gravity.BOTTOM;
  }

  protected static void switchColor(final BottomNavigationView navigation, final View v,
      final View backgroundOverlay, final ColorDrawable backgroundDrawable, final int newColor) {

    backgroundOverlay.clearAnimation();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Animator currentAnimator =
          (Animator) backgroundOverlay.getTag(R.id.bbn_backgroundOverlay_animator);
      if (null != currentAnimator) {
        currentAnimator.cancel();
      }
    }

    backgroundDrawable.setColor(newColor);
    backgroundOverlay.setVisibility(View.INVISIBLE);
    ViewCompat.setAlpha(backgroundOverlay, 1);
  }

  public static void log(final String tag, final int level, String message, Object... arguments) {
    if (BottomNavigationView.DEBUG) {
      Log.println(level, tag, String.format(message, arguments));
    }
  }

  @Nullable static Activity getActivity(@Nullable Context context) {
    if (context == null) {
      return null;
    } else if (context instanceof Activity) {
      return (Activity) context;
    } else if (context instanceof ContextWrapper) {
      return getActivity(((ContextWrapper) context).getBaseContext());
    }
    return null;
  }
}
