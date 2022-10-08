package com.samvad;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class LearnActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        MyListData[] myListData = new MyListData[] {
                new MyListData("Arm"),
                new MyListData("Table"),
                new MyListData("Fly"),
                new MyListData("Home"),
                new MyListData("Strong"),
        };

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_learn);
        MyListAdapter adapter = new MyListAdapter(myListData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}