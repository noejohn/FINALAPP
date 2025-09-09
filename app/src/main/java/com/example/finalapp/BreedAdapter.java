package com.example.finalapp;

import com.example.finalapp.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BreedAdapter extends RecyclerView.Adapter<BreedAdapter.VH> {

    public interface Callback {
        void onEdit(Breed b);
        void onDelete(Breed b);
    }

    private List<Breed> items = new ArrayList<>();
    private Callback cb;

    public BreedAdapter(List<Breed> items, Callback cb) {
        this.items = items;
        this.cb = cb;
    }

    public void update(List<Breed> data) {
        this.items = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_breed, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Breed b = items.get(position);
        holder.tvName.setText(b.getName());
        holder.tvType.setText(b.getType());
        holder.btnEdit.setOnClickListener(v -> cb.onEdit(b));
        holder.btnDelete.setOnClickListener(v -> cb.onDelete(b));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvType;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBreedName);
            tvType = itemView.findViewById(R.id.tvBreedType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
