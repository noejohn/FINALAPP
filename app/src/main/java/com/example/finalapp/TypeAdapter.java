package com.example.finalapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.VH> {

    public interface Callback {
        void onEdit(Type t);
        void onDelete(Type t);
    }

    private List<Type> items = new ArrayList<>();
    private Callback cb;

    public TypeAdapter(List<Type> items, Callback cb) {
        this.items = items;
        this.cb = cb;
    }

    public void update(List<Type> data) {
        this.items = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Type t = items.get(position);
        holder.tvName.setText(t.getName());
        holder.btnEdit.setOnClickListener(v -> { if (cb != null) cb.onEdit(t); });
        holder.btnDelete.setOnClickListener(v -> { if (cb != null) cb.onDelete(t); });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTypeName);
            btnEdit = itemView.findViewById(R.id.btnEditType);
            btnDelete = itemView.findViewById(R.id.btnDeleteType);
        }
    }
}
