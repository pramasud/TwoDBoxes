package com.anusha.twodboxes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import static com.anusha.twodboxes.MainActivity.silent;
import static com.anusha.twodboxes.MainActivity.storedUserName;

/**
 * Created by skpraman on 5/22/2017.
 */

public class SettingsHandler extends Activity {
    public static final String PREFS_NAME = "TwoDBoxesPrefsFile";
    Button commitButton;
    EditText userNameInfo;
    TextView complexWinsValues;
    TextView advancedWinsValues;
    String userName;
    private boolean adDisplayed = false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        adDisplayed = false;
        commitButton = findViewById(R.id.buttonUserCommit);
        userNameInfo = findViewById(R.id.settingUserName);

        complexWinsValues = findViewById(R.id.complexWinsValues) ;
        advancedWinsValues = findViewById(R.id.advancedWinsValues);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        RadioButton radioButtonMute = findViewById(R.id.radioSoundMute);
        RadioButton radioButtonUnMute = findViewById(R.id.radioSoundUnMute);

        RadioButton radioGridSix = findViewById(R.id.radioGridSix);
        RadioButton radioGridEight = findViewById(R.id.radioGridEight);
        RadioButton radioGridTen = findViewById(R.id.radioGridTen);
        RadioButton radioGridTwelve = findViewById(R.id.radioGridTwelve);

        RadioButton radioPreFill2 = findViewById(R.id.radioPreFill2);
        RadioButton radioPreFill3 = findViewById(R.id.radioPreFill3);
        RadioButton radioPreFill4 = findViewById(R.id.radioPreFill4);
        RadioButton radioPreFillNone = findViewById(R.id.radioPreFillNone);

        RadioButton radioGameModeNormal = findViewById(R.id.radioGameModeNormal);
        RadioButton radioGameModeComplex = findViewById(R.id.radioGameModeComplex);
        RadioButton radioGameModeAdvanced = findViewById(R.id.radioGameModeAdvanced);

        // sound Settings Default
        if (silent) {
            radioButtonMute.setChecked(true);
            radioButtonUnMute.setChecked(false);
        } else {
            radioButtonMute.setChecked(false);
            radioButtonUnMute.setChecked(true);
        }
        // Grid size Settings Default
        if (MainActivity.gridSize == 6) {
            radioGridSix.setChecked(true);
            radioGridEight.setChecked(false);
            radioGridTen.setChecked(false);
            radioGridTwelve.setChecked(false);
        }
        else if (MainActivity.gridSize == 8) {
            radioGridSix.setChecked(false);
            radioGridEight.setChecked(true);
            radioGridTen.setChecked(false);
            radioGridTwelve.setChecked(false);
        }
        else if (MainActivity.gridSize == 10) {
            radioGridSix.setChecked(false);
            radioGridEight.setChecked(false);
            radioGridTen.setChecked(true);
            radioGridTwelve.setChecked(false);
        }
        else if (MainActivity.gridSize == 12) {
            radioGridSix.setChecked(false);
            radioGridEight.setChecked(false);
            radioGridTen.setChecked(false);
            radioGridTwelve.setChecked(true);
        }
        //Pre fill Percentage Default
        if (MainActivity.fillPerc == 3) {
            radioPreFill2.setChecked(true);
            radioPreFill3.setChecked(false);
            radioPreFill4.setChecked(false);
            radioPreFillNone.setChecked(false);
        }
        else if (MainActivity.fillPerc == 2) {
            radioPreFill2.setChecked(false);
            radioPreFill3.setChecked(true);
            radioPreFill4.setChecked(false);
            radioPreFillNone.setChecked(false);
        }
        else if (MainActivity.fillPerc == 2) {
            radioPreFill2.setChecked(false);
            radioPreFill3.setChecked(false);
            radioPreFill4.setChecked(true);
            radioPreFillNone.setChecked(false);
        }
        else if (MainActivity.fillPerc == 0) {
            radioPreFill2.setChecked(false);
            radioPreFill3.setChecked(false);
            radioPreFill4.setChecked(false);
            radioPreFillNone.setChecked(true);
        }

        /*
        //Game Mode Default
        if (MainActivity.advancedModeEnabled) {
            radioGameModeAdvanced.setEnabled(true);
        } else {
            radioGameModeAdvanced.setEnabled(false);
        }
        */

        //\Advanced Mode Not Available
        radioGameModeAdvanced.setEnabled(false);

        if (MainActivity.gameMode.equals("N")) {
            radioGameModeNormal.setChecked(true);
            radioGameModeComplex.setChecked(false);
            radioGameModeAdvanced.setChecked(false);
        } else if (MainActivity.gameMode.equals("C")) {
            radioGameModeNormal.setChecked(false);
            radioGameModeComplex.setChecked(true);
            radioGameModeAdvanced.setChecked(false);
        } else if (MainActivity.gameMode.equals("A")) {
            radioGameModeNormal.setChecked(false);
            radioGameModeComplex.setChecked(false);
            radioGameModeAdvanced.setEnabled(true);
            radioGameModeAdvanced.setChecked(true);
        }

        userNameInfo.setText(storedUserName);
        String complexWins = "Complex : " + MainActivity.complexGameWon;
        String advancedWins = "Advanced : " + MainActivity.advancedGameWon;
        complexWinsValues.setText(complexWins);
        advancedWinsValues.setText(advancedWins);

        commitButton.setOnClickListener(
            new View.OnClickListener() {
                public void onClick (View view) {
                    userName = userNameInfo.getText().toString().trim();
                    if (userName.length() > 0) {
                        SharedPreferences updSettings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = updSettings.edit();
                        editor.putString("userName", userName);

                        // Commit the edits!
                        editor.apply();
                        storedUserName = userName;
                        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                        try {
                            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Log.d("User Name","Not Entered");
                    }
                }
            }
        );
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        boolean soundClicked = false, gridClicked = false,  fillPercClicked = false, modeSet = false;
        boolean silent = true;
        int gridSize = 0;
        int fillPerc = 0;
        String setGameMode = "N";
        RadioButton radioButtonMute = findViewById(R.id.radioSoundMute);
        RadioButton radioButtonUnMute = findViewById(R.id.radioSoundUnMute);

        RadioButton radioGridSix = findViewById(R.id.radioGridSix);
        RadioButton radioGridEight = findViewById(R.id.radioGridEight);
        RadioButton radioGridTen = findViewById(R.id.radioGridTen);
        RadioButton radioGridTwelve = findViewById(R.id.radioGridTwelve);

        RadioButton radioPreFill2 = findViewById(R.id.radioPreFill2);
        RadioButton radioPreFill3 = findViewById(R.id.radioPreFill3);
        RadioButton radioPreFill4 = findViewById(R.id.radioPreFill4);
        RadioButton radioPreFillNone = findViewById(R.id.radioPreFillNone);

        RadioButton radioGameModeNormal = findViewById(R.id.radioGameModeNormal);
        RadioButton radioGameModeComplex = findViewById(R.id.radioGameModeComplex);
        RadioButton radioGameModeAdvanced = findViewById(R.id.radioGameModeAdvanced);

        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        try {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // Check which radio button was clicked
        switch(view.getId()) {
            // Radio Sound Buttons
            case R.id.radioSoundMute:
                if (checked) {
                    radioButtonUnMute.setChecked(false);
                    radioButtonMute.setChecked(true);
                    silent = true;
                    soundClicked = true;
                }
                break;
            case R.id.radioSoundUnMute:
                if (checked) {
                    radioButtonUnMute.setChecked(true);
                    radioButtonMute.setChecked(false);
                    silent = false;
                    soundClicked = true;
                }
                break;
            //Radio Grid Buttons
            case R.id.radioGridSix:
                if (checked) {
                    radioGridSix.setChecked(true);
                    radioGridEight.setChecked(false);
                    radioGridTen.setChecked(false);
                    radioGridTwelve.setChecked(false);
                    gridSize = 6;
                    gridClicked = true;
                    MainActivity.settingsUpdated = true;
                }
                break;
            case R.id.radioGridEight:
                if (checked) {
                    radioGridSix.setChecked(false);
                    radioGridEight.setChecked(true);
                    radioGridTen.setChecked(false);
                    radioGridTwelve.setChecked(false);
                    gridSize = 8;
                    gridClicked = true;
                    MainActivity.settingsUpdated = true;
                }
                break;
            case R.id.radioGridTen:
                if (checked) {
                    radioGridSix.setChecked(false);
                    radioGridEight.setChecked(false);
                    radioGridTen.setChecked(true);
                    radioGridTwelve.setChecked(false);
                    gridSize = 10;
                    gridClicked = true;
                    MainActivity.settingsUpdated = true;
                }
                break;
            case R.id.radioGridTwelve:
                if (checked) {
                    radioGridSix.setChecked(false);
                    radioGridEight.setChecked(false);
                    radioGridTen.setChecked(false);
                    radioGridTwelve.setChecked(true);
                    gridSize = 12;
                    gridClicked = true;
                    MainActivity.settingsUpdated = true;
                }
                break;
            //Radio Pre Fill Buttons
            case R.id.radioPreFill2:
                if (checked) {
                    radioPreFill2.setChecked(true);
                    radioPreFill3.setChecked(false);
                    radioPreFill4.setChecked(false);
                    radioPreFillNone.setChecked(false);
                    fillPerc = 3;
                    fillPercClicked = true;
                    MainActivity.settingsUpdated = true;
                }
                break;

            case R.id.radioPreFill3:
                if (checked) {
                    radioPreFill2.setChecked(false);
                    radioPreFill3.setChecked(true);
                    radioPreFill4.setChecked(false);
                    radioPreFillNone.setChecked(false);
                    fillPerc = 2;
                    fillPercClicked = true;
                    MainActivity.settingsUpdated = true;
                }
                break;

            case R.id.radioPreFill4:
                if (checked) {
                    radioPreFill2.setChecked(false);
                    radioPreFill3.setChecked(false);
                    radioPreFill4.setChecked(true);
                    radioPreFillNone.setChecked(false);
                    fillPerc = 1;
                    fillPercClicked = true;
                    MainActivity.settingsUpdated = true;
                }
                break;

            case R.id.radioPreFillNone:
                if (checked) {
                    radioPreFill2.setChecked(false);
                    radioPreFill3.setChecked(false);
                    radioPreFill4.setChecked(false);
                    radioPreFillNone.setChecked(true);
                    fillPerc = 0;
                    fillPercClicked = true;
                    MainActivity.settingsUpdated = true;
                }
                break;
            // Game Mode Buttons
            case R.id.radioGameModeNormal:
                if (checked) {
                    radioGameModeNormal.setChecked(true);
                    radioGameModeComplex.setChecked(false);
                    radioGameModeAdvanced.setChecked(false);
                    setGameMode = "N";
                    modeSet = true;
                    MainActivity.settingsUpdated = true;
                }
                break;
            case R.id.radioGameModeComplex:
                if (checked) {
                    radioGameModeNormal.setChecked(false);
                    radioGameModeComplex.setChecked(true);
                    radioGameModeAdvanced.setChecked(false);
                    setGameMode = "C";
                    modeSet = true;
                    MainActivity.settingsUpdated = true;
                }
                break;
            case R.id.radioGameModeAdvanced:
                if (checked) {
                    radioGameModeNormal.setChecked(false);
                    radioGameModeComplex.setChecked(false);
                    radioGameModeAdvanced.setChecked(true);
                    setGameMode = "A";
                    modeSet = true;
                    MainActivity.settingsUpdated = true;
                }
                break;
        }
        SharedPreferences updSettings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = updSettings.edit();
        if (soundClicked) {
            editor.putBoolean("silentMode", silent);
            MainActivity.silent = silent;
            // Commit the edits!
            editor.apply();
        }
        if (gridClicked) {
            editor.putInt("gridSize", gridSize);
            MainActivity.gridSize = gridSize;
            // Commit the edits!
            editor.apply();
        }
        if (fillPercClicked) {
            editor.putInt("fillPerc", fillPerc);
            MainActivity.fillPerc = fillPerc;
            // Commit the edits!
            editor.apply();
        }
        if (modeSet) {
            editor.putString("GameMode", setGameMode);
            MainActivity.gameMode = setGameMode;
            // Commit the edits!
            editor.apply();
        }
    }
}


