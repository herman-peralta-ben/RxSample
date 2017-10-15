package net.tikva.rxsample.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.tikva.rxsample.R;
import net.tikva.rxsample.adapter.SingleStringAdapter;

import java.util.Arrays;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SingleStringAdapter.ListClickListener {

    @BindArray(R.array.list_main_samples)
    String[] list_main_samples;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    SingleStringAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        ButterKnife.bind(this);

        adapter = new SingleStringAdapter(R.layout.item_sample, this);
        adapter.setItems(Arrays.asList(list_main_samples));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClicked(int position) {
        switch (position) {
            case 0:
                startActivity(SimpleListActivity.getStartIntent(this));
                break;

            case 1:
                startActivity(TemperatureActivity.getStartIntent(this));
                break;
        }
    }
}
