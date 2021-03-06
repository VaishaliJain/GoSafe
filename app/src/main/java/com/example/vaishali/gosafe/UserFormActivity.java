package com.example.vaishali.gosafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class UserFormActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.userform);
        Button submitReviewButton = findViewById(R.id.submit);
        Button cancelReviewButton = findViewById(R.id.cancel);
        submitReviewButton.setOnClickListener(this);
        cancelReviewButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(findViewById(R.id.submit))) {
            // upload data to server
            Toast toast = Toast.makeText(getApplicationContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        } else if (view.equals(findViewById(R.id.cancel))) {
            finish();
        }
    }
}
