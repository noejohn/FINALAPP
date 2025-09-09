package com.example.finalapp;

import com.example.finalapp.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.VH> {
    public interface Callback {
        void onEdit(Pet pet);
        void onDelete(Pet pet);
    }

    private List<Pet> items;
    private List<Pet> fullList;
    private Callback cb;

    public PetAdapter(List<Pet> items, Callback cb) {
        this.items = new ArrayList<>(items);
        this.fullList = new ArrayList<>(items);
        this.cb = cb;
    }

    public void updateList(List<Pet> newList) {
        this.items = new ArrayList<>(newList);
        this.fullList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            items = new ArrayList<>(fullList);
        } else {
            String q = query.toLowerCase();
            List<Pet> filtered = new ArrayList<>();
            for (Pet p : fullList) {
                if (p.getName() != null && p.getName().toLowerCase().contains(q)) {
                    filtered.add(p);
                }
            }
            items = filtered;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Pet p = items.get(position);
        holder.tvName.setText(p.getName());
        holder.tvTypeBreed.setText((p.getType() == null ? "" : p.getType()) + " • " + (p.getBreed() == null ? "" : p.getBreed()));
        holder.tvPriceDob.setText("$" + p.getPrice() + "  |  " + (p.getDob() == null ? "" : p.getDob()));
        holder.tvGender.setText(p.getGender());
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            Picasso.get().load(p.getImageUrl()).into(holder.img);
        } else {
            holder.img.setImageResource(R.mipmap.ic_launcher);
        }

        holder.itemView.setOnClickListener(v -> {
            if (cb != null) cb.onEdit(p);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (cb != null) cb.onDelete(p);
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvTypeBreed, tvPriceDob, tvGender;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPet);
            tvName = itemView.findViewById(R.id.tvName);
            tvTypeBreed = itemView.findViewById(R.id.tvTypeBreed);
            tvPriceDob = itemView.findViewById(R.id.tvPriceDob);
            tvGender = itemView.findViewById(R.id.tvGender);
        }
    }
}
