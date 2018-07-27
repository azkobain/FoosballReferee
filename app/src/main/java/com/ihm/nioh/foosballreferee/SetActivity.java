package com.ihm.nioh.foosballreferee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class SetActivity extends AppCompatActivity {
    private GameSet currentGameSet;
    private TextView outputInfo;
    private Button resetLeft;
    private Button resetRight;
    private boolean showResets = false;
    private Button timeoutLeft;
    private Button timeoutRight;
    private Button teamSide;
    private Button scoreLeft;
    private Button scoreRight;
    private Vibrator expiredVibrator;
    private CountDownTimer countDownTimer;
    private Boolean timerStart = false;
    private Boolean acceptNew = false;
    enum Sides {LEFT, RIGHT}
    Sides currentSide, goalSide;
    private boolean inHandler = false;

    private void startTimer(States state) {
        if (!inHandler) {
            inHandler = true;
            currentGameSet.setCurrentState(state);
            currentGameSet.setPreviousState(state);
            setTimer();
            if (!showResets) {
                resetLeft.setVisibility(View.VISIBLE);
                resetRight.setVisibility(View.VISIBLE);
                showResets = true;
            }
        }
    }

    private void setReset(Sides side, Button pressed) {
        if (!inHandler) {
            inHandler = true;
            int sidePosition = side.ordinal();

            currentGameSet.addReset(sidePosition);
            if (currentGameSet.getReset(sidePosition) > 1) {
                if (currentGameSet.getReset(sidePosition) % 2 == 0) {
                    String buttonNewText = getResources().getString(R.string.reset_second);

                    pressed.setText(buttonNewText);
                    if (currentGameSet.getPreviousState() != States.STOP)
                        currentGameSet.setCurrentState(currentGameSet.getPreviousState());
                } else {
                    String buttonNewText = getResources().getString(R.string.reset_third);

                    showResets = false;
                    resetRight.setVisibility(View.INVISIBLE);
                    resetLeft.setVisibility(View.INVISIBLE);

                    pressed.setText(buttonNewText);
                    outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 80);
                    currentGameSet.setCurrentState(States.STOP);
                }
            }
            setTimer();
        }
    }

    private void setScore(Sides side, Button pressed) {
        if (!inHandler) {
            inHandler = true;
            currentGameSet.incScore(side.ordinal());

            int score = currentGameSet.getScore(side.ordinal());

            pressed.setText(String.format(Locale.getDefault(), "%d", score));
            currentGameSet.setCurrentState(States.GOAL);
            currentGameSet.setCurrentTime(currentGameSet.getCurrentState().getTime());
            setTimer();
            currentGameSet.setSideNotFreeze(true);
            goalSide = side;
            setOwnerSide();
            currentGameSet.setSideNotFreeze(false);

            String reset = getResources().getString(R.string.reset_first);

            resetLeft.setText(reset);
            resetLeft.setVisibility(View.INVISIBLE);

            resetRight.setText(reset);
            resetRight.setVisibility(View.INVISIBLE);

            currentGameSet.setResets();

            showResets = false;
        }
    }

    private void setTimeout(Sides side, Button pressed) {
        if (!inHandler) {
            inHandler = true;
            if (currentGameSet.isSideNotFreeze())
                currentGameSet.setSideNotFreeze(false);

            int timeout = currentGameSet.getTimeout(side.ordinal());

            if (timeout > 0) {
                resetRight.setEnabled(false);
                resetLeft.setEnabled(false);
                currentGameSet.decTimeout(side.ordinal());
                timeout = currentGameSet.getTimeout(side.ordinal());
                pressed.setText(String.format(Locale.getDefault(), "%d", timeout));
                currentGameSet.setCurrentState(States.TIMEOUT);
                currentGameSet.setCurrentTime(currentGameSet.getCurrentState().getTime());
                setTimer();
            }
        }
    }

    private void setOwnerSide() {
        if (acceptNew)
            acceptNew = false;

        if (currentGameSet.isSideNotFreeze()) {
            if (currentGameSet.getCurrentState() == States.GOAL) {
                changeSide(goalSide);
            }
            else {
                changeSide(currentSide);
            }
        }
    }

    private void changeSide(Sides side) {
        if (side == Sides.LEFT) {
            String sideLogo = getResources().getString(R.string.team_right);

            currentSide = Sides.RIGHT;
            teamSide.setText(sideLogo);
        }
        else if (side == Sides.RIGHT) {
            String sideLogo = getResources().getString(R.string.team_left);

            currentSide = Sides.LEFT;
            teamSide.setText(sideLogo);
        }
    }

    private void setTimer() {
        if (acceptNew)
            acceptNew = false;

        if (currentGameSet.isSideNotFreeze())
            currentGameSet.setSideNotFreeze(false);

        if (currentGameSet.getCurrentState() != States.TIMEOUT) {
            resetLeft.setEnabled(true);
            resetRight.setEnabled(true);
        }

        if (timerStart)
            countDownTimer.cancel();

        currentGameSet.setCurrentTime(currentGameSet.getCurrentState().getTime());
        countDownTimer = new CountDownTimer(Math.round(currentGameSet.getCurrentTime()) * 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentGameSet.decCurrentTime();
                outputInfo.setText(String.format(Locale.getDefault(),"%.1f", currentGameSet.getCurrentTime()));
                outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 70);
            }

            @Override
            public void onFinish() {
                String outputLogo = getResources().getString(R.string.app_name);

                switch (currentGameSet.getCurrentState()) {
                    case GOAL:
                        outputLogo = getResources().getString(R.string.time_to_prepare);
                        outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                        break;
                    case TIMEOUT:
                        outputLogo = getResources().getString(R.string.timeout_expired);
                        break;
                    case GAME_MID:
                    case GAME_NO_MID:
                        outputLogo = getResources().getString(R.string.time_expired);
                        outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);
                        break;
                    case STOP:
                        outputLogo = getResources().getString(R.string.technical);
                        break;
                }
                outputInfo.setText(outputLogo);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    expiredVibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    expiredVibrator.vibrate(500);
                }
                timerStart = false;
            }
        }.start();
        timerStart = true;
    }

    private void startNewGameSet() {
        if (!inHandler) {
            inHandler = true;
            if (!acceptNew) {
                if (timerStart) {
                    timerStart = false;
                    countDownTimer.cancel();
                }
                String logo = getResources().getString(R.string.accept_new);

                outputInfo.setText(logo);
                outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
                acceptNew = true;
            } else {
                currentGameSet.newGameSet();

                String logo = getResources().getString(R.string.app_name);

                outputInfo.setText(logo);
                outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 70);


                String reset = getResources().getString(R.string.reset_first);

                resetLeft.setText(reset);
                resetLeft.setVisibility(View.INVISIBLE);

                resetRight.setText(reset);
                resetRight.setVisibility(View.INVISIBLE);

                showResets = false;

                timeoutLeft.setText(String.format(Locale.getDefault(), "%d", currentGameSet.getTimeout(0)));
                timeoutRight.setText(String.format(Locale.getDefault(), "%d", currentGameSet.getTimeout(0)));

                scoreLeft.setText(String.format(Locale.getDefault(), "%d", currentGameSet.getScore(0)));
                scoreRight.setText(String.format(Locale.getDefault(), "%d", currentGameSet.getScore(0)));

                if (timerStart) {
                    timerStart = false;
                    countDownTimer.cancel();
                }

                acceptNew = false;
            }
            inHandler = false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_set);

        currentGameSet = new GameSet();
        expiredVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        outputInfo = findViewById(R.id.output_content);

