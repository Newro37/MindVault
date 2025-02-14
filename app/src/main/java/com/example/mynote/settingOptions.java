package com.example.mynote;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class settingOptions extends AppCompatActivity {
    private TextView themeValue;
    private Switch switchOrientation;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);


        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_options);


        editor = sharedPreferences.edit();


        TextView themeSelector = findViewById(R.id.themeSelector);
        themeValue = findViewById(R.id.themeValue);
        switchOrientation = findViewById(R.id.switchOrientation);


        String savedTheme = sharedPreferences.getString("Theme", "System Default");
        themeValue.setText(savedTheme);


        switchOrientation.setChecked(sharedPreferences.getBoolean("NewestFirst", true));


        themeSelector.setOnClickListener(v -> showThemeDialog());


        switchOrientation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("NewestFirst", isChecked);
            editor.apply();
        });
    }


    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        String savedTheme = sharedPreferences.getString("Theme", "System Default");


        int selectedIndex = 2;
        for (int i = 0; i < themes.length; i++) {
            if (themes[i].equals(savedTheme)) {
                selectedIndex = i;
                break;
            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Theme")
                .setSingleChoiceItems(themes, selectedIndex, (dialog, which) -> {
                    String selectedTheme = themes[which];
                    themeValue.setText(selectedTheme);
                    editor.putString("Theme", selectedTheme);
                    editor.apply();

                    dialog.dismiss();
                    recreate();
                })
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void applyTheme() {
        String theme = sharedPreferences.getString("Theme", "System Default");

        switch (theme) {
            case "Light":
                setTheme(com.google.android.material.R.style.Theme_Material3_Light_NoActionBar);
                break;
            case "Dark":
                setTheme(com.google.android.material.R.style.Theme_Material3_Dark_NoActionBar);
                break;
            default:
                setTheme(R.style.Theme_MyNote);
                break;
        }
    }
}
