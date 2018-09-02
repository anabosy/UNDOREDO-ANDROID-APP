package com.undoredo.app.intro;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;
import com.undoredo.app.R;
import com.undoredo.app.main.MainActivity;
import com.undoredo.app.services.InputAccessibilityService;
import com.undoredo.app.utils.MyUtils;

import io.fabric.sdk.android.Fabric;

/**
 * Activity to display the app Introduction (Page that explain what the app do and how to enable it)
 */
public class IntroActivity extends AppIntro implements
        IntroEnableServiceFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean firstLaunchPassed = MyUtils.getBooleanSharedPref(getApplicationContext(), MyUtils.FIRST_LAUNCH_PASSED);

        if (firstLaunchPassed){
            // if se'rvice enabled so app is not at the first launch and we start the main activity directly
            startMainActivity();

        }

        Fabric.with(this, new Crashlytics());

        // Note here that we DO NOT use setContentView();

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        SliderPage slideP1 = new SliderPage();
        slideP1.setTitle(getString(R.string.what_is_undo_redo));
        slideP1.setDescription(getString(R.string.what_is_undo_redo_desc));
        slideP1.setTitleColor(getResources().getColor(R.color.colorAccent));
        slideP1.setDescColor(getResources().getColor(R.color.secondary_text));
        slideP1.setBgColor(Color.parseColor("#FFFFFF"));
        slideP1.setImageDrawable(R.drawable.undo_redo);

        setColorDoneText(getResources().getColor(R.color.colorAccent));
        setNextArrowColor(getResources().getColor(R.color.colorAccent));
        setColorSkipButton(getResources().getColor(R.color.colorAccent));
        setIndicatorColor(getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorGris));

        addSlide(AppIntroFragment.newInstance(slideP1));

        addSlide(IntroEnableServiceFragment.newInstance());

        showSkipButton(false);


        setFadeAnimation();

    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        MyUtils.setBooleanSharedPref(getApplicationContext(), MyUtils.FIRST_LAUNCH_PASSED, true);
        finish();
    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // si slide login, changer text button done : Plus Tard
        if (newFragment instanceof IntroEnableServiceFragment){
            setDoneText(getString(R.string.settings));
        }else {
            // si slide 1, changer text button suivant en suivant
            setDoneText("");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        boolean serviceEnabled = MyUtils.isAccessibilityServiceEnabled(getApplicationContext(), InputAccessibilityService.class);

        if (serviceEnabled){
            // if service enabled so app is not at the first launch and we start the main activity directly
            startMainActivity();
        }

    }

    @Override
    public void onSettingsButonClick() {
        // on lance les parametres d'accessibilité
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

}