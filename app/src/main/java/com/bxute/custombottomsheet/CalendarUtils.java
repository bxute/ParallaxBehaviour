/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import java.util.Calendar;

public class CalendarUtils {
  public static final String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
  public static void printDates() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR,2019);
    cal.set(Calendar.MONTH,0);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    int myMonth = cal.get(Calendar.MONTH);
    while (myMonth == cal.get(Calendar.MONTH)) {
      int date = cal.get(Calendar.DATE);
      int day = cal.get(Calendar.DAY_OF_WEEK);
      System.out.println(date +"|"+ days[day-1]);
      cal.add(Calendar.DAY_OF_MONTH, 1);
    }
  }
}
