/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class ParallaxViewBehaviour<V extends View> extends CoordinatorLayout.Behavior<V> {
  private float mMinOffset;
  private float mMaxOffset;
  private int mRowHeight;
  private int mSnapToRow;

  public ParallaxViewBehaviour() {
  }

  public ParallaxViewBehaviour(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void configureBehaviour(int rowHeight, int snapToRow, int minOffset, int maxOffset) {
    this.mRowHeight = rowHeight;
    snapToRow(snapToRow);
    this.mMinOffset = minOffset;
    this.mMaxOffset = maxOffset;
  }

  public void snapToRow(int row) {
    this.mSnapToRow = row;
  }

  @Override
  public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
    if (dependency.getId() == R.id.bottom_content_sheet) {
      return true;
    }
    return false;
  }

  @Override
  public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull View dependency) {
    //Get the current top of the dependency
    float currentTop = dependency.getTop();
    //Calculate the Offset Ratio in range of [0:1]
    float ratio = calculateRatio(currentTop);
    //set new Child top according to the shift distance
    //-ve for shifting up
    int childTop = -(int) (ratio * getShiftDistance());
    child.setTop(childTop);
    return super.onDependentViewChanged(parent, child, dependency);
  }

  /**
   * Calculates ratio in range of [0:1]
   * If sheet is in its MaxOffset (in collapsed state) position, ratio will be 0
   * If sheet is in its MinOffset (int expanded state) position, ration will be 1
   * And intermediate position will result in ratio in 0 to 1 e.g. 0.2, 0.3 etc.
   *
   * @param top: calculate parallax ratio given the top of view attached to this behaviour.
   * @return value [0:1]
   */
  private float calculateRatio(float top) {
    return (mMaxOffset - top) / (mMaxOffset - mMinOffset);
  }

  /**
   * Multiplier is the totalShift required for snapping to particular row.
   * For e.g. we need 0 shift for snapping to 0th index child, since it is already in final position
   * Similarly, we need to shift 4*rowHeight distance to snap the 5th row.
   * In general, we need rowIndex times rowHeight shift distance
   *
   * @return shift distance to perform the parallax.
   */
  private int getShiftDistance() {
    return (mSnapToRow * mRowHeight);
  }
}
