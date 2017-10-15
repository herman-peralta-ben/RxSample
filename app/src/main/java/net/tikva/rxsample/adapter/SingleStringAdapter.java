package net.tikva.rxsample.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SingleStringAdapter extends RecyclerView.Adapter<SingleStringAdapter.StringViewHolder> {

    protected final int itemLayout;
    protected List<String> items;
    protected ListClickListener listClickListener;

    /**
     * @param itemLayout - root must be TextView
     */
    public SingleStringAdapter(@LayoutRes final int itemLayout, ListClickListener listClickListener) {
        this.itemLayout = itemLayout;
        this.listClickListener = listClickListener;
        this.items = new ArrayList<>();
    }

    @Override
    public StringViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new StringViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StringViewHolder holder, int position) {
        ((TextView) holder.itemView).setText(items.get(position));
    }

    public void setItems(List<String> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void addItem(String item) {
        items.add(item);
    }

    public void addAll(@NonNull List<String> items) {
        this.items.addAll(items);
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface ListClickListener {
        void onItemClicked(final int position);
    }

    class StringViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        StringViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listClickListener != null) {
                int position = getAdapterPosition();
                listClickListener.onItemClicked(position);
            }
        }
    }
}
