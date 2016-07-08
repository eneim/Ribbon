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

package im.ene.ribbon.demo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import im.ene.ribbon.BadgeProvider;
import im.ene.ribbon.BottomNavigationView;
import java.util.HashMap;

/**
 * Created by eneim on 7/8/16.
 */
public class NumberedBadgeProvider extends BadgeProvider {

  private static final String TAG = "CustomBadgeProvider";

  private final HashMap<Integer, Integer> countMap = new HashMap<>();

  public NumberedBadgeProvider(final BottomNavigationView navigation) {
    super(navigation);
  }

  public int getBadgeTextCount(@IdRes final int itemId) {
    if (countMap.containsKey(itemId)) {
      return countMap.get(itemId);
    }
    return 0;
  }

  public void show(@IdRes final int itemId, int count) {
    countMap.put(itemId, count);
    super.show(itemId);
  }

  @Override public void hide(@IdRes final int itemId) {
    countMap.remove(itemId);
    super.hide(itemId);
  }

  @Override protected Drawable newDrawable(@IdRes final int itemId, final int preferredColor) {
    int count = 1;
    if (countMap.containsKey(itemId)) {
      count = countMap.get(itemId);
    }
    return new BadgeDrawable(preferredColor, count);
  }

  public static final class BadgeDrawable extends Drawable {
    final Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint textPaint =
        new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    private String text;
    private float top;
    private float left;

    public BadgeDrawable(final int color, final int count) {
      super();
      this.text = String.valueOf(count);
      badgePaint.setColor(color);
      backgroundPaint.setColor(Color.WHITE);
      textPaint.setColor(Color.WHITE);
      textPaint.setTextSize(27);
    }

    @Override public void draw(final Canvas canvas) {
      final Rect rect = getBounds();
      canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() / 2, backgroundPaint);
      canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width() / 2 - 2, badgePaint);
      canvas.drawText(text, 0, text.length(), left, top, textPaint);
    }

    @Override protected void onBoundsChange(final Rect bounds) {
      super.onBoundsChange(bounds);
      bounds.offset(bounds.width() / 2, -bounds.height() / 3);
      Paint.FontMetrics metrics = textPaint.getFontMetrics();
      float size = textPaint.measureText(text, 0, text.length());
      left = (bounds.left + (bounds.width() - size) / 2);
      top = bounds.centerY() - (metrics.ascent / 2) - metrics.descent / 2;
    }

    @Override public void setAlpha(final int alpha) {
      badgePaint.setAlpha(alpha);
      textPaint.setAlpha(alpha);
    }

    @Override public void setColorFilter(final ColorFilter colorFilter) {
    }

    @Override public int getOpacity() {
      return PixelFormat.TRANSLUCENT;
    }

    @Override public int getIntrinsicWidth() {
      return 48;
    }

    @Override public int getIntrinsicHeight() {
      return 48;
    }
  }
}
