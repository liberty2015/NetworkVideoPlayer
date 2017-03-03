package com.accurme.networkvideoplayer.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.accurme.networkvideoplayer.R;
import com.accurme.networkvideoplayer.widget.SimpleDrawerLayout;

public class LayoutTestActivity extends AppCompatActivity {

    SimpleDrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_test);
        drawerLayout=(SimpleDrawerLayout) findViewById(R.id.drawerLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.action_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.openDrawer:{
                drawerLayout.openDrawer();
            }
            break;
        }
        return true;
    }
}
