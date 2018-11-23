package com.example.robmillaci.go4lunch.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Custom textview class that applies a custom font to all instances
 */
public class CustomTextView extends AppCompatTextView {
    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        String fontPath = "fonts/EraserRegular.ttf";
        Typeface customTypeFace = Typeface.createFromAsset(context.getAssets(), fontPath);
        setTypeface(customTypeFace);
    }

    @Override
    public void setTextAppearance(Context context, int resId) {
        super.setTextAppearance(context, resId);
        applyCustomFont(context);
    }
}
