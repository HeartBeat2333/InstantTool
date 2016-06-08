package com.heartbeat.instanttool.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    private Integer[] imageSource = {R.drawable.i2, R.drawable.i3,
                                    R.drawable.i4, R.drawable.i5, R.drawable.i6,
                                    R.drawable.i7, R.drawable.i8, R.drawable.i9};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new ImageAdapter(this);
        List<Integer> items = new ArrayList<>();
        Collections.addAll(items, imageSource);
        mAdapter.setItems(items);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
    }

}
