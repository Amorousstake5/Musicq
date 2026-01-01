package com.uxp.musicq;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupCredits();
    }

    private void setupCredits() {
        TextView txtVersion = findViewById(R.id.txtVersion);
        txtVersion.setText("Version 2026.01.01.2000NewVibe");

        MaterialCardView cardDeveloper = findViewById(R.id.cardDeveloper);
        MaterialCardView cardLibraries = findViewById(R.id.cardLibraries);
        MaterialCardView cardGithub = findViewById(R.id.cardGithub);
        MaterialCardView cardBrett = findViewById(R.id.cardBrett);

        cardGithub.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/Amorousstake5/musicq"));
            startActivity(intent);
        });
        cardBrett.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.pexels.com/photo/black-and-gray-vinyl-record-2746823"));
            startActivity(intent);
        });
    }
}