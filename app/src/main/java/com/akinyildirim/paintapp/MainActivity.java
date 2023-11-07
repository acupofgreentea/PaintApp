package com.akinyildirim.paintapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.akinyildirim.paintapp.databinding.ActivityMainBinding;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.flag.BubbleFlag;
import com.skydoves.colorpickerview.flag.FlagMode;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private DrawView paint;
    private int lastColor = Color.BLACK;
    private SeekBar strokeSlider;
    private ImageView colorPicker;
    private ColorPickerDialog.Builder colorPickerDialog;
    private ColorPickerView colorPickerView;

    private boolean keepSplashScreen = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                keepSplashScreen = false;
            }
        }, 2000);

        splashScreen.setKeepOnScreenCondition(() -> keepSplashScreen);

        setContentView(binding.getRoot());

        paint = findViewById(R.id.drawView2);

        paint.addDrawStartListener(new DrawStartListener() {
            @Override
            public void onDrawStart() {
                closeStrokeSlider();
            }
        });

        strokeSlider = binding.strokeSlider;
        strokeSlider.setVisibility(View.INVISIBLE);
        colorPicker = binding.colorPicker;

        colorPicker.setOnClickListener(v -> {
            showColorPicker();
        });

        binding.clearbutton.setOnClickListener(v -> {
            askForClear();
        });

        binding.undo.setOnClickListener(v -> {
            paint.undo();
        });

        binding.save.setOnClickListener(v -> {
            paint.save();
        });

        strokeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                paint.setStrokeWidth(progressChangedValue);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        binding.eraser.setOnClickListener(v -> {
            lastColor = paint.getColor();
            paint.setColor(Color.WHITE);
            openStrokeSlider();
        });

        binding.pencil.setOnClickListener(v -> {
            paint.setColor(lastColor);
            Log.e("pencil", "pencil pressed");
            openStrokeSlider();
        });

        ViewTreeObserver vto = paint.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                paint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = paint.getMeasuredWidth();
                int height = paint.getMeasuredHeight();
                paint.init(height, width);
            }
        });
        paint.setStrokeWidth(strokeSlider.getProgress());
    }

    private void askForClear(){
        if(paint.isEmpty())
            return;

        AlertDialog clearOption = new AlertDialog.Builder(this)
                .setTitle("Clear Canvas")
                .setMessage("Are you sure to clear canvas?")
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        paint.clear();
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        clearOption.show();
    }

    private void closeStrokeSlider(){
        if(strokeSlider.getVisibility() == View.INVISIBLE){
            return;
        }

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.stroke_slider_close);
        strokeSlider.startAnimation(anim);
        strokeSlider.setVisibility(View.VISIBLE);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                strokeSlider.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                strokeSlider.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void openStrokeSlider(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.stroke_slider_open);

        strokeSlider.startAnimation(anim);
        strokeSlider.setClickable(false);
        strokeSlider.setVisibility(View.VISIBLE);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                strokeSlider.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void showColorPicker(){
        BubbleFlag bubbleFlag = new BubbleFlag(this);
        bubbleFlag.setFlagMode(FlagMode.FADE);
        colorPickerDialog = new ColorPickerDialog.Builder(this);

        colorPickerDialog
                .setTitle("ColorPicker")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                Color color = Color.valueOf(envelope.getColor());
                                paint.setColor(color.toArgb());
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(false)
                .setBottomSpace(1)
                .setCancelable(true).show();

            colorPickerView = colorPickerDialog.getColorPickerView();
            colorPickerView.setFlagView(bubbleFlag);
    }
}