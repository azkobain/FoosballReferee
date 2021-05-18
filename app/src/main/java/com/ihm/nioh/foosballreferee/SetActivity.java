package com.ihm.nioh.foosballreferee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Locale;
import android.util.Log;

public class SetActivity extends AppCompatActivity {
    private final ArrayList<Game> games = new ArrayList<>();
    private Game currentGame;
    private GameSet currentGameSet;
    private TextView outputInfo;
    private Button resetLeft;
    private Button resetRight;
    private Button timeNoMid;
    private Button timeMid;
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
    private boolean inLongClick = false;
    private int winSets;
    private int nGame;
    private boolean isFreeze = false;

    public void freezeButtons(boolean freeze) {
        timeNoMid.setEnabled(!freeze);
        timeMid.setEnabled(!freeze);
        resetLeft.setEnabled(!freeze);
        resetRight.setEnabled(!freeze);
        timeoutLeft.setEnabled(!freeze);
        timeoutRight.setEnabled(!freeze);
        teamSide.setEnabled(!freeze);
        scoreLeft.setEnabled(!freeze);
        scoreRight.setEnabled(!freeze);
        isFreeze = freeze;
    }

    private void startTimer(States state) {
        if (!inHandler) {
            inHandler = true;
            currentGameSet.setCurrentState(state);
            currentGameSet.setPreviousState(state);
            setTimer();
        }
    }

    private void showResets(boolean showRes) {
        if (showRes) {
            resetLeft.setVisibility(View.VISIBLE);
            resetRight.setVisibility(View.VISIBLE);
        }
        else {
            resetLeft.setVisibility(View.INVISIBLE);
            resetRight.setVisibility(View.INVISIBLE);
        }
        resetLeft.setEnabled(showRes);
        resetRight.setEnabled(showRes);
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

                    showResets(false);

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
            resetRight.setText(reset);
            currentGameSet.setResets();
            showResets(false);
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
        if (currentGameSet.isSideNotFreeze())
            currentGameSet.setSideNotFreeze(false);

        showResets(currentGameSet.getCurrentState() != States.TIMEOUT &&
                   currentGameSet.getCurrentState() != States.GOAL &&
                   currentGameSet.getCurrentState() != States.BSETS &&
                   currentGameSet.getCurrentState() != States.BGAMES);

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
                    case BSETS:
                    case BGAMES:
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
                    case UNDO:
                        outputLogo = getResources().getString(R.string.app_name);
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

    private void startGameSet() {
        if (!inHandler) {
            inHandler = true;

            if (isFreeze)
                freezeButtons(false);

            currentGameSet.newGameSet();

            String reset = getResources().getString(R.string.reset_first);

            resetLeft.setText(reset);
            resetRight.setText(reset);
            showResets(false);

            timeoutLeft.setText(String.format(Locale.getDefault(), "%d", currentGameSet.getTimeout(0)));
            timeoutRight.setText(String.format(Locale.getDefault(), "%d", currentGameSet.getTimeout(1)));

            scoreLeft.setText(String.format(Locale.getDefault(), "%d", currentGameSet.getScore(0)));
            scoreRight.setText(String.format(Locale.getDefault(), "%d", currentGameSet.getScore(1)));

            if (timerStart) {
                timerStart = false;
                countDownTimer.cancel();
            }

            inHandler = false;
        }
    }

    private void restartGame() {
        String logo = getResources().getString(R.string.app_name);

        outputInfo.setText(logo);
        outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 70);
        for ( Game game: games ) {
            game.zeroResults();
            game.zeroSets();
        }
        nGame = 0;
        currentGame = games.get(nGame++);
        currentGameSet = currentGame.getSet();
        startGameSet();
        currentGameSet.setCurrentState(States.BGAMES);
    }

    private boolean isBalance() {
        Log.i("isBalance", "currentGame.getBalance() > currentGame.getGoals() " + (currentGame.getBalance() > currentGame.getGoals()) );
        Log.i("isBalance", "currentGame.getResult(0) == currentGame.getResult(1) " + (currentGame.getResult(0) == currentGame.getResult(1)) );
        Log.i("isBalance", "currentGame.getCurrentSet() == currentGame.getSets() " + (currentGame.getCurrentSet() == currentGame.getSets()) );
        Log.i("isBalance", "currentGameSet.getScore(0) >= currentGame.getGoals() - 1 " + (currentGameSet.getScore(0) >= currentGame.getGoals() - 1) );
        Log.i("isBalance", "currentGameSet.getScore(1) >= currentGame.getGoals() - 1 " + (currentGameSet.getScore(1) >= currentGame.getGoals() - 1) );
        return currentGame.getBalance() > currentGame.getGoals() &&
               currentGame.getResult(0) == currentGame.getResult(1) &&
               currentGame.getCurrentSet() == currentGame.getSets() &&
               currentGameSet.getScore(0) >= currentGame.getGoals() - 1 &&
               currentGameSet.getScore(1) >= currentGame.getGoals() - 1 ; }

    private boolean isBalanceWin(Sides side) {
        Log.i("isBalanceWin", "currentGameSet.getScore(side.ordinal()) == currentGame.getBalance() " + (currentGameSet.getScore(side.ordinal()) == currentGame.getBalance()) );
        Log.i("isBalanceWin", "currentGameSet.getScore(side.ordinal()) == 2 + currentGameSet.getScore(1 - side.ordinal()) " + (currentGameSet.getScore(side.ordinal()) == 2 + currentGameSet.getScore(1 - side.ordinal())) );
        return currentGameSet.getScore(side.ordinal()) == currentGame.getBalance() ||
               currentGameSet.getScore(side.ordinal()) == 2 + currentGameSet.getScore(1 - side.ordinal());
    }

    private boolean isLastSetGoal(Sides side) {
        Log.i("isLastSetGoal", "score " + currentGameSet.getScore(side.ordinal())+", goals "+currentGame.getGoals());
        return currentGame.getGoals() == currentGameSet.getScore(side.ordinal()); }

    private boolean isLastGameGoal(Sides side) {
        return currentGame.getResult(side.ordinal()) == winSets ||
               currentGame.getResult(0) + currentGame.getResult(1) == currentGame.getSets(); }

    private boolean isLastGame() { return nGame == games.size(); }

    private void waitNewGame() {
        countDownTimer.cancel();
        freezeButtons(true);
        String logo = getResources().getString(R.string.app_name);
        outputInfo.setText(logo);
        outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 70);
    }

    private void startNewGame() {
        Log.i("startNewGame", "game "+ nGame);
        currentGame = games.get(nGame++);
        currentGameSet = currentGame.getSet();
        inHandler = false;
        startGameSet();
        showResets(false);
        currentGameSet.setCurrentState(States.BGAMES);
        startTimer(States.BGAMES);
    }

    private void startNewSet() {
        currentGameSet = currentGame.getSet();
        inHandler = false;
        startGameSet();
        showResets(false);
        currentGameSet.setCurrentState(States.BSETS);
        startTimer(States.BSETS);
    }

    @SuppressLint({"ClickableViewAccessibility", "SourceLockedOrientationActivity"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_set);

        Intent intent = getIntent();
        int nGames = intent.getIntExtra("games", 1);
        for (int i = 0; i < nGames; i++) {
            int nSets = intent.getIntExtra("sets", 1);
            int nGoals = intent.getIntExtra("goals", 5);
            int nBalance = intent.getIntExtra("balance", 5);
            winSets = nSets / 2 + 1;
            games.add(new Game(nSets, nGoals, nBalance));
        }
        nGame = 0;
        currentGame = games.get(nGame++);
        currentGameSet = currentGame.getSet();
        expiredVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        outputInfo = findViewById(R.id.output_content);

// Time section buttons

        timeNoMid = findViewById(R.id.time_no_mid);
        timeNoMid.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startTimer(States.GAME_NO_MID);
                    return true;
                case MotionEvent.ACTION_UP:
                    inHandler = false;
                    return true;
            }
            return false;
        });

        timeMid = findViewById(R.id.time_mid);
        timeMid.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startTimer(States.GAME_MID);
                    return true;
                case MotionEvent.ACTION_UP:
                    inHandler = false;
                    return true;
            }
            return false;
        });

        resetLeft = findViewById(R.id.reset_left);
        resetLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setReset(Sides.LEFT, resetLeft);
                    return true;
                case MotionEvent.ACTION_UP:
                    inHandler = false;
                    return true;
            }
            return false;
        });

        resetRight = findViewById(R.id.reset_right);
        resetRight.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setReset(Sides.RIGHT, resetRight);
                    return true;
                case MotionEvent.ACTION_UP:
                    inHandler = false;
                    return true;
            }
            return false;
        });
        showResets(false);

// Timeout section buttons

        timeoutLeft = findViewById(R.id.timeout_left);
        timeoutLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return false;
                case MotionEvent.ACTION_UP:
                    if (inLongClick) {
                        int timeout = currentGameSet.getTimeout(Sides.LEFT.ordinal());

                        if (timeout < 2) {
                            currentGameSet.incTimeout(Sides.LEFT.ordinal());
                            timeout = currentGameSet.getTimeout(Sides.LEFT.ordinal());
                            timeoutLeft.setText(String.format(Locale.getDefault(), "%d", timeout));
                            currentGameSet.setCurrentState(States.UNDO);
                            currentGameSet.setCurrentTime(currentGameSet.getCurrentState().getTime());
                            setTimer();
                        }
                        inLongClick = false;
                    }
                    else
                        setTimeout(Sides.LEFT, timeoutLeft);
                    inHandler = false;
                    return false;
            }
            return false;
        });
        timeoutLeft.setOnLongClickListener(v -> {
            inLongClick = true;
            return false;
        });

        timeoutRight = findViewById(R.id.timeout_right);
        timeoutRight.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return false;
                case MotionEvent.ACTION_UP:
                    if (inLongClick) {
                        int timeout = currentGameSet.getTimeout(Sides.RIGHT.ordinal());

                        if (timeout < 2) {
                            currentGameSet.incTimeout(Sides.RIGHT.ordinal());
                            timeout = currentGameSet.getTimeout(Sides.RIGHT.ordinal());
                            timeoutRight.setText(String.format(Locale.getDefault(), "%d", timeout));
                            currentGameSet.setCurrentState(States.UNDO);
                            currentGameSet.setCurrentTime(currentGameSet.getCurrentState().getTime());
                            setTimer();
                        }
                        inLongClick = false;
                    }
                    else
                        setTimeout(Sides.RIGHT, timeoutRight);
                    inHandler = false;
                    return false;
            }
            return false;
        });
        timeoutRight.setOnLongClickListener(v -> {
            inLongClick = true;
            return false;
        });

