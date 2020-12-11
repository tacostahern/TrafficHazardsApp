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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        back = findViewById(R.id.backMain);
        Type = findViewById(R.id.Type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Type.setAdapter(adapter);
        Intent Back = new Intent(this,LoginActivity.class);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toBack();
            }
        });
    }
    public void toBack(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}