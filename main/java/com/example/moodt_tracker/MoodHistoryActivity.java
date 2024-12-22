package com.example.moodt_tracker;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MoodHistoryActivity extends AppCompatActivity {

    private TextView moodHistoryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        moodHistoryTextView = findViewById(R.id.moodHistoryTextView);

        ArrayList<String> moodEntries = getIntent().getStringArrayListExtra("moodEntries");
        if (moodEntries != null) {
            StringBuilder historyText = new StringBuilder();
            for (String entry : moodEntries) {
                historyText.append(entry).append("\n\n");
            }
            moodHistoryTextView.setText(historyText.toString());
        }
    }
}
