package com.example.traffichazardsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


public class MainActivity extends AppCompatActivity {
    Button back;
    Spinner Type;
    Button Submit;
    Button Upload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Submit = findViewById(R.id.Submit);
        back = findViewById(R.id.backMain);
        Type = findViewById(R.id.Type);
        Upload = findViewById(R.id.Upload);

        //Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Type.setAdapter(adapter);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toBack();
            }
        });
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toMaps();
            }
        });
    }
    public void toBack(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
    public void toMaps(){
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
    }
}