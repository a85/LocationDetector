package com.rickreation.locationdetector;

import android.app.Activity;
import android.content.Context;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class HomeActivitiy extends Activity {
	private final String TAG = "HomeActivity";
	//Location manager related
	private Location curLocation = null;
	private LocationManager mLocationManager;
	private String locationProvider;
	private int locationTime = 60000;
	private int locationDistance = 0;
	private Boolean locationChanged = false;
	private Geocoder geocoder;
	private String currentAddress;
	private List<Address> addressList = null;
	private Handler mHandler;
	
	float lat = 0f;
	float lng = 0f;
	int iLat;
	int iLng;
	
	private final int MILLION = 1000000;

	private TextView latView;
	private TextView lngView;
	private TextView logView;
	
	private StringBuilder logMessages;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        logMessages = new StringBuilder("Starting log. \n");
		
		latView = (TextView) findViewById(R.id.text_lat);
		lngView = (TextView) findViewById(R.id.text_lng);
		logView = (TextView) findViewById(R.id.log);		
		
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mHandler = new Handler();
		startReceivingLocationUpdates();
		mHandler.postDelayed(mGetCurrentLocationTask, 1);		
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	stopReceivingLocationUpdates();
    }
    
    public class LocationListener implements android.location.LocationListener {
    	Location mLastLocation;
        boolean mValid = false;
        String mProvider;

        public LocationListener(String provider) {
            mProvider = provider;
            mLastLocation = new Location(mProvider);
        }

        public void onLocationChanged(Location newLocation) {
            if (newLocation.getLatitude() == 0.0 && newLocation.getLongitude() == 0.0) {
                // Hack to filter out 0.0,0.0 locations
                return;
            }
            mLastLocation.set(newLocation);
            mValid = true;
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
            mValid = false;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    mValid = false;                    
                    break;
                }
            }
        }

        public Location current() {
            return mValid ? mLastLocation : null;
        }
    }
    
    LocationListener [] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };
    
    private void startReceivingLocationUpdates() {
		if (mLocationManager != null) {
			Log.d(TAG, "Starting to receive location updates");
            try {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0F, mLocationListeners[1]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
            }
            
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0F, mLocationListeners[0]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
            }
            
            try {
                mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0F, mLocationListeners[2]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
            }
        }
	}
    
    private void stopReceivingLocationUpdates() {
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private Location getCurrentLocation() {
        // go in best to worst order
    	Location current = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	boolean foundLastKnownLocation = false;
    	if(current == null) {
    		current = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    		if(current == null) {
    			current = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    			if(current == null) {
    				foundLastKnownLocation = false;
    			}
    			else {
    				foundLastKnownLocation = true;
    			}
    		}
    		else {
    			foundLastKnownLocation = true;
    		}
    	}
    	else {
    		foundLastKnownLocation = true;
    	}
    	
    	if(foundLastKnownLocation == true) {
    		lat = (float)current.getLatitude();
    		lng = (float)current.getLongitude();    		    		
    		runOnUiThread(mUpdateTextView);
    	}
    	
        for (int i = 0; i < mLocationListeners.length; i++) {
        	Location l = mLocationListeners[i].current();
            if (l != null) return l;
        }        
        return null;
    }
    
    private Runnable mGetCurrentLocationTask = new Runnable() {
		public void run() {			
			curLocation = getCurrentLocation();
			if (curLocation != null) {
				lat = (float)curLocation.getLatitude();
				lng = (float)curLocation.getLongitude();				
				iLat = (int)(lat * MILLION);
				iLng = (int)(lng * MILLION);				
				logMessages.append("Found lat/lng from " + curLocation.getProvider() + " \n");
			}
			else {
				logMessages.append("Couldn't find location from any provider. \n");
			}
			
			runOnUiThread(mUpdateTextView);
			
			if (mHandler != null) {
				mHandler.postDelayed(this, 250);
			}				
		}
	};
	
	private Runnable mUpdateTextView = new Runnable() {
		public void run() {
			latView.setText("Latitude: " + lat);
			lngView.setText("Longitude: " + lng);		
			logView.setText(logMessages.toString());
		}
	};
}