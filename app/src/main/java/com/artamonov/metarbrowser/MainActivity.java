package com.artamonov.metarbrowser;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.artamonov.metarbrowser.utils.NetworkConnectivityHelper;
import com.artamonov.metarbrowser.utils.PostTextChangeWatcher;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    private TextView rawText;
    private TextView decodedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rawText = findViewById(R.id.raw_data);
        decodedText = findViewById(R.id.decoded_data);
        TextInputEditText stationInputText = findViewById(R.id.station_name);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        stationInputText.addTextChangedListener(new PostTextChangeWatcher(new PostTextChangeWatcher.PostTextChangedListener() {
            @Override
            public void textChanged(String stationName) {
                if (!NetworkConnectivityHelper.isConnected(getApplicationContext())) {
                    if (viewModel.isInOffline(stationName)) {
                        viewModel.setRawStationNameFromOffline(stationName);
                        viewModel.setDecodedStationNameFromOffline(stationName);
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.network_error_unknown), Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    viewModel.setStationName(stationName);
                }
            }
        }));

        viewModel.rawLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                rawText.setText(s);
            }
        });

        viewModel.decodedLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                decodedText.setText(s);
            }
        });

    }
}