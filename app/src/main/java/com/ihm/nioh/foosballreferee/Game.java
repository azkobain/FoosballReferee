package com.ihm.nioh.foosballreferee;

import android.util.Log;

import java.util.ArrayList;

public class Game {
    private final int goals;
    private final int balance;
    private final int[] results = {0, 0};
    private final ArrayList<GameSet> arraySets = new ArrayList<>();
    private int currentSet;

    public Game(int pSets, int pGoals, int pBal) {
        goals = pGoals;
        balance = pBal;
        for (int i = 0; i < pSets; i++) {
            GameSet tSet = new GameSet();
            arraySets.add(tSet);
        }
        currentSet = 0;
    }

    public final int getGoals() { return goals; }

    public final int getBalance() { return balance; }

    public final int getSets() { return arraySets.size(); }

    public final int getResult(int i) { return results[i]; }

    public final int getCurrentSet() { return currentSet; }

    public void setResult(int i) { results[i]++; }

    public void zeroResults() {
        results[0] = results[1] = 0;
        for (GameSet set: arraySets)
            set.newGameSet();
    }

    public void zeroSets() { currentSet = 0; }

    public GameSet getSet() {
        Log.i("getSet", "currentSet " + currentSet);
        if (currentSet < arraySets.size())
            return arraySets.get(currentSet++);
        else
            return null;
    }
}
