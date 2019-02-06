/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


public class ParallaxCalendarView extends FrameLayout {
  public static final float ROW_HEIGHT_DP = 56;
  private LinearLayout bottomSheet;
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
    bottomSheet = view.findViewById(R.id.bottom_sheet);
  }
}
