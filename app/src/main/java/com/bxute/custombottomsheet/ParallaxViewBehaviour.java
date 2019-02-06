/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import static com.bxute.custombottomsheet.ParallaxCalendarView.ROW_HEIGHT_DP;

public class ParallaxViewBehaviour<V extends View> extends CoordinatorLayout.Behavior<V> {
  private float mMinOffset;
  private float mMaxOffset;

  private int mSelectedIndex = 2;

  public ParallaxViewBehaviour() {
  }

  public ParallaxViewBehaviour(Context context, AttributeSet attrs) {
    super(context, attrs);
    mMinOffset = calculateRowHeight(context);
    mMaxOffset = 5 * mMinOffset;
  }

  private float calculateRowHeight(Context context) {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
     ROW_HEIGHT_DP,
     context.getResources().getDisplayMetrics());
  }

  private int getMultiplier() {
    return (int) (mSelectedIndex * mMinOffset);
  }

  @Override
  public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
    if (dependency.getId() == R.id.bottom_sheet) {
      return true;
    }
    return false;
  }

  @Override
  public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
    float currentTop = dependency.getTop();
    float ratio = calculateRatio(currentTop);
    int childTop = -(int) (ratio * getMultiplier());
    child.setTop(childTop);
    return super.onDependentViewChanged(parent, child, dependency);
  }

  private float calculateRatio(float top) {
    return (mMaxOffset - top) / (mMaxOffset - mMinOffset);
  }
}
