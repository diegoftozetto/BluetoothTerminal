package com.example.chronopassbluetoothterminal.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.chronopassbluetoothterminal.R;

public class MatrixItemView extends FrameLayout {

    private ImageView ivLed;

    public MatrixItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.style_matrix, this);
        ivLed = getRootView().findViewById(R.id.iv_led);
        ivLed.setBackgroundColor(getContext().getResources().getColor(R.color.color_matrix_default));
    }

    public void display(boolean isSelected, int color) {
        ivLed.setBackgroundColor(isSelected ? color :
                getContext().getResources().getColor(R.color.color_matrix_default));
    }
}