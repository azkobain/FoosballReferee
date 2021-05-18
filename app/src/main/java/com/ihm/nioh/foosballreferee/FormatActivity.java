package com.ihm.nioh.foosballreferee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class FormatActivity extends AppCompatActivity {
    private TextView selFormat;
    private Button bLeague;
    private Button bBoN;
    private TextView selLeague;
    private RadioGroup leagueFormat;
    private RadioButton div12;
    private RadioButton funDiv;
    private Button bStartL;
    private TextView confFormat;
    private TextView confSets;
    private HorNumberPicker npSets;
    private TextView confGoals;
    private HorNumberPicker npGoals;
    private TextView confBalance;
    private HorNumberPicker npBal;
    private Button bStartB;
    private int state;
    private Intent intent;

    private void onFormat() {
        selFormat.setVisibility(View.VISIBLE);
        bLeague.setVisibility(View.VISIBLE);
        bBoN.setVisibility(View.VISIBLE);
    }
    private void offFormat() {
        selFormat.setVisibility(View.INVISIBLE);
        bLeague.setVisibility(View.INVISIBLE);
        bBoN.setVisibility(View.INVISIBLE);
    }

    private void onLeague() {
        selLeague.setVisibility(View.VISIBLE);
        leagueFormat.setVisibility(View.VISIBLE);
        bStartL.setVisibility(View.VISIBLE);
    }
    private void offLeague() {
        selLeague.setVisibility(View.INVISIBLE);
        leagueFormat.setVisibility(View.INVISIBLE);
        bStartL.setVisibility(View.INVISIBLE);
    }

    private void onBon() {
        confFormat.setVisibility(View.VISIBLE);
        confSets.setVisibility(View.VISIBLE);
        confGoals.setVisibility(View.VISIBLE);
        confBalance.setVisibility(View.VISIBLE);
        npSets.setVisibility(View.VISIBLE);
        npGoals.setVisibility(View.VISIBLE);
        npBal.setVisibility(View.VISIBLE);
        npSets.setValue(1);
        npSets.setMin(1);
        npSets.setMax(7);
        npGoals.setValue(1);
        npGoals.setMin(1);
        npGoals.setMax(10);
        npBal.setValue(1);
        npBal.setMin(1);
        npBal.setMax(10);
        bStartB.setVisibility(View.VISIBLE);
    }
    private void offBon() {
        confFormat.setVisibility(View.INVISIBLE);
        confSets.setVisibility(View.INVISIBLE);
        confGoals.setVisibility(View.INVISIBLE);
        confBalance.setVisibility(View.INVISIBLE);
        npSets.setVisibility(View.INVISIBLE);
        npGoals.setVisibility(View.INVISIBLE);
        npBal.setVisibility(View.INVISIBLE);
        bStartB.setVisibility(View.INVISIBLE);
    }

    @SuppressLint({"ClickableViewAccessibility", "SourceLockedOrientationActivity"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_format);

        intent = new Intent(this, SetActivity.class);
        selFormat = findViewById(R.id.format);
        bLeague = findViewById(R.id.league);
        bBoN = findViewById(R.id.bon);
        selLeague = findViewById(R.id.tvleague);
        leagueFormat = findViewById(R.id.divs);
        div12 = findViewById(R.id.div12);
        funDiv = findViewById(R.id.fun_div);
        bStartL = findViewById(R.id.startL);
        confFormat = findViewById(R.id.tvconfig);
        confSets = findViewById(R.id.tvsets);
        confGoals = findViewById(R.id.tvgoals);
        confBalance = findViewById(R.id.tvbalance);
        npSets = findViewById(R.id.npsets);
        npGoals = findViewById(R.id.npgoals);
        npBal = findViewById(R.id.npbal);
        bStartB = findViewById(R.id.startB);
        offLeague();
        offBon();
        state = 0; // select format

        bLeague.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                offFormat();
                onLeague();
                state = 1; // select league
                return true;
            }
            return false;
        });

        bBoN.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                offFormat();
                onBon();
                state = 2; // select BoN
                return true;
            }
            return false;
        });

        bStartL.setOnTouchListener((v, event) -> {
            int selectedRadioButtonId = leagueFormat.getCheckedRadioButtonId();
            if (selectedRadioButtonId != -1) {
                RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                if (selectedRadioButton == div12) {
                    intent.putExtra("games", 1);
                    intent.putExtra("sets", 1);
                    intent.putExtra("goals", 40); // 40
                    intent.putExtra("balance", 40); //40
                }
                if (selectedRadioButton == funDiv) {
                    intent.putExtra("games", 6); // 6
                    intent.putExtra("sets", 2);
                    intent.putExtra("goals", 5); // 5
                    intent.putExtra("balance", 5); // 5
                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                startActivity(intent);
                return true;
            }
            return false;
        });

        bStartB.setOnTouchListener((v, event) -> {
            intent.putExtra("games", 1);
            intent.putExtra("sets", npSets.getValue());
            int goals = npGoals.getValue();
            intent.putExtra("goals", goals);
            int balance = npBal.getValue() < goals? goals: npBal.getValue();
            intent.putExtra("balance", balance);

            if (event.getAction() == MotionEvent.ACTION_UP) {
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        switch (state) {
            case 0:
                finish();
            case 1:
                offLeague();
                onFormat();
                leagueFormat.clearCheck();
                state = 0;
            case 2:
                offBon();
                onFormat();
                state = 0;
        }
    }
}