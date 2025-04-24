package com.jsrr.android_app93;

import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.activity.compose.setContent;
import androidx.activity.enableEdgeToEdge;
import androidx.compose.foundation.layout.FillMaxSize;
import androidx.compose.foundation.layout.Padding;
import androidx.compose.material3.Scaffold;
import androidx.compose.material3.Text;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.tooling.preview.Preview;
import com.jsrr.android_app93.ui.theme.AndroidApp93Theme;

public class MainActivity extends ComponentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();
        setContent(() -> {
            AndroidApp93Theme.INSTANCE.invoke(() -> {
                Scaffold scaffold = new Scaffold(Modifier.fillMaxSize(), innerPadding -> {
                    Greeting("Android", Modifier.padding(innerPadding));
                });
                return scaffold;
            });
        });
    }

    @Composable
    public void Greeting(String name, Modifier modifier) {
        if (modifier == null) {
            modifier = Modifier.INSTANCE;
        }
        Text text = new Text("Hello " + name + "!", modifier);
    }

    @Preview(showBackground = true)
    @Composable
    public void GreetingPreview() {
        AndroidApp93Theme.INSTANCE.invoke(() -> {
            Greeting("Android", Modifier.INSTANCE);
            return null;
        });
    }
}