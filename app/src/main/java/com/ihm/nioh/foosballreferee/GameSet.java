package com.ihm.nioh.foosballreferee;

enum States {
    UNDO(0), STOP(0), GAME_MID(10), GAME_NO_MID(15), GOAL(5), TIMEOUT(30), BSETS(90), BGAMES(180);

    private final double time;

    States(double time) {
        this.time = time;
    }

    double getTime() {
        return time;
    }
}

public class GameSet {
    private final int[] resets = {0, 0};
    private final int[] score = {0, 0};
    private final int[] timeouts = {2, 2};
    private double currentTime = 0;
    private States currentState = States.STOP;
    private States previousState = States.STOP;
    private boolean sideNotFreeze = true;

    GameSet() {
        newGameSet();
    }

    void newGameSet() {
        for (int i = 0; i < 2; i++) {
            resets[i] = 0;
            score[i] = 0;
            timeouts[i] = 2;
        }
        currentTime = 0;
        currentState = States.STOP;
        previousState = States.STOP;
        sideNotFreeze = true;
    }

    void setResets() {
        for (int i = 0; i < 2; i++)
            if (resets[i] > 0)
                resets[i] = 1;
    }
    void addReset(int i) {
        resets[i]++;
    }
    int getReset(int i) {
        return resets[i];
    }

    void decScore(int i) {
        score[i]--;
    }
    void incScore(int i) {
        score[i]++;
    }
    int getScore(int i) {
        return score[i];
    }

    void decTimeout(int i) {
        timeouts[i]--;
    }
    void incTimeout(int i) {
        timeouts[i]++;
    }
    int getTimeout(int i) {
        return timeouts[i];
    }

    void setCurrentTime(double time) {
        currentTime = time;
    }
    void decCurrentTime() {
        currentTime -= 0.1;
    }
    double getCurrentTime() {
        return currentTime;
    }

    void setCurrentState(States state) {
        currentState = state;
    }
    States getCurrentState() {
        return currentState;
    }

    void setPreviousState(States state) {
        previousState = state;
    }
    States getPreviousState() {
        return previousState;
    }

    void setSideNotFreeze(boolean isFreeze) {
        sideNotFreeze = isFreeze;
    }
    boolean isSideNotFreeze() {
        return sideNotFreeze;
    }
}