/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


public class ParallaxCalendarView extends FrameLayout {
  public static final int ROWS = 5;
  public static final float ROW_HEIGHT_DP = 56;
  public static final float MINIMUM_OFFSET = ROW_HEIGHT_DP;
  public static final float MAXIMUM_OFFSET = ROWS * ROW_HEIGHT_DP;

  private LinearLayout bottomContentSheet;
  private LinearLayout calendarView;
  private Context mContext;

  public ParallaxCalendarView(@NonNull Context context) {
    this(context, null);
  }

  public ParallaxCalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ParallaxCalendarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    this.mContext = context;
    View view = LayoutInflater.from(context).inflate(R.layout.parallax_calendarview_layout, this);
    bottomContentSheet = view.findViewById(R.id.bottom_content_sheet);
    calendarView = view.findViewById(R.id.calender_view);
    setupBehaviours();
  }

  private void setupBehaviours() {
    int rowHeight = toPx(mContext, ROW_HEIGHT_DP);
    int mMinOffset = toPx(mContext, MINIMUM_OFFSET);
    int mMaxOffset = toPx(mContext, MAXIMUM_OFFSET);

    //Behaviour for CalendarView to enable parallax depending on bottomSheet position
    ParallaxViewBehaviour parallaxViewBehaviour = new ParallaxViewBehaviour();
    //snap to first row by default.
    //we can change row with snapTo(int) method of ParallaxViewBehaviour.class
    parallaxViewBehaviour.configureBehaviour(rowHeight, 2, mMinOffset, mMaxOffset);

    //Behaviour for bottom sheet to enable scroll
    CustomBottomSheetBehaviour bottomSheetBehaviour = new CustomBottomSheetBehaviour();
    bottomSheetBehaviour.configureBehaviour(mMinOffset, mMaxOffset, rowHeight);

    CoordinatorLayout.LayoutParams calendarViewLayoutParams = (CoordinatorLayout.LayoutParams) calendarView.getLayoutParams();
    calendarViewLayoutParams.setBehavior(parallaxViewBehaviour);

    CoordinatorLayout.LayoutParams bottomSheetLayoutParams = (CoordinatorLayout.LayoutParams) bottomContentSheet.getLayoutParams();
    bottomSheetLayoutParams.setBehavior(bottomSheetBehaviour);

    calendarView.setLayoutParams(calendarViewLayoutParams);
    bottomContentSheet.setLayoutParams(bottomSheetLayoutParams);
    calendarView.requestLayout();
    bottomContentSheet.requestLayout();
  }

  private int toPx(Context context, float dp) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
     dp,
     context.getResources().getDisplayMetrics());
  }
}
