package com.bxute.custombottomsheet;

import org.junit.Assert;
import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
  @Test
  public void test_itemIndexConversion() {
    int itemIndex = 0;
    for (int i = 0; i < 4800; i++) {
      itemIndex = i;
      int monthInYear = Utils.monthInYearFromItemIndex(itemIndex);
      int year = Utils.yearFromItemIndex(itemIndex);
      System.out.println(itemIndex + ": DD/" + monthInYear + "/" + year);
      int _itemIndex = Utils.itemIndexFromMonthYear(monthInYear, year);
      Assert.assertEquals(itemIndex, _itemIndex);
    }
  }

  @Test
  public void test_itemIndexFromMonthYear() {
    int monthInYear = 1;
    int year = 2019;
    int _itemIndex = Utils.itemIndexFromMonthYear(monthInYear, year);
    System.out.println(_itemIndex);
  }

  @Test
  public void testDateGenerator() {
    new CalendarPage().prepareForFor(2020, 4);
  }
}