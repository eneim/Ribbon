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

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

public class BadgeDrawable extends Drawable {
  private static final String TAG = BadgeDrawable.class.getSimpleName();
  public static final float FADE_DURATION = 100f;
  public static final float ALPHA_MAX = 255f;
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private long startTimeMillis;
  private boolean animating;
  private final int size;

  public BadgeDrawable(final int color, final int size) {
    super();
    this.paint.setColor(color);
    this.size = size;
    this.animating = true;
    this.startTimeMillis = 0;
  }

  public void setIsAnimating(boolean animating) {
    this.animating = animating;
  }

  @Override public void draw(final Canvas canvas) {
    if (!animating) {
      paint.setAlpha((int) ALPHA_MAX);
      drawInternal(canvas);
    } else {
      if (startTimeMillis == 0) {
        startTimeMillis = SystemClock.uptimeMillis();
      }

      float normalized = (SystemClock.uptimeMillis() - startTimeMillis) / FADE_DURATION;
      if (normalized >= 1f) {
        animating = false;
        paint.setAlpha((int) ALPHA_MAX);
        drawInternal(canvas);
      } else {
        int partialAlpha = (int) (ALPHA_MAX * normalized);
        setAlpha(partialAlpha);
        drawInternal(canvas);
      }
    }
  }

  private void drawInternal(final Canvas canvas) {
    Rect bounds = getBounds();
    final int w = bounds.width();
    final int h = bounds.height();
    canvas.drawCircle(bounds.centerX() + w / 2, bounds.centerY() - h / 2, w / 2, paint);
  }

  @Override public void setAlpha(final int alpha) {
    paint.setAlpha(alpha);
    invalidateSelf();
  }

  @Override public int getAlpha() {
    return paint.getAlpha();
  }

  @Override public boolean isStateful() {
    return false;
  }

  @Override public void setColorFilter(final ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
    invalidateSelf();
  }

  @Override public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override public int getIntrinsicHeight() {
    return size;
  }

  @Override public int getIntrinsicWidth() {
    return size;
  }
}
