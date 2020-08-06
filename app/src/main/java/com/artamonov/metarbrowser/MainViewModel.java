package com.artamonov.metarbrowser;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class MainViewModel extends AndroidViewModel {

    private String stationName;
    public MutableLiveData<String> rawliveData = new MutableLiveData<>();
    public MutableLiveData<String> decodedliveData = new MutableLiveData<>();
    private SharedPreferences preferences;

    public MainViewModel(@NonNull Application application) {
        super(application);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
    }

    public void setStationName(String newName) {
        stationName = newName;
        getRaw();
        getDecoded();
    }

    public void getRaw() { parseRawTxtFile(); }

    public void getDecoded() { parseDecodedTxtFile(); }

    private void parseRawTxtFile() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = null;
                    url = new URL("https://tgftp.nws.noaa.gov/data/observations/metar/stations/ED"
                            + stationName + ".TXT");

                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder everything = new StringBuilder();
                    String str;
                    while ((str = in.readLine()) != null) {
                        everything.append(str + "\n");
                    }
                    rawliveData.postValue(everything.toString());
                    saveRawMetar(stationName, everything.toString());
                    in.close();
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                }
            }
        };
        new Thread(runnable).start();
    }

    private void parseDecodedTxtFile() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = null;
                    url = new URL("https://tgftp.nws.noaa.gov/data/observations/metar/decoded/ED"
                            + stationName + ".TXT");

                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder everything = new StringBuilder();
                    String str;
                    while ((str = in.readLine()) != null) {
                        everything.append(str + "\n");
                    }
                    decodedliveData.postValue(everything.toString());
                    saveDecodedMetar(stationName, everything.toString());
                    in.close();
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                }
            }
        };
        new Thread(runnable).start();
    }

    private SharedPreferences.Editor getSharedEditor() {
        return preferences.edit();
    }

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

    public boolean isInOffline(String stationName) {
        return getRawMetarByStationName(stationName) != null &&
                getDecodedMetarByStationName(stationName) != null;
    }

    public void setRawStationNameFromOffline(String stationName) {
        String result = getRawMetarByStationName(stationName);
        if (result != null) { rawliveData.postValue(result); }
    }

    public void setDecodedStationNameFromOffline(String stationName) {
        String result = getDecodedMetarByStationName(stationName);
        if (result != null) { decodedliveData.postValue(result); }
    }
}


