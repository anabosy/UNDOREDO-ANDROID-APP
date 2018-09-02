package com.undoredo.app.eventbus;

public class OnFindReplaceDone {
    String newText;

    public OnFindReplaceDone(String newText) {
        this.newText = newText;
    }

    public String getNewText() {
        return newText;
    }
}
