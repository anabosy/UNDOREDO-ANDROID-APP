package com.undoredo.app.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.omadahealth.lollipin.lib.PinSettingsActivity;
import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.undoredo.app.R;
import com.undoredo.app.services.InputAccessibilityService;
import com.undoredo.app.utils.MyUtils;

/**
 * A Settings Activity that control the UndoRedo behaviors
 */
public class MainActivity extends PinSettingsActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View backgroundExplanationV;

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_main, new LinearLayout(this), false);
        LayoutInflater.from(this).inflate(layoutResID, (ViewGroup) viewGroup.findViewById(R.id.settings_container), true);
        getWindow().setContentView(viewGroup);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(R.id.settings_container, new MainPreferenceFragment()).commit();

        backgroundExplanationV = findViewById(R.id.backgroundExplanationV);
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // about preference click listener
            Preference myPref = findPreference(getString(R.string.key_pref_about));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    showAbout(getActivity());
                    return true;
                }
            });

            Preference pinPref = findPreference(getString(R.string.key_pref_enable_pinlock));
            pinPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if(o instanceof Boolean){
                        Intent intent = new Intent(getActivity(), MyLockActivity.class);

                        if ((Boolean) o) {
                            // enable pin lock
                            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                        } else {
                            // disable pin lock
                            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.DISABLE_PINLOCK);
                        }
                        startActivity(intent);
                    }
                    return true;
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        manageInputService();
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.key_pref_enable_service_undoredo))) { // UndoRedo Service
            manageInputService();
        }
    }


    /**
     * Show the about dialog
     * @param activity
     */
    private static void showAbout(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppCompatAlertDialogStyle);
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(activity).inflate(R.layout.about, new LinearLayout(activity), false);
        builder.setView(viewGroup);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * Start or Stop the Input Service based on the SharedPref value
     */
    private void manageInputService(){
        boolean boolValue = MyUtils.getBooleanSharedPref(this, getString(R.string.key_pref_enable_service_undoredo));
        if (boolValue) {
            // enable service
            startInputService();
            backgroundExplanationV.setVisibility(View.GONE);
        } else {
            // disable service
            stopInputService();
            backgroundExplanationV.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Start the Input Service
     */
    private void startInputService(){
        boolean serviceEnabled = MyUtils.isAccessibilityServiceEnabled(getApplicationContext(), InputAccessibilityService.class);

        if (!serviceEnabled) {
            // show settings dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(R.string.enable_accessibility_service);
            builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // on lance les parametres d'accessibilité
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                }
            });

            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.setMessage(getString(R.string.enable_accessibility_service_process));
            dialog.show();
        }

        startService(new Intent(this, InputAccessibilityService.class));
    }


    /**
     * Stop the Input Service
     */
    private void stopInputService() {
        stopService(new Intent(this, InputAccessibilityService.class));
    }




}
