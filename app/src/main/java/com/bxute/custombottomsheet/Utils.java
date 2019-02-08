/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import java.util.Locale;

public class Utils {
  public static final int MAX_YEAR = 2200;
  public static final int MIN_YEAR = 1800;

  public static int itemIndexFromMonthYear(int month, int year) {
    if (year < MIN_YEAR || year > MAX_YEAR)
      return -1;
    if (month < 0 || month > 11)
      return -1;
    return (year - MIN_YEAR) * 12 + month;
  }

  public static String getMonthYearFormatFromItemIndex(int itemIndex) {
    int monthInYear = Utils.monthInYearFromItemIndex(itemIndex);
    int year = Utils.yearFromItemIndex(itemIndex);
    return String.format(Locale.US, "DD/%d/%d", monthInYear, year);
  }

  public static int monthInYearFromItemIndex(int index) {
    int yearIndex = yearIndexFromItemIndexIndex(index);
    int monthInYear = index - (yearIndex * 12);
    return monthInYear;
  }

  public static int yearFromItemIndex(int itemIndex) {
    int year = yearIndexFromItemIndexIndex(itemIndex);
    return 1800 + year;
  }

  public static int yearIndexFromItemIndexIndex(int index) {
    int year = index / 12;
    return year;
  }
}
