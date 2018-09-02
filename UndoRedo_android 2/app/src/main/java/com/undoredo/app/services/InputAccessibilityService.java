package com.undoredo.app.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.undoredo.app.BuildConfig;
import com.undoredo.app.MyApplication;
import com.undoredo.app.R;
import com.undoredo.app.eventbus.FindReplaceEvent;
import com.undoredo.app.eventbus.OnFindReplaceDone;
import com.undoredo.app.eventbus.RedoEvent;
import com.undoredo.app.eventbus.UndoEvent;
import com.undoredo.app.find_replace.FindReplaceContainer;
import com.undoredo.app.model.TextInputed;
import com.undoredo.app.model.TextInputed_;
import com.undoredo.app.utils.MyUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;


/**
 * This service is reponsible of listening the user actions (Editext selected, Editext content changed,
 * Undo/Redo text change.. etc).
 * We use {@link AccessibilityService} to be able to receive events for those listed actions.
 *
 * Basically when the Service is Enable and authorized by the user (in Android Settings):
 *   - we display the 3 buttons (Undo, Find, Redo) when an Editext is focused
 *   - When the user edit the text of the selected Editext, we save the new text in the database
 *   - When the user ask to Undo/Redo the text modification, we retreive the saved text and set it to the selected Editext
 */
public class InputAccessibilityService extends AccessibilityService {

    public static String TAG = "InputAccessibilityService";
    private static InputAccessibilityService mInstance;
    private boolean isRunningFVService = false;
    private Box<TextInputed> mTextInputedBox;

    private final String SP_CURRENT_INDEX_TEXTINPUTED = "current_index_textinputed";

    private volatile AccessibilityNodeInfo currentSource, currentEditableSource;
    private TextInputed lastTextInputed;
    private boolean shouldApplyOnFindReplaceTextEditFinish;
    private String newOnFindReplaceTextEditFinishString;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mInstance = this;
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onInterrupt() {
    }

    /**
     * onServiceConnected is called by the system usally when the Service is started and the user
     * authorised it in the Android Settings
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // we retreive the ObjectBox database manager
        mTextInputedBox = MyApplication.getInstance().getBoxStore().boxFor(TextInputed.class);

        // register to receive EventBus events
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // if the app is not allowed to display content on top of other app, we ask the user to authorize
        // it by launching the Settings Application
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // askPermission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, R.string.askpermission_system_alert_window, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * This methods receive the Accessibilty Events
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        String type = "";

        switch (event.getEventType()){
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                type="TYPE_VIEW_CLICKED";
                treatEvent(event);
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                type="TYPE_VIEW_FOCUSED";
                treatEvent(event);
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                type="TYPE_VIEW_TEXT_CHANGED";
                if (event.getSource() != null && !event.getPackageName().toString().contains(BuildConfig.APPLICATION_ID)) {
                    String newText = "" + event.getSource().getText();
                    if (lastTextInputed == null || !newText.equals(lastTextInputed.getText())) {
                        addTextInputed(event.getSource());
                    }
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                type="TYPE_VIEW_TEXT_SELECTION_CHANGED";
                treatEvent(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if(shouldApplyOnFindReplaceTextEditFinish){
                    setTextToSource(this.currentEditableSource, newOnFindReplaceTextEditFinishString);
                    shouldApplyOnFindReplaceTextEditFinish = false;
                    newOnFindReplaceTextEditFinishString = null;
                }
                break;
            default:
                if( findFocus(AccessibilityNodeInfo.FOCUS_INPUT) == null){
                    // the windows don't contain any inputs so we close the undoredo floating button or notification
                    stopFloatingViewService();
                }
        }
    }



    @Override
    public void onDestroy() {
        // when thi service is stoped, we unregister it from receiving EventBus Events
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * This methods is call by EventBus when an UndoEvent is received
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doUndoAction(UndoEvent event){
        setUndoTextToAccessibilitySource(this.currentSource);
    }

    /**
     * This methods is call by EventBus when an FindReplaceEvent is received
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doFindReplaceAction(FindReplaceEvent event){
        if (currentEditableSource != null && currentEditableSource.getText() != null) {
            FindReplaceContainer findReplaceContainer = new FindReplaceContainer(getApplicationContext(), this.currentEditableSource.getText().toString());
            findReplaceContainer.show();
        }
    }


    /**
     * This methods is call by EventBus when an OnFindReplaceDone
     * (when the user have finished Find/Replacing text) is received
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doOnFindReplaceDoneAction(OnFindReplaceDone event){
        onFindReplaceFragmentTextEditFinish(event.getNewText());
    }

    /**
     * This methods is call by EventBus when an RedoEvent is received
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doRedoAction(RedoEvent event){
        setRedoTextToAccessibilitySource(this.currentSource);
    }


    /**
     * Depending on the event source we show on hide the 3 buttons overlays
     * @param event
     */
    private void treatEvent(AccessibilityEvent event){
        currentSource = event.getSource();
        if (currentSource!=null && currentSource.isEditable()){
            if (!currentSource.getPackageName().toString().contains(BuildConfig.APPLICATION_ID)) {
                currentEditableSource = event.getSource();
                startFloatingViewService();
            }

        }else{
            stopFloatingViewService();
        }
    }

