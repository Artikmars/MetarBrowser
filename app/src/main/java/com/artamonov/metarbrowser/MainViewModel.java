package com.artamonov.metarbrowser;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * View Model class with context which is needed to initiate Shared Preferences class
 */

public class MainViewModel extends AndroidViewModel {

    private String stationName;
    public final MutableLiveData<String> rawLiveData = new MutableLiveData<>();
    public final MutableLiveData<String> decodedLiveData = new MutableLiveData<>();
    private final SharedPreferences preferences;

    public MainViewModel(@NonNull Application application) {
        super(application);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
    }

    /**
     * Get raw and decoded data for the given station name
     *
     * @param newName last given station name
     */

    public void setStationName(String newName) {
        stationName = newName;
        getRaw();
        getDecoded();
    }

    public void getRaw() {
        parseRawTxtFile();
    }

    public void getDecoded() {
        parseDecodedTxtFile();
    }

    /**
     * The background threading is processed by Thread class with Runnable object as a parameter
     */

    private void parseRawTxtFile() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url;
                    url = new URL("https://tgftp.nws.noaa.gov/data/observations/metar/stations/ED"
                            + stationName + ".TXT");
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder everything = new StringBuilder();
                    String str;
                    while ((str = in.readLine()) != null) {
                        everything.append(str).append("\n");
                    }
                    rawLiveData.postValue(everything.toString());
                    saveRawMetar(stationName, everything.toString());
                    in.close();
                } catch (IOException e) {
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * The background threading is processed by Thread class with Runnable object as a parameter
     */

    private void parseDecodedTxtFile() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url;
                    url = new URL("https://tgftp.nws.noaa.gov/data/observations/metar/decoded/ED"
                            + stationName + ".TXT");
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder everything = new StringBuilder();
                    String str;
                    while ((str = in.readLine()) != null) {
                        everything.append(str).append("\n");
                    }
                    decodedLiveData.postValue(everything.toString());
                    saveDecodedMetar(stationName, everything.toString());
                    in.close();
                } catch (IOException e) {
                }
            }
        };
        new Thread(runnable).start();
    }

    private SharedPreferences.Editor getSharedEditor() {
        return preferences.edit();
    }


    /**
     * Get raw data from Shared Preferences by given station name
     * Returns null if string does not exist in database
     * For the raw data RAW-ED prefix is added
     * For the decoded data DECODED-ED prefix is added
     */

    public String getRawMetarByStationName(String stationName) {
        return preferences.getString("RAW-ED" + stationName, null);
    }

    public String getDecodedMetarByStationName(String stationName) {
        return preferences.getString("DECODED-ED" + stationName, null);
    }

    public void saveRawMetar(String stationName, String data) {
        getSharedEditor().putString("RAW-ED" + stationName, data).commit();
    }

    public void saveDecodedMetar(String stationName, String data) {
        getSharedEditor().putString("DECODED-ED" + stationName, data).commit();
    }

    /**
     * Checks the data presence on the database
     *
     * @return true if both raw and decoded data exist
     */

    public boolean isInOffline(String stationName) {
        return getRawMetarByStationName(stationName) != null &&
                getDecodedMetarByStationName(stationName) != null;
    }

    /**
     * Posts non-null value to Live Data object taken from Shared Preferences
     */

    public void setRawStationNameFromOffline(String stationName) {
        String result = getRawMetarByStationName(stationName);
        if (result != null) {
            rawLiveData.postValue(result);
        }
    }

    public void setDecodedStationNameFromOffline(String stationName) {
        String result = getDecodedMetarByStationName(stationName);
        if (result != null) {
            decodedLiveData.postValue(result);
        }
    }
}


