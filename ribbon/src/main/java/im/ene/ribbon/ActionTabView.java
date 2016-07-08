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

import android.animation.ArgbEvaluator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import java.lang.ref.SoftReference;

abstract class ActionTabView extends View {
  public static final float ALPHA_MAX = 255f;
  private ActionTab action;
  protected final int rippleColor;
  private boolean expanded;
  protected final Paint textPaint;
  protected boolean textDirty;
  protected final ArgbEvaluator evaluator;
  private final BadgeProvider provider;
  protected Drawable badge;
  protected Drawable icon;

  public ActionTabView(final BottomNavigationView parent, final boolean expanded,
      final MenuParser.Menu menu) {
    super(parent.getContext());
    this.evaluator = new ArgbEvaluator();
    this.rippleColor = menu.getRippleColor();
    this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.textDirty = true;
    this.expanded = expanded;
    this.provider = parent.getBadgeProvider();
  }

  void setAction(ActionTab item) {
    final Drawable drawable =
        ContextCompat.getDrawable(getContext(), R.drawable.ribbon_ripple_selector);
    drawable.mutate();
    MiscUtils.setDrawableColor(drawable, rippleColor);
    this.setBackground(drawable);

    this.action = item;
    this.setId(item.getItemId());
    this.setEnabled(item.isEnabled());
    invalidateBadge();
  }

  void invalidateBadge() {
    Drawable drawable = provider.getBadgeDrawable(getId());

    if (badge != drawable) {
      if (badge != null) {
        badge.setCallback(null);
        badge = null;
      }

      badge = drawable;

      if (badge != null) {
        badge.setCallback(this);
        if (badge instanceof Badge && null == getParent()) {
          ((Badge) badge).setIsAnimating(false);
        }
      }

      if (null != getParent()) {
        invalidate();
      }
    }
  }

  @Override public void invalidateDrawable(@NonNull final Drawable drawable) {
    super.invalidateDrawable(drawable);

    if (drawable == badge) {
      invalidate();
    }
  }

  protected abstract void onStatusChanged(final boolean expanded, final int size,
      final boolean animate);

  public final ActionTab getAction() {
    return action;
  }

  public final boolean isExpanded() {
    return expanded;
  }

  public void setExpanded(final boolean expanded, int newSize, boolean animate) {
    if (this.expanded != expanded) {
      this.expanded = expanded;
      onStatusChanged(expanded, newSize, animate);
    }
  }

  protected final void drawBadge(final Canvas canvas) {
    if (badge != null && icon != null) {
      Rect iconBounds = icon.getBounds();
      badge.setBounds(iconBounds.right - badge.getIntrinsicWidth(), iconBounds.top,
          iconBounds.right, iconBounds.top + badge.getIntrinsicHeight());
      badge.draw(canvas);
    }
  }

  public void setTypeface(final SoftReference<Typeface> typeface) {
    if (null != typeface) {
      Typeface tf = typeface.get();
      if (null != tf) {
        textPaint.setTypeface(tf);
      } else {
        textPaint.setTypeface(Typeface.DEFAULT);
      }

      textDirty = true;
      requestLayout();
    }
  }
}
