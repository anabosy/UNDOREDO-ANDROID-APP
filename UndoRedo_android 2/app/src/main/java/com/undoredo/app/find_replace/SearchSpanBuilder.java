package com.undoredo.app.find_replace;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.widget.TextView;

import com.undoredo.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible of searching a word/character through a text, color the finds and allow to replace them
 */
public class SearchSpanBuilder {
    private final TextView mTextView;
    private final TextView mIndexTxtV;
    private String mTextToProcess;

    private int currentSearchIndexSpanSection = 0;
    private SparseArray<Integer> currentSearchIndexSpanSectionSp = new SparseArray<>();

    private String currentToFindText;

    private int findedItemForegroundColor;
    private int findedItemBackgroundColor;

    private List<SpanSection> spanSections;
    private StringBuilder stringBuilder;

    public SearchSpanBuilder(String textToProcess, TextView textView, TextView indexTxtV, int findedItemForegroundColor, int findedItemBackgroundColor){
        stringBuilder = new StringBuilder();
        spanSections = new ArrayList<>();
        mTextView = textView;
        mIndexTxtV = indexTxtV;
        mTextToProcess = textToProcess;
        this.findedItemForegroundColor = findedItemForegroundColor;
        this.findedItemBackgroundColor = findedItemBackgroundColor;
    }


    private class SpanSection{
        private final boolean isSearchWord;
        private String text;
        private int startIndex;
        private CharacterStyle[] styles;

        private SpanSection(boolean isSearchWord, String text, int startIndex,CharacterStyle... styles){
            this.isSearchWord = isSearchWord;
            this.styles = styles;
            this.text = text;
            this.startIndex = startIndex;
        }

        public void setStyles(CharacterStyle... styles){
            this.styles = styles;
        }

        public boolean isSearchWord() {
            return isSearchWord;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public SpanSection setStartIndex(int startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public String getText() {
            return text;
        }

        public SpanSection setText(String text) {
            this.text = text;
            return this;
        }

        private void apply(SpannableStringBuilder spanStringBuilder){
            if (spanStringBuilder == null || styles == null) return;
            for (CharacterStyle style : styles) {
                spanStringBuilder.setSpan(style, startIndex, startIndex + text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
    }


    private SearchSpanBuilder append(String text, boolean isSearchWord, CharacterStyle... styles){
//        if (styles != null && styles.length > 0) {
        spanSections.add(new SpanSection(isSearchWord, text, 0,styles));
//        }
//        stringBuilder.append(text);
        return this;
    }


    private SpannableStringBuilder build(){
        // construction on the stringbuilder
        stringBuilder = new StringBuilder();
        if(spanSections != null && spanSections.size()>0) {
            for (SpanSection section : spanSections) {
                if (section.isSearchWord()){
                    section.setStartIndex(stringBuilder.length());
                }
                stringBuilder.append(section.getText());
            }

            // create the SpannableStringBuilder
            SpannableStringBuilder ssb = new SpannableStringBuilder(stringBuilder.toString());
            // applying each span to the SpannableStringBuilder
            for (SpanSection section : spanSections) {
//                stringBuilder.append(section.getText());
                if (section.isSearchWord()) {
                    section.apply(ssb);
                }
            }

            return ssb;

        }else {
            return new SpannableStringBuilder(stringBuilder = new StringBuilder(mTextToProcess));
        }

    }


    public void findAll(String toFind){
        currentToFindText = toFind;
        resetFind();

        if(mTextToProcess != null && !mTextToProcess.isEmpty()) {

            if(mTextToProcess.contains(toFind)) {
                String[] splits = mTextToProcess.split(toFind);

                int y = 0;

                for (int i = 0; i < splits.length; i++) {
                    append(splits[i], false);
                    if (i!=splits.length-1) {
                        if (i==0) { // if it is the first find, we make it selected by applying the select backgroung color
                            append(toFind, true,
                                    new ForegroundColorSpan(findedItemForegroundColor),
                                    new BackgroundColorSpan(findedItemBackgroundColor)
                            );
                            currentSearchIndexSpanSection = 1;
                        }else {
                            append(toFind, true, new ForegroundColorSpan(findedItemForegroundColor));
                        }
                        currentSearchIndexSpanSectionSp.put(++y, spanSections.size()-1);
                    }
                }

                notifyChanges();
            }else{

                resetFind();
                notifyChanges();
            }

        }
    }

    private void resetFind(){
        // reset all find item related vars
        spanSections = new ArrayList<>();
        currentSearchIndexSpanSection = 0;
        stringBuilder = new StringBuilder();
        currentSearchIndexSpanSectionSp = new SparseArray<>();
    }



    public void selectPreviousFind(){

        if(currentSearchIndexSpanSectionSp.size() > 1 && currentSearchIndexSpanSection > 1){


            // we reset the previous SpanSelection
            spanSections.get(currentSearchIndexSpanSectionSp.get(currentSearchIndexSpanSection))
                    .setStyles(new ForegroundColorSpan(findedItemForegroundColor));


            // Set the new selected SpanSelection
            currentSearchIndexSpanSection--;
            spanSections.get(currentSearchIndexSpanSectionSp.get(currentSearchIndexSpanSection)).setStyles(
                    new ForegroundColorSpan(findedItemForegroundColor),
                    new BackgroundColorSpan(findedItemBackgroundColor)

            );


            notifyChanges();

        }
    }

    public void selectNextFind(){
        if(currentSearchIndexSpanSectionSp.size() >= currentSearchIndexSpanSection + 1){

            // we reset the previous SpanSelection
            Integer pos = currentSearchIndexSpanSectionSp.get(currentSearchIndexSpanSection);
            spanSections.get(pos)
                    .setStyles(new ForegroundColorSpan(findedItemForegroundColor));

            // Set the new selected SpanSelection
            currentSearchIndexSpanSection++;
            spanSections.get(currentSearchIndexSpanSectionSp.get(currentSearchIndexSpanSection)).setStyles(
                    new ForegroundColorSpan(findedItemForegroundColor),
                    new BackgroundColorSpan(findedItemBackgroundColor)

            );

            notifyChanges();
        }
    }


    public void replaceCurrentFind(String replacement){
        if (currentSearchIndexSpanSectionSp.size()>0 && currentSearchIndexSpanSection<spanSections.size()) {
            spanSections.get(currentSearchIndexSpanSectionSp.get(currentSearchIndexSpanSection)).setText(replacement);
            spanSections.get(currentSearchIndexSpanSectionSp.get(currentSearchIndexSpanSection)).setStyles(null);
            notifyChanges();
            selectNextFind();
        }
    }


    public void replaceAllFind(String replacement){
        if (currentSearchIndexSpanSectionSp.size()>0) {
            mTextToProcess = mTextToProcess.replaceAll(currentToFindText, replacement);
            resetFind();
            notifyChanges();
        }
    }

    public String getCurrentText(){
        build();
        return stringBuilder.toString();
    }

    private void notifyChanges(){
        mIndexTxtV.setText(
                mIndexTxtV.getResources().getString(
                        R.string.find_index_count,
                        currentSearchIndexSpanSection,
                        currentSearchIndexSpanSectionSp.size())
        );
        mTextView.setText(build());
    }



    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
