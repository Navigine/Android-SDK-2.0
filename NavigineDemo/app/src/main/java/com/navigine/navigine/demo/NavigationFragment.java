package com.navigine.navigine.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.navigine.idl.java.AnimationType;
import com.navigine.idl.java.IconMapObject;
import com.navigine.idl.java.CircleMapObject;
import com.navigine.idl.java.Location;
import com.navigine.idl.java.LocationListener;
import com.navigine.idl.java.LocationPoint;
import com.navigine.idl.java.Notification;
import com.navigine.idl.java.NotificationListener;
import com.navigine.idl.java.NotificationManager;
import com.navigine.idl.java.Point;
import com.navigine.idl.java.Position;
import com.navigine.idl.java.PositionListener;
import com.navigine.idl.java.Sublocation;
import com.navigine.view.LocationView;
import com.navigine.view.TouchInput;

import java.util.ArrayList;
import java.util.List;

public class NavigationFragment extends Fragment {

    Button swap;
    Button record;

    GridView gvMain;
    SublocationsAdapter mAdapter;
    Location mLocation = null;
    List<Sublocation> mSublocations = new ArrayList<>();

    LocationView locationView;

    NotificationManager mNotificationManager = null;

    IconMapObject mPosition = null;

    boolean isRecording = false;


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        swap = view.findViewById(R.id.swap_sublocations);
        swap.setOnClickListener(view1 -> {
            if (gvMain.getVisibility() == View.VISIBLE)
                gvMain.setVisibility(View.GONE);
            else if (gvMain.getVisibility() == View.GONE)
                gvMain.setVisibility(View.VISIBLE);
        });

        locationView = view.findViewById(R.id.location_view);

        record = view.findViewById(R.id.record);
        record.setOnClickListener(view1 -> {
            if (isRecording) {
                NavigineApp.NavigationManager.stopLogRecording();
                isRecording = false;
                record.setText("Rec");
                record.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            } else {
                NavigineApp.NavigationManager.startLogRecording();
                isRecording = true;
                record.setText("Stop");
                record.setTextColor(getResources().getColor(R.color.removeColor));
            }
        });

        NavigineApp.NavigationManager.addPositionListener(new PositionListener() {
            @Override
            public void onPositionUpdated(Position position) {
                mPosition.setPositionAnimated(new LocationPoint(position.getPoint(), position.getLocationId(), position.getSublocationId()), 1.0f, AnimationType.CUBIC);
            }

            @Override
            public void onPositionError(Error error) { }
        });

        gvMain = view.findViewById(R.id.sub_loc_list);

        NavigineApp.LocationManager.addLocationListener(new LocationListener() {
            @Override
            public void onLocationLoaded(Location location) {
                mLocation = location;

                mSublocations.clear();
                mSublocations.addAll(location.getSublocations());

                gvMain.setVisibility(View.VISIBLE);
                mAdapter = new SublocationsAdapter();
                gvMain.setAdapter(mAdapter);
                mAdapter.updateList();
            }

            @Override
            public void onDownloadProgress(int i, int i1) {

            }

            @Override
            public void onLocationFailed(Error error) {
                System.out.println(error.getMessage());
            }
        });


        locationView.getLocationViewController().getTouchInput().setTapResponder(new TouchInput.TapResponder() {
            @Override
            public boolean onSingleTapUp(float v, float v1) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(float x, float y) {
                return true;
            }
        });

        mNotificationManager = NavigineApp.NotificationManager;

        mNotificationManager.addNotificationListener(new NotificationListener() {
            @Override
            public void onNotificationLoaded(Notification notification) {
                Log.d("Notification", "onNotificationLoaded: " + notification.getTitle());
            }

            @Override
            public void onNotificationFailed(Error error) {
                Log.d("Notification", "onNotificationLoaded: " + error.getMessage());
            }
        });

        return view;
    }

    // location loader to list view
    private class SublocationsAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mSublocations.size();
        }

        @Override
        public Object getItem(int i)
        {
            return mSublocations.get(i);
        }

        @Override
        public long getItemId(int pos)
        {
            return pos;
        }

        private void updateList()
        {
            notifyDataSetChanged();
        }

        @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup)
        {
            Sublocation sublocation     = mSublocations.get(i);
            String      sublocationName = sublocation.getName();

            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item, null);
            }

            TextView titleTextView = view.findViewById(R.id.tvText);

            if (sublocationName.length() >= 12)
                sublocationName = sublocationName.substring(0, 10) + "...";

            titleTextView.setText(sublocationName);

            view.setOnClickListener(v -> {
                locationView.getLocationViewController().setSublocationId(sublocation.getId());
                gvMain.setVisibility(View.GONE);
                mPosition = locationView.getLocationViewController().addIconMapObject();
                mPosition.setSize(30, 30);
                mPosition.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.blue_dot));


            });

            return view;
        }
    }
}
