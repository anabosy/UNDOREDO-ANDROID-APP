package com.undoredo.app.model;


import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class TextInputed {
    @Id
    long id;

    String packageName;
    String text;
    Date created;


    public TextInputed(long id, String packageName, String text, Date created) {
        this.id = id;
        this.packageName = packageName;
        this.text = text;
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public TextInputed setId(long id) {
        this.id = id;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public TextInputed setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public String getText() {
        return text;
    }

    public TextInputed setText(String text) {
        this.text = text;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public TextInputed setCreated(Date created) {
        this.created = created;
        return this;
    }
}
