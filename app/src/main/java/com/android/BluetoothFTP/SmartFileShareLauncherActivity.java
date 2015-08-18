package com.android.BluetoothFTP;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by siva.r on 7/6/2015.
 */
public class SmartFileShareLauncherActivity extends AppCompatActivity  implements NavigationDrawerFragment.NavigationDrawerCallbacks{

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }
}
