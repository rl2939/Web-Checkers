package com.webcheckers.ui;

import java.util.Iterator;

public class Row implements Iterator<Space> {
    private int index;

    public Row(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public Space next() {
        return null;
    }

    public void remove() {

    }

    public boolean hasNext() {
        return false;
    }

    public Iterator<Space> iterator() {
        return null;
    }
}