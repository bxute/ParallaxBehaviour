/*
 * Developer email: hiankit.work@gmail.com
 * GitHub: https://github.com/bxute
 */

package com.bxute.custombottomsheet;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MonthViewHolder> {
  public RecyclerViewAdapter() {
  }

  @NonNull
  @Override
  public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.month_view, viewGroup, false);
    return new MonthViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MonthViewHolder monthViewHolder, int i) {
    monthViewHolder.setIndex(i);
  }

  @Override
  public int getItemCount() {
    return 4800;
  }

  class MonthViewHolder extends RecyclerView.ViewHolder {
    TextView textView;

    public MonthViewHolder(@NonNull View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.text);
    }

    public void setIndex(int index) {
      String text = Utils.getMonthYearFormatFromItemIndex(index);
      textView.setText(text);
    }
  }
}
