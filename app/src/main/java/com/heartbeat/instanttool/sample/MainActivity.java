package com.heartbeat.instanttool.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MotionEvent;
import android.widget.Toast;

import com.heartbeat.instanttool.InstantButton;
import com.heartbeat.instanttool.InstantToolLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private InstantToolLayout mInstantTool;
    private ImageAdapter mAdapter;

    private Integer[] imageSource = {R.drawable.i2, R.drawable.i3,
                                    R.drawable.i4, R.drawable.i5, R.drawable.i6,
                                    R.drawable.i7, R.drawable.i8, R.drawable.i9};

    private Integer[] btnIconSource = {R.drawable.icon_like, R.drawable.icon_unlike,
            R.drawable.icon_build, R.drawable.icon_more};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

        mInstantTool.setButtonListener(new InstantToolLayout.InstantToolButtonListener() {
            @Override
            public void onShowButton(InstantButton button, int slotIndex) {
                button.setIconResource(getResources().getDrawable(btnIconSource[slotIndex]));
                switch (slotIndex) {
                    case 0 :
                        button.setText("like");
                        break;
                    case 1 :
                        button.setText("unlike");
                        break;
                    case 2 :
                        button.setText("build");
                        break;
                    case 3 :
                        button.setText("more");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onItemButtonChecked(InstantButton button, int slotIndex) {
                Toast.makeText(MainActivity.this
                        , button.getText() + " image id ： " + mInstantTool.getTag().toString()
                        , Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void initData() {
        mAdapter = new ImageAdapter(this);
        List<Integer> items = new ArrayList<>();
        Collections.addAll(items, imageSource);
        mAdapter.setItems(items);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setLongListner(new ImageAdapter.OnItemLongSelectedListener() {
            @Override
            public void onItemLongSelected(MotionEvent e, int rid) {
                mInstantTool.showButtons(e);
                mInstantTool.setTag(rid);
            }
        });
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mInstantTool = (InstantToolLayout) findViewById(R.id.il_tool);
    }

}