// Low section buttons

        teamSide = findViewById(R.id.team_side);
        currentSide = Sides.LEFT;
        teamSide.setOnClickListener(v -> setOwnerSide());

        final Button startGame = findViewById(R.id.new_game);
        startGame.setOnClickListener(v -> {
            acceptNew = !acceptNew;
            if (acceptNew) {
                if (timerStart) {
                    timerStart = false;
                    countDownTimer.cancel();
                }
                String logo = getResources().getString(R.string.accept_new);

                outputInfo.setText(logo);
                outputInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
                acceptNew = true;
            }
            else
                restartGame();
        });

        scoreLeft = findViewById(R.id.score_left);
        scoreLeft.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return false;
                case MotionEvent.ACTION_UP:
                    if (inLongClick) {
                        int score = currentGameSet.getScore(Sides.LEFT.ordinal());

                        if (score > 0) {
                            currentGameSet.decScore(Sides.LEFT.ordinal());
                            score = currentGameSet.getScore(Sides.LEFT.ordinal());
                            scoreLeft.setText(String.format(Locale.getDefault(), "%d", score));
                            currentGameSet.setCurrentState(States.UNDO);
                            currentGameSet.setCurrentTime(currentGameSet.getCurrentState().getTime());
                            setTimer();
                        }
                        inLongClick = false;
                    }
                    else {
                        setScore(Sides.LEFT, scoreLeft);
                        if (isBalance()) {
                            Log.i("LeftBalance", "LeftBalance");
                            if (isBalanceWin(Sides.LEFT)) {
                                Log.i("isBalanceWin", "LeftWin");
                                currentGame.setResult(Sides.LEFT.ordinal());
                                waitNewGame();
                            }
                        }
                        else {
                            if (isLastSetGoal(Sides.LEFT)) {
                                Log.i("if", "isLastSetGoal");
                                currentGame.setResult(Sides.LEFT.ordinal());
                                if (isLastGameGoal(Sides.LEFT)) {
                                    Log.i("if", "isLastGameGoal");
                                    if (isLastGame()) {
                                        Log.i("if", "isLastGame");
                                        waitNewGame();
                                    }
                                    else {
                                        Log.i("if", "!isLastGame");
                                        startNewGame();
                                    }
                                }
                                else {
                                    Log.i("if", "!isLastGameGoal");
                                    startNewSet();
                                }
                            }
                        }
                    }
                    inHandler = false;
                    return false;
            }
            return false;
        });
        scoreLeft.setOnLongClickListener(v -> {
            inLongClick = true;
            return false;
        });

        scoreRight = findViewById(R.id.score_right);
        scoreRight.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return false;
                case MotionEvent.ACTION_UP:
                    if (inLongClick) {
                        int score = currentGameSet.getScore(Sides.RIGHT.ordinal());

                        if (score > 0) {
                            currentGameSet.decScore(Sides.RIGHT.ordinal());
                            score = currentGameSet.getScore(Sides.RIGHT.ordinal());
                            scoreRight.setText(String.format(Locale.getDefault(), "%d", score));
                            currentGameSet.setCurrentState(States.UNDO);
                            currentGameSet.setCurrentTime(currentGameSet.getCurrentState().getTime());
                            setTimer();
                        }
                        inLongClick = false;
                    }
                    else {
                        setScore(Sides.RIGHT, scoreRight);
                        if (isBalance()) {
                            Log.i("RightBalance", "RightBalance");
                            if (isBalanceWin(Sides.RIGHT)) {
                                Log.i("isBalanceWin", "RightWin");
                                currentGame.setResult(Sides.RIGHT.ordinal());
                                waitNewGame();
                            }
                        }
                        else {
                            if (isLastSetGoal(Sides.RIGHT)) {
                                Log.i("if", "isLastSetGoal");
                                currentGame.setResult(Sides.RIGHT.ordinal());
                                if (isLastGameGoal(Sides.RIGHT)) {
                                    Log.i("if", "isLastGameGoal");
                                    if (isLastGame()) {
                                        Log.i("if", "isLastGame");
                                        waitNewGame();
                                    }
                                    else {
                                        Log.i("if", "!isLastGame");
                                        startNewGame();
                                    }
                                }
                                else {
                                    Log.i("if", "!isLastGameGoal");
                                    startNewSet();
                                }
                            }
                        }
                    }
                    inHandler = false;
                    return false;
            }
            return false;
        });
        scoreRight.setOnLongClickListener(v -> {
            inLongClick = true;
            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isFreeze) {
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
        return true;
    }

    @Override
    public void onBackPressed() {
        countDownTimer.cancel();
        startActivity(new Intent(this, FormatActivity.class));
    }
}
