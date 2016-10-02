package com.wh0_cares.projectstk.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class SearchBox extends com.quinny898.library.persistentsearch.SearchBox {
    public SearchBox(Context context) {
        super(context);
    }

    public SearchBox(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        return false;
    }
}