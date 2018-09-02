package com.undoredo.app.find_replace;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatImageView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.undoredo.app.R;
import com.undoredo.app.eventbus.OnFindReplaceDone;

import org.greenrobot.eventbus.EventBus;

/**
 * Class to display the Find/Replace dialog
 * We use an AlertDialog with a custom view
 */
public class FindReplaceContainer {

    private String mText;
    private Context cxt;
    private TextInputEditText findEditText;
    private TextInputEditText replaceEditText;
    private TextView replaceAllTxtV;
    private TextView replaceTxtV;
    private AppCompatImageView prevImgV;
    private TextView indexTxtV;
    private AppCompatImageView nextImgV;
    private TextView textTxtV;
    private TextView limitTxtV;
    private SearchSpanBuilder mSearchSpanBuilder;
    private FloatingActionButton floatingActionButton;

    private AlertDialog.Builder mDialogBuilder;
    private AlertDialog curentDisplayedDialog;

    public FindReplaceContainer(Context cxt, String mText) {
        this.mText = mText;
        this.cxt = cxt;

        LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(cxt, R.style.AppThemeFindReplaceDialog));
        View mView = inflater.inflate(R.layout.fragment_find_replace, null);

        initView(mView);

        mDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(cxt, R.style.AppThemeFindReplaceDialog));
        mDialogBuilder.setView(mView);

        curentDisplayedDialog = mDialogBuilder.create();
        curentDisplayedDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

    }

    private void initView(View convertView) {

        findEditText = convertView.findViewById(R.id.findEditText);
        replaceEditText = convertView.findViewById(R.id.replaceEditText);

        replaceAllTxtV = convertView.findViewById(R.id.replaceAllTxtV);
        replaceTxtV = convertView.findViewById(R.id.replaceTxtV);

        prevImgV = convertView.findViewById(R.id.prevImgV);
        indexTxtV = convertView.findViewById(R.id.indexTxtV);
        nextImgV = convertView.findViewById(R.id.nextImgV);

        textTxtV = convertView.findViewById(R.id.textTxtV);
        limitTxtV = convertView.findViewById(R.id.limitTxtV);


        floatingActionButton = convertView.findViewById(R.id.floatingActionButton);

        textTxtV.setText(mText);

        mSearchSpanBuilder = new SearchSpanBuilder(
                mText,
                textTxtV,
                indexTxtV,
                cxt.getResources().getColor(R.color.search_word_foregound),
                cxt.getResources().getColor(R.color.search_word_background)
        );


        findEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSearchSpanBuilder.findAll(editable.toString());
            }
        });


        prevImgV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchSpanBuilder.selectPreviousFind();
            }
        });

        nextImgV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchSpanBuilder.selectNextFind();
            }
        });

        replaceTxtV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchSpanBuilder.replaceCurrentFind(replaceEditText.getEditableText().toString());
            }
        });

        replaceAllTxtV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchSpanBuilder.replaceAllFind(replaceEditText.getEditableText().toString());
            }
        });

        replaceAllTxtV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchSpanBuilder.replaceAllFind(replaceEditText.getEditableText().toString());
            }
        });


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new OnFindReplaceDone(mSearchSpanBuilder.toString()));
                curentDisplayedDialog.dismiss();
            }
        });
    }


    public void show(){
        curentDisplayedDialog.show();
    }
}
