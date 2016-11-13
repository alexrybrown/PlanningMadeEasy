package com.example.alex.planningmadeeasy;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.alex.planningmadeeasy.utils.DBTools;
import com.example.alex.planningmadeeasy.utils.Goal;
import com.example.alex.planningmadeeasy.utils.User;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AddGoalActivity extends AppCompatActivity {
    private String username;
    private Intent originalIntent;
    private boolean baseLevel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        originalIntent = getIntent();
        username = originalIntent.getStringExtra(LoginActivity.USER);

        if (originalIntent.getLongArrayExtra(GoalAcitivity.GOALS) != null || originalIntent.getLongExtra(GoalAcitivity.SINGLE_GOAL, 0) != 0) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final DBTools dbTools = new DBTools(this);
            final User user = dbTools.getUser(username);
            LinearLayout ll = (LinearLayout) findViewById(R.id.enter_goal);
            if(originalIntent.getLongExtra(GoalAcitivity.SINGLE_GOAL, 0) != 0) {
                Button parentGoal = new Button(this);
                parentGoal.setText(dbTools.getGoal(originalIntent.getLongExtra(GoalAcitivity.SINGLE_GOAL, 0), dbTools.getUser(username)).message);
                parentGoal.setLayoutParams(lp);
                // Do the traversal backwards NEEDS TO BE FINISHED
                parentGoal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Goal father = dbTools.getParentGoal(originalIntent.getLongExtra(GoalAcitivity.SINGLE_GOAL, 0), user);
                        long key;
                        Goal current = dbTools.getGoal(originalIntent.getLongExtra(GoalAcitivity.SINGLE_GOAL, 0), user);
                        if (father.goalKey != 0 && current.parentKey != 0) {
                            key = father.goalKey;
                        } else {
                            father = null;
                            key = 0;
                        }
                        Intent intent;
                        ArrayList<Goal> goals = new ArrayList<Goal>();
                        long[] goalKeys;
                        if (father != null) {
                            intent = new Intent(AddGoalActivity.this, AddGoalActivity.class);
                            intent.putExtra(GoalAcitivity.SINGLE_GOAL, key);
                            goals = dbTools.getGoals(father);
                            goalKeys = new long[goals.size()];
                            for (int i = 0; i < goals.size(); ++i) {
                                goalKeys[i] = goals.get(i).goalKey;
                            }
                            intent.putExtra(GoalAcitivity.GOALS, goalKeys);
                            intent.putExtra(LoginActivity.USER, username);
                        } else {
                            intent = new Intent(AddGoalActivity.this, GoalAcitivity.class);
                            intent.putExtra(LoginActivity.USER, username);
                        }

                        startActivity(intent);
                    }
                });
                ll.addView(parentGoal);
            }

            if(originalIntent.getLongArrayExtra(GoalAcitivity.GOALS) != null) {
                Button childGoal;
                long[] longGoals = originalIntent.getLongArrayExtra(GoalAcitivity.GOALS);
                ArrayList<Goal> goals = new ArrayList<Goal>();
                for(int i = 0; i < longGoals.length; ++i) {
                    goals.add(dbTools.getGoal(longGoals[i], user));
                }
                while (goals != null && !goals.isEmpty()) {
                    final Goal goal = goals.remove(0);
                    childGoal = new Button(this);
                    childGoal.setText(goal.message);
                    childGoal.setLayoutParams(lp);
                    childGoal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ArrayList<Goal> childGoals = dbTools.getGoals(goal);
                            Intent finalIntent = new Intent(AddGoalActivity.this, AddGoalActivity.class);
                            finalIntent.putExtra(GoalAcitivity.GOALS, childGoals);
                            finalIntent.putExtra(GoalAcitivity.SINGLE_GOAL, goal.goalKey);
                            finalIntent.putExtra(LoginActivity.USER, username);
                            startActivity(finalIntent);
                        }
                    });
                    ll.addView(childGoal);
                }
            }

        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            EditText parentGoal = new EditText(this);
            final DBTools dbTools = new DBTools(this);
            parentGoal.setHint("Enter Sub Goal");
            parentGoal.setLayoutParams(lp);
            LinearLayout ll = (LinearLayout) findViewById(R.id.enter_new_goals);
            ll.addView(parentGoal);
            baseLevel = true;
        }
    }

    /** Called when user clicks add sub goal button **/
    public void addSubGoal(View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        EditText subGoal = new EditText(this);
        subGoal.setHint("Enter Sub Goal");
        subGoal.setLayoutParams(lp);

        LinearLayout ll = (LinearLayout) findViewById(R.id.activity_add_goal);
        ll = (LinearLayout) ((LinearLayout) ((LinearLayout) ((ScrollView) ll.getChildAt(0)).getChildAt(0)).getChildAt(0)).getChildAt(1);
        ll.addView(subGoal);
    }

    /** Called when user clicks remove sub goal button **/
    public void removeSubGoal(View view) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.activity_add_goal);
        ll = (LinearLayout) ((LinearLayout) ((LinearLayout) ((ScrollView) ll.getChildAt(0)).getChildAt(0)).getChildAt(0)).getChildAt(1);

        // We can remove a sub goal if we still have at least one
        if (ll.getChildCount() > 1) {
            ll.removeView(ll.getChildAt(ll.getChildCount() - 1));
        }
    }

    /** Called when user clicks submit goals button **/
    public void submitGoals(View view) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.activity_add_goal);
        ll = (LinearLayout) ((LinearLayout) ((LinearLayout) ((ScrollView) ll.getChildAt(0)).getChildAt(0)).getChildAt(0)).getChildAt(1);

        // Verify the data that was entered correctly
        boolean cancel = false;
        View focusView = null;

        for(int i = 0; i < ll.getChildCount(); ++i) {
            EditText mGoalView = (EditText) ll.getChildAt(i);
            if (TextUtils.isEmpty(mGoalView.getText())) {
                mGoalView.setError(getString(R.string.goal_submission_error));
                focusView = mGoalView;
                cancel = true;
            }
        }

        if(cancel) {
            focusView.requestFocus();
            return;
        }

        // Get the user attached to these goals
        DBTools dbTools = new DBTools(this);
        User user = dbTools.getUser(username);
        EditText mGoalView;
        Goal parentGoal;
        if (baseLevel) {
            // Create a primary goal
            mGoalView = (EditText) ll.getChildAt(0);
            parentGoal = new Goal(0, mGoalView.getText().toString(), 0, user);
            parentGoal = dbTools.insertGoal(parentGoal);

            // Create sub goals
            Goal goal;
            for (int i = 1; i < ll.getChildCount(); ++i) {
                mGoalView = (EditText) ll.getChildAt(i);
                goal = new Goal(0, mGoalView.getText().toString(), parentGoal.goalKey, user);
                dbTools.insertGoal(goal);
            }
        } else {
            parentGoal = dbTools.getParentGoal(originalIntent.getLongExtra(GoalAcitivity.SINGLE_GOAL, 0), user);

            // Create sub goals
            Goal goal;
            for (int i = 0; i < ll.getChildCount(); ++i) {
                mGoalView = (EditText) ll.getChildAt(i);
                goal = new Goal(0, mGoalView.getText().toString(), parentGoal.goalKey, user);
                dbTools.insertGoal(goal);
            }
        }

        ArrayList<Goal> goals = new ArrayList<Goal>();
        goals = dbTools.getGoals(parentGoal);
        long[] goalKeys = new long[goals.size()];
        for(int i = 0; i < goals.size(); ++i) {
            goalKeys[i] = goals.get(i).goalKey;
        }

        Intent intent = new Intent(this, AddGoalActivity.class);
        intent.putExtra(LoginActivity.USER, username);
        intent.putExtra(GoalAcitivity.SINGLE_GOAL, parentGoal.goalKey);
        intent.putExtra(GoalAcitivity.GOALS, goalKeys);
        startActivity(intent);
    }
}
