package com.example.mraxn.obuapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity
{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    GPSTracker gps;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
  private ViewPager mViewPager = null;
  private TabLayout tabLayout = null;
  private Context mContext = null;
  static FloatingActionButton fab = null;
  private ULClientAsyncTask ulClientAST = null;
  private DLClientAsyncTask dlClientAST = null;
  ImageButton sendButton = null;
  ImageButton cancelButton = null;
  static private int iAddedPerson = 0;
  File extStorageDir = null;
  Properties cfg;
  private String txJsonStr = null;
  public double latitude;
  public double longitude;
  public JSONObject txJson;

  static public Properties ReadProperties(File extStorageDir)
  {
    FileInputStream fileInStream = null;
    Properties cfg = new Properties();

    try
    {
      fileInStream = new FileInputStream(extStorageDir + "/cfg.ini");
      cfg.load(fileInStream);
    }
    catch (Exception e1)
    {
      cfg.setProperty("UL_HOST", "192.168.0.20");
      cfg.setProperty("DL_HOST", "192.168.0.20");
      cfg.setProperty("BNAME", "Acme");
      cfg.setProperty("BNo", "54112925");

      try
      {
        FileOutputStream fileOutputStream = new FileOutputStream(extStorageDir + "/cfg.ini");
        cfg.store(fileOutputStream, null);
      }
      catch (Exception e2)
      {
        System.err.println("Can't open file stream!");
      }
    }

    try
    {
      if (fileInStream != null)
        fileInStream.close();
    }
    catch (IOException e)
    {
      System.err.println("Can't close file stream!");
    }

    return cfg;
  }

  static public void SaveProperties(File extStorageDir, Properties cfg)
  {
    FileOutputStream fileOutputStream = null;

    try
    {
      fileOutputStream = new FileOutputStream(extStorageDir + "/cfg.ini");
      cfg.store(fileOutputStream, null);
    }
    catch (Exception e2)
    {
      System.err.println("Can't open file stream!");
    }

    try
    {
      if (fileOutputStream != null)
        fileOutputStream.close();
    }
    catch (IOException e)
    {
      System.err.println("Can't close file stream!");
    }
  }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      iAddedPerson = 0;

      extStorageDir =  getExternalFilesDir(null);

      cfg = ReadProperties(extStorageDir);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setupTabIcons();

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager)
        {
          @Override public void onTabSelected(TabLayout.Tab tab)
          {

            if (tab.getPosition() == 1)
              fab.show();
            else
            fab.hide();
          }
        });


      if(getSupportActionBar() != null)
      {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
      }

        fab = findViewById(R.id.fab);

        fab.setOnClickListener((View view) ->
        {
          int selTanId;

                mContext = getApplicationContext();
//                CardView cardView = (CardView) findViewById(R.id.pd_card);

                selTanId = tabLayout.getSelectedTabPosition();

                switch (selTanId)
                {
                    case 0:
                        break;
                    case 1:
                      LinearLayout linearLayout = mViewPager.getChildAt(2).findViewById(R.id.pd_main_linear_layout);
                      LayoutInflater inflater = LayoutInflater.from(mContext);
                      View inflatedLayout = inflater.inflate(R.layout.pd_card, linearLayout, false);
                      ((EditText) inflatedLayout.findViewById(R.id.pd_card_id)).setText(Integer.toString((new Random()).nextInt(900000000) + 10000000));
                      ((ImageView) inflatedLayout.findViewById(R.id.pd_card_img)).
                                                  setImageResource(R.drawable.passenger_pic);
                      inflatedLayout.setBackground(linearLayout.getChildAt(2).getBackground());
                      String[] fullNamesArr = getResources().getStringArray(R.array.full_names);

                      String newPersonStr;

                      iAddedPerson += 1;
                      try
                      {
                        newPersonStr = fullNamesArr[iAddedPerson];
                      }
                      catch(Exception e)
                      {
                        newPersonStr = "Joanne Smith " + iAddedPerson;
                      }

                      ((EditText) inflatedLayout.findViewById(R.id.pd_card_name)).setText(newPersonStr);
                      linearLayout.addView(inflatedLayout);

                      break;

                    case 2:
                        break;
                    case 3:
                        break;
                }

        });

        gps = new GPSTracker(MainActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation())
        {

          latitude  = gps.getLatitude();
          longitude = gps.getLongitude();


          final TextView textView = findViewById(R.id.gps_tv);
          textView.setText(String.format(Locale.US, "LON: %.05f,    LAT: %.05f", longitude, latitude));

          LocalBroadcastManager.getInstance(this).registerReceiver
          (
            new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                  latitude = intent.getDoubleExtra(GPSTracker.EXTRA_LATITUDE, 0);
                  longitude = intent.getDoubleExtra(GPSTracker.EXTRA_LONGITUDE, 0);

                  if (txJson != null)
                  {
                    try
                    {
                      txJson.getJSONObject("EventDetails").put("RSU_LAT", Double.toString(latitude));
                      txJson.getJSONObject("EventDetails").put("RSU_LON", Double.toString(longitude));
                    }
                    catch (JSONException e)
                    {
                      e.printStackTrace();
                    }
                    catch (ConcurrentModificationException ec)
                    {
                      System.err.println("Concurrency JSON write exception occurred!");
                    }
                  }

                  textView.setText(String.format(Locale.US, "LON: %.05f,    LAT: %.05f", longitude, latitude));
                }
            },
              new IntentFilter(GPSTracker.ACTION_LOCATION_BROADCAST)
            );
        }
        else
        {
          // can't get location
          // GPS or Network is not enabled
          // Ask user to enable GPS/network in settings
          gps.showSettingsAlert();
        }

        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener((View view) ->
        {
          //AcmeNotifications.SendNotification(this, "DSRC Transmit stopped!");
          //AcmeNotifications.SendToast(this, "DSRC Transmit stopped!");

          sendButton.setEnabled(true);
          sendButton.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));

          if (ulClientAST != null)
          {
            ulClientAST.cancel(true);
            ulClientAST = null;
          }
          if (dlClientAST != null)
          {
            dlClientAST.cancel(true);
            dlClientAST = null;
          }

        });

      sendButton = findViewById(R.id.send_button);
      sendButton.setOnClickListener((View view) ->
      {

        ulClientAST = new ULClientAsyncTask(MainActivity.this, cfg.getProperty("UL_HOST"));
        dlClientAST = new DLClientAsyncTask(MainActivity.this, cfg.getProperty("DL_HOST"));
        view.setEnabled(false);
        view.setBackgroundColor(Color.GRAY);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss", Locale.CANADA);
        String tstamp = dateFormat.format(new Date());
        String vid = "000000001";
        String lpn = ((EditText) mViewPager.getChildAt(3).findViewById(R.id.lpn_et)).getText().toString();

        if (lpn.equalsIgnoreCase("random"))
        {
          lpn = Integer.toString((new Random()).nextInt(90000000) + 1000000);
        }

        String model = ((EditText) mViewPager.getChildAt(3).findViewById(R.id.model_et)).getText().toString();
        String origin = ((EditText) mViewPager.getChildAt(3).findViewById(R.id.origin_et)).getText().toString();
        String dest = ((EditText) mViewPager.getChildAt(3).findViewById(R.id.dest_et)).getText().toString();
//          String start_dt = ((EditText) mViewPager.getChildAt(3).findViewById(R.id.start_et)).getText().toString();

        String bname = ((EditText) mViewPager.getChildAt(4).findViewById(R.id.bname_et)).getText().toString();
        String bn = ((EditText) mViewPager.getChildAt(4).findViewById(R.id.bn_text_et)).getText().toString();

        cfg.setProperty("BNAME", bname);
        cfg.setProperty("BNo", bn);

        SaveProperties(extStorageDir, cfg);

        String tt;
        if (((CheckBox) mViewPager.getChildAt(4).findViewById(R.id.tt_cb)).isChecked())
        {
          tt = "true";
        }
        else
        {
          tt = "false";
        }

        String cert = ((EditText) mViewPager.getChildAt(4).findViewById(R.id.cn_et)).getText().toString();
        String cid = ((EditText) mViewPager.getChildAt(4).findViewById(R.id.cid_et)).getText().toString();
        String cargo = ((EditText) mViewPager.getChildAt(4).findViewById(R.id.cargo_desc_et)).getText().toString();
        String n_items = ((EditText) mViewPager.getChildAt(4).findViewById(R.id.nitems_et)).getText().toString();

        txJsonStr =
            "{" +
              "\"VehicleDetails\": " +
              "{" +
                "\"VID\": \""    + vid   + "\"," +
                "\"LP\": \""     + lpn   + "\"," +
                "\"Model\": \""  + model + "\"" +
              "}," +
              "\"TripDetails\": " +
              "{" +
                "\"Origin\": \""      + origin   + "\"," +
                "\"Destination\": \"" + dest     + "\"," +
                "\"TripStart\": \""   + tstamp   + "\"" +
              "}," +
                "\"PersonalDetails\": " +
                "[";


        LinearLayout linearLayout = mViewPager.getChildAt(2).findViewById(R.id.pd_main_linear_layout);

        for (int i = 0; i < linearLayout.getChildCount(); i++)
        {
          String fullName = ((EditText) linearLayout.getChildAt(i).findViewById(R.id.pd_card_name)).getText().toString();
          String pdCardId = ((EditText) linearLayout.getChildAt(i).findViewById(R.id.pd_card_id)).getText().toString();

          String driver;
          if (i == 0)
            driver = "true";
          else
          {
            driver = "true";
          }

          txJsonStr = txJsonStr +
              "{" +
                "\"Name\": \"" + fullName + "\"," +
                "\"ID\": " + pdCardId + "," +
                "\"Driver\": " + driver +
              "},";

        }

        txJsonStr = txJsonStr.substring(0, txJsonStr.length() - 1);

        txJsonStr += "]," +
            "\"CargoDetails\": " +
             "{" +
               "\"BName\": \""    + bname   + "\"," +
               "\"BNo\": "     + bn   + "," +
               "\"TrustedTrader\": "  + tt + "," +
               "\"CertNo\": "    + cert   + "," +
               "\"CarrierId\": "     + cid   + "," +
               "\"CargoDescr\": \""  + cargo + "\"," +
               "\"NoItems\": "  + n_items +
             "}," +
            "\"EventDetails\": " +
            "{" +
              "\"RSUID\": "    + "0"   + "," +
              "\"Timestamp\": \""     + tstamp   + "\"," +
              "\"RSU_LAT\": \""  + latitude + "\"," +
              "\"RSU_LON\": \""  + longitude + "\""  +
            "}, \"Reserved\":[]" +
        "}";

//          Log.i("txJsonStr", txJsonStr);

        try
        {
          txJson = new JSONObject(txJsonStr);
        }
        catch (JSONException e)
        {
          e.printStackTrace();
        }

        ulClientAST.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        dlClientAST.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

      });
    }

    private void setupTabIcons()
    {
      tabLayout.getTabAt(0).setIcon(R.drawable.ic_info_outline_white_36dp);
      tabLayout.getTabAt(1).setIcon(R.drawable.ic_account_circle_white_36dp);
      tabLayout.getTabAt(2).setIcon(R.drawable.ic_directions_car_white_36dp);
      tabLayout.getTabAt(3).setIcon(R.drawable.trolley_with_cargo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id)
        {
          case (R.id.clr_msgs):
            ((LinearLayout)findViewById(R.id.info_main_ll)).removeAllViews();
            break;
//          case (R.id.profile1):
//            break;
//          case (R.id.profile2):
//            break;
//          case (R.id.profile3):
//            break;
//          case (R.id.profile4):
//            break;

        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment()
        {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
          fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            int tabIdx = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;

            fab.hide();

            switch (tabIdx)
            {
                case 0:
                  rootView = inflater.inflate(R.layout.info_main, container, false);
                  break;
                case 1:
                    rootView = inflater.inflate(R.layout.pd_main, container, false);
                    LinearLayout linearLayout = rootView.findViewById(R.id.pd_main_linear_layout);
                    String[] fullNamesArr = getResources().getStringArray(R.array.full_names);

                    for (int i = 0; i < linearLayout.getChildCount(); i++)
                    {
                      if (i == 0)
                      {
                        ((ImageView) linearLayout.getChildAt(i).findViewById(R.id.pd_card_img)).
                            setImageResource(R.drawable.driver_pic);
                      }
                      else
                      {
                        ((ImageView) linearLayout.getChildAt(i).findViewById(R.id.pd_card_img)).
                            setImageResource(R.drawable.passenger_pic);

                      }

                      ((EditText) linearLayout.getChildAt(i).findViewById(R.id.pd_card_name)).
                          setText(fullNamesArr[i]);
                      ((EditText) linearLayout.getChildAt(i).findViewById(R.id.pd_card_id)).
                          setText(Integer.toString((new Random()).nextInt(900000000) + 10000000));
                      iAddedPerson = i;
                    }
                    break;
                case 2:
                  rootView = inflater.inflate(R.layout.trip_main, container, false);
                  break;
                case 3:
                  File extStorageDir = getContext().getExternalFilesDir(null);
                  Properties cfg = MainActivity.ReadProperties(extStorageDir);

                  rootView = inflater.inflate(R.layout.cargo_main, container, false);

                  String bname = cfg.getProperty("BNAME");
                  String bn = cfg.getProperty("BNo");

                  if (bname == null || bn == null)
                  {
                    bname = "Acme";
                    bn = "54112925";
                  }

                  ((EditText) rootView.findViewById(R.id.bname_et)).setText(bname);
                  ((EditText) rootView.findViewById(R.id.bn_text_et)).setText(bn);

                  SaveProperties(extStorageDir, cfg);

                  break;
            }

//            scrollView2 = (ScrollView) MainActivity.this.findViewById(R.id.scroll_view);
//            View pg_tab = getLayoutInflater().inflate(R.layout.pd_tab, scrollView2, false);
////        scrollView2.addView(pg_tab);
//
//            LinearLayout ll_old = (LinearLayout) MainActivity.this.findViewById(R.id.info_tab);
//
//            ViewGroupUtils.replaceView(ll_old, pg_tab);


//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.textView);
//            textView.setText(getString(R.string.section_format, );
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return null;
        }
    }
}

//class ViewGroupUtils
//{
//
//    private static ViewGroup getParent(View view) {
//        return (ViewGroup)view.getParent();
//    }
//
//    public static void removeView(View view)
//    {
//        ViewGroup parent = getParent(view);
//        if(parent != null)
//        {
//            parent.removeView(view);
//        }
//    }
//
//    public static void replaceView(View currentView, View newView)
//    {
//        ViewGroup parent = getParent(currentView);
//        if(parent == null)
//        {
//            return;
//        }
//
//        final int index = parent.indexOfChild(currentView);
//        newView.setId(currentView.getId());
//        removeView(currentView);
//        removeView(newView);
//        parent.addView(newView, index);
//    }
//}