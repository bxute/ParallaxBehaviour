/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import java.util.Calendar;

public class CalendarPage {
  public static final int WEEK_ROWS = 6;
  public static final int DAY_COLUMNS = 7;
  private static String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
  private int maxWeeks;
  //Row x Day
  private int mMonth;
  private int mYear;

  private int[] prevMonthMatrix = new int[DAY_COLUMNS];
  private int[][] thisMonthMatrix = new int[WEEK_ROWS][DAY_COLUMNS];
  private int[] nextMonthMatrix = new int[DAY_COLUMNS];
  private int firstDayColumn;
  private int lastDayColumn;

  public void prepareForFor(int year, int month) {
    this.mMonth = month;
    this.mYear = year;
    //fill in current month matrix
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    int row = 0;
    firstDayColumn = cal.get(Calendar.DAY_OF_WEEK);
    int myMonth = cal.get(Calendar.MONTH);
    while (myMonth == cal.get(Calendar.MONTH)) {
      int date = cal.get(Calendar.DATE);
      int day = cal.get(Calendar.DAY_OF_WEEK);
      lastDayColumn = day;
      thisMonthMatrix[row][day - 1] = date;
      maxWeeks = row + 1;
      row = row + ((day == 7) ? 1 : 0);
      cal.add(Calendar.DAY_OF_MONTH, 1);
    }
    //fill in prev month matrix
    fillInPrevMonthMatrix();
    //fill in next month matrix
    fillInNextMonthMatrix();

    //print all the calender
    printCalendar(month, year);
  }

  private void fillInPrevMonthMatrix() {
    //If current month is first month of the year, then decrease year and month
    int prevMonth;
    int prevYear;
    if (this.mMonth == 0) {
      prevYear = mYear - 1;
      prevMonth = 11;
    } else {
      prevYear = mYear;
      prevMonth = mMonth - 1;
    }
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, prevYear);
    cal.set(Calendar.MONTH, prevMonth);
    int lastDate = cal.getActualMaximum(Calendar.DATE);
    cal.set(Calendar.DATE, lastDate);
    int dayColumn = cal.get(Calendar.DAY_OF_WEEK);
    while (dayColumn > 0) {
      prevMonthMatrix[dayColumn - 1] = lastDate;
      dayColumn--;
      lastDate--;
    }
  }

  private void fillInNextMonthMatrix() {
    int nextMonth;
    int nextYear;
    //If current month is last month of the year, then increase year and set month = 0
    if (this.mMonth == 11) {
      nextYear = mYear + 1;
      nextMonth = 0;
    } else {
      nextYear = mYear;
      nextMonth = mMonth + 1;
    }
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, nextYear);
    cal.set(Calendar.MONTH, nextMonth);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    int dayColumn = cal.get(Calendar.DAY_OF_WEEK);
    while (dayColumn < 7) {
      dayColumn = cal.get(Calendar.DAY_OF_WEEK);
      nextMonthMatrix[dayColumn - 1] = cal.get(Calendar.DATE);
      cal.add(Calendar.DAY_OF_MONTH, 1);
    }
  }

  private void printCalendar(int month, int year) {
    String header = "| Sun | Mon | Tue | Wed | Thu | Fri | Sat |";
    String divider = "-------------------------------------------";
    System.out.println(divider);
    System.out.println(year + "," + months[month]);
    System.out.println(divider);
    System.out.println(header);
    System.out.println(divider);
    for (int i = 0; i < maxWeeks; i++) {
      for (int j = 0; j < DAY_COLUMNS; j++) {
        if (thisMonthMatrix[i][j] == 0) {
          if (i == 0) {
            System.out.printf("||%2d  " + (j == 6 ? "|" : ""), prevMonthMatrix[j]);
          } else if (i == (WEEK_ROWS - 2) || i == (WEEK_ROWS - 1)) {
            System.out.printf("| %2d  " + (j == 6 ? "|" : ""), nextMonthMatrix[j]);
          }
        } else {
          System.out.printf("| %2d  " + (j == 6 ? "|" : ""), thisMonthMatrix[i][j]);
        }
      }
      System.out.println("");
      System.out.println(divider);
    }
  }

  public int getNumberOfWeeks() {
    return maxWeeks;
  }

  public int getMonth() {
    return mMonth;
  }

  public int getYear() {
    return mYear;
  }
}