    /**
     * Method to display the 3 overlays buttons
     */
    private void startFloatingViewService(){
        if (!isRunningFVService){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                startService(new Intent(InputAccessibilityService.this, UndoRedoFloatingWidgetService.class));
            } else if (Settings.canDrawOverlays(this)) {
                startService(new Intent(InputAccessibilityService.this, UndoRedoFloatingWidgetService.class));
            } else {
                // UndoRedo can't work
                Toast.makeText(this, R.string.undoredo_cant_work_system_alert_window_disabled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Methode to hide the 3 overlays buttons
     */
    private void stopFloatingViewService(){
        stopService(new Intent(InputAccessibilityService.this, UndoRedoFloatingWidgetService.class));
    }


    /**
     * Method to add a text in the database
     * @param source
     */
    private void addTextInputed(AccessibilityNodeInfo source){
        TextInputed textInputed = new TextInputed(0,
                source.getPackageName().toString(),
                source.getText().toString(),
                new Date());
        mTextInputedBox.put(textInputed);
        setCurrentIndexTextInputed(textInputed);
    }


    /**
     * Methode to retreive the Undo text and set it to the Editext
     * @param source
     */
    private synchronized void setUndoTextToAccessibilitySource(AccessibilityNodeInfo source){

        QueryBuilder<TextInputed> builder = mTextInputedBox.query();
        TextInputed txtInputed = null;

        long cursorIndex = MyUtils.getLongSharedPref(InputAccessibilityService.this, SP_CURRENT_INDEX_TEXTINPUTED);

        if (cursorIndex > 0) {
            builder.less(TextInputed_.id, cursorIndex);
        }
        builder.orderDesc(TextInputed_.id);

        txtInputed = builder.build().findFirst();

        if (txtInputed != null) {
            setTextToSource(source, txtInputed.getText());
            setCurrentIndexTextInputed(txtInputed);
        }

    }


    /**
     * Method to retreive the Redo text and set it to the Editext
     * @param source
     */
    private void setRedoTextToAccessibilitySource(AccessibilityNodeInfo source){

        QueryBuilder<TextInputed> builder = mTextInputedBox.query();
        TextInputed txtInputed = null;

        long cursorIndex = MyUtils.getLongSharedPref(InputAccessibilityService.this, SP_CURRENT_INDEX_TEXTINPUTED);

        if (cursorIndex > 0){
            builder.greater(TextInputed_.id, cursorIndex);
        }

        txtInputed = builder.build().findFirst();

        if (txtInputed != null) {
            setTextToSource(source, txtInputed.getText());
            setCurrentIndexTextInputed(txtInputed);
        }
    }

    /**
     * Methode that set text to the Editext (Source)
     * @param source
     * @param text
     */
    private void setTextToSource(AccessibilityNodeInfo source, String text){

        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text);
        source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        source.performAction(AccessibilityNodeInfo.MOVEMENT_GRANULARITY_LINE);
    }


    private void setCurrentIndexTextInputed(@NonNull TextInputed textInputed){
        lastTextInputed = textInputed;
        MyUtils.setLongSharedPref(InputAccessibilityService.this, SP_CURRENT_INDEX_TEXTINPUTED, textInputed.getId());
    }



    private void onFindReplaceFragmentTextEditFinish(String newString) {
        // set the new string to the current source
        setTextToSource(this.currentEditableSource, newString);
        shouldApplyOnFindReplaceTextEditFinish = true;
        newOnFindReplaceTextEditFinishString = newString;
    }
}
