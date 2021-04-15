package com.anusha.twodboxes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import java.util.Random;

import static com.anusha.twodboxes.SettingsHandler.PREFS_NAME;

public class MainActivity extends AppCompatActivity {
    public static String storedUserName = "My Friend";
    public static boolean silent = false;
    public static boolean advancedModeEnabled = false;
    public static int gridSize = 8;
    public static int fillPerc = 3;
    public static String gameMode = "N";
    public static int complexGameWon = 0;
    public static int advancedGameWon = 0;
    public static int sleepTimer = 10;
    public static int longSleepTimer = 200;
    public static boolean gameStopped = false;
    public static boolean gameRefresh = false;
    public static boolean settingsUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean resetGameCounts = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Get the Settings
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        if (resetGameCounts) {
            editor.putInt("complexGameWon", 0);
            editor.putInt("advancedGameWon", 0);
            editor.apply();
        }


        storedUserName = settings.getString("userName", "My Friend");
        silent = settings.getBoolean("silentMode", false);
        gridSize = settings.getInt("gridSize",8);
        fillPerc = settings.getInt("fillPerc",3);
        if (fillPerc == 4) fillPerc = 3;
        gameMode = settings.getString("GameMode","N");
        complexGameWon = settings.getInt("complexGameWon",0);
        advancedGameWon = settings.getInt("advancedGameWon",0);
        advancedModeEnabled = settings.getBoolean("hardModeEnabled",false);

        //Activate the Play Button
        Button newGame = (Button) findViewById(R.id.newGameButton);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingsUpdated) {
                    settingsUpdated = false;
                }
                gameRefresh = true;
                Random rand = new Random();
                int randNumber;
                if (gameMode.equals("A")) {
                    randNumber = (rand.nextInt(100)%2);
                } else {
                    randNumber = (rand.nextInt(100)%10);
                }


                Log.d("MainActivity","gameStopped " + gameStopped);
                // Intent playIntent = new Intent("com.anusha.twodboxes.PLAY");
                Intent playIntent = new Intent("com.anusha.twodboxes.GAMEHANDLER");
                startActivity(playIntent);
            }
        });

        Button resumeGame = (Button) findViewById(R.id.resumeGameButton);
        if (!gameStopped || settingsUpdated) {
            resumeGame.setEnabled(false);
            resumeGame.setBackgroundResource(R.drawable.button_shape_disabled);
            settingsUpdated = false;
            Log.d("MainActivity","Resume Disabled");
        }
        else {
            resumeGame.setEnabled(true);
            resumeGame.setBackgroundResource(R.drawable.play_button_shape);
            Log.d("MainActivity","Resume Enabled");
        }

        resumeGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity","gameStopped " + gameStopped);
                // Intent playIntent = new Intent("com.anusha.twodboxes.PLAY");
                gameRefresh = false;
                Random rand = new Random();
                int randNumber;
                if (gameMode.equals("A")) {
                    randNumber = (rand.nextInt(100)%2);
                } else {
                    randNumber = (rand.nextInt(100)%1);
                }

                Log.d("MainActivity","gameStopped " + gameStopped);
                // Intent playIntent = new Intent("com.anusha.twodboxes.PLAY");
                Intent playIntent = new Intent("com.anusha.twodboxes.GAMEHANDLER");
                startActivity(playIntent);
            }
        });

        //Activate the Settings Button
        Button changeSettings = (Button) findViewById(R.id.settingsButton);
        changeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent playIntent = new Intent("com.anusha.twodboxes.PLAY");
                Intent settingsIntent = new Intent("com.anusha.twodboxes.SETTINGSHANDLER");
                startActivity(settingsIntent);
            }
        });
        //Activate the Help Button
        Button displayHelp = (Button) findViewById(R.id.helpButton);
        displayHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent playIntent = new Intent("com.anusha.twodboxes.PLAY");
                Intent helpIntent = new Intent("com.anusha.twodboxes.HELPHANDLER");
                startActivity(helpIntent);
            }
        });
    }

    protected void onResume() {
        super.onResume();
        Button resumeGame = (Button) findViewById(R.id.resumeGameButton);
        if (!gameStopped || settingsUpdated) {
            resumeGame.setEnabled(false);
            resumeGame.setBackgroundResource(R.drawable.button_shape_disabled);
            settingsUpdated = false;
            Log.d("MainActivity","On resume, Resume Disabled");
        }
        else {
            resumeGame.setEnabled(true);
            resumeGame.setBackgroundResource(R.drawable.play_button_shape);
            Log.d("MainActivity","On resume, Resume Enabled");
        }
    }
}