// Time section buttons

        final Button timeNoMid = findViewById(R.id.time_no_mid);
        timeNoMid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTimer(States.GAME_NO_MID);
                        return true;
                    case MotionEvent.ACTION_UP:
                        inHandler = false;
                        return true;
                }
                return false;
            }
        });

        final Button timeMid = findViewById(R.id.time_mid);
        timeMid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startTimer(States.GAME_MID);
                        return true;
                    case MotionEvent.ACTION_UP:
                        inHandler = false;
                        return true;
                }
                return false;
            }
        });

        resetLeft = findViewById(R.id.reset_left);
        resetLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setReset(Sides.LEFT, resetLeft);
                        return true;
                    case MotionEvent.ACTION_UP:
                        inHandler = false;
                        return true;
                }
                return false;
            }
        });
        resetLeft.setVisibility(View.INVISIBLE);

        resetRight = findViewById(R.id.reset_right);
        resetRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setReset(Sides.RIGHT, resetRight);
                        return true;
                    case MotionEvent.ACTION_UP:
                        inHandler = false;
                        return true;
                }
                return false;
            }
        });
        resetRight.setVisibility(View.INVISIBLE);
        showResets = false;

// Timeout section buttons

        timeoutLeft = findViewById(R.id.timeout_left);
        timeoutLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setTimeout(Sides.LEFT, timeoutLeft);
                        return true;
                    case MotionEvent.ACTION_UP:
                        inHandler = false;
                        return true;
                }
                return false;
            }
        });

        timeoutRight = findViewById(R.id.timeout_right);
        timeoutRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setTimeout(Sides.RIGHT, timeoutRight);
                        return true;
                    case MotionEvent.ACTION_UP:
                        inHandler = false;
                        return true;
                }
                return false;
            }
        });

// Low section buttons

        teamSide = findViewById(R.id.team_side);
        currentSide = Sides.LEFT;
        teamSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOwnerSide();
            }
        });

        final Button startGame = findViewById(R.id.new_game);
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGameSet();
            }
        });

        scoreLeft = findViewById(R.id.score_left);
        scoreLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setScore(Sides.LEFT, scoreLeft);
                        return true;
                    case MotionEvent.ACTION_UP:
                        inHandler = false;
                        return true;
                }
                return false;
            }
        });

        scoreRight = findViewById(R.id.score_right);
        scoreRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setScore(Sides.RIGHT, scoreRight);
                        return true;
                    case MotionEvent.ACTION_UP:
                        inHandler = false;
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            startTimer(States.GAME_NO_MID);
            inHandler = false;
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            startTimer(States.GAME_MID);
            inHandler = false;
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
