/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;

public class RecyclerViewTestPage extends AppCompatActivity {

  private RecyclerView recyclerView;
  private LinearLayoutManager linearLayoutManager;
  private RecyclerViewAdapter recyclerViewAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_recycler_view_test_page);

    recyclerView = findViewById(R.id.recyclerView);
    linearLayoutManager = new LinearLayoutManager(this);
    recyclerViewAdapter = new RecyclerViewAdapter();
    PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    int todaysPosition = Utils.itemIndexFromMonthYear(1, 2019);
    linearLayoutManager.scrollToPosition(todaysPosition);
    recyclerView.setLayoutManager(linearLayoutManager);
    pagerSnapHelper.attachToRecyclerView(recyclerView);
    recyclerView.setAdapter(recyclerViewAdapter);
  }
}
