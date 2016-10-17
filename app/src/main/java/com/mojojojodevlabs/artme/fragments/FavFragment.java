package com.mojojojodevlabs.artme.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mojojojodevlabs.artme.R;
import com.mojojojodevlabs.artme.Supplier;
import com.mojojojodevlabs.artme.adapters.ListAdapter;
import com.mojojojodevlabs.artme.data.WallData;

import java.util.ArrayList;

public class FavFragment extends Fragment {

    ArrayList<WallData> walls;
    RecyclerView recycler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recycler = (RecyclerView) inflater.inflate(R.layout.fragment_fav, container, false);

        walls = new ArrayList<>();
        for (WallData data : ((Supplier) getContext().getApplicationContext()).getWallpapers(getContext())) {
            if (data.favorite) walls.add(data);
        }

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        ListAdapter adapter = new ListAdapter(getActivity(), walls);
        adapter.setLayoutMode(ListAdapter.LAYOUT_MODE_COMPLEX);
        recycler.setAdapter(adapter);

        return recycler;
    }
}
