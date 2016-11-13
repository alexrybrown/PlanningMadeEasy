package com.example.alex.planningmadeeasy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.alex.planningmadeeasy.utils.DBTools;
import com.example.alex.planningmadeeasy.utils.Goal;
import com.example.alex.planningmadeeasy.utils.User;

import java.util.ArrayList;

public class GoalAcitivity extends AppCompatActivity {
    public static final String SINGLE_GOAL = "com.example.alex.planningmadeeasy.SINGLE_GOAL";
    public static final String GOALS = "com.example.alex.planningmadeeasy.GOALS";
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        Intent intent = getIntent();
        this.username = intent.getStringExtra(LoginActivity.USER);
        LinearLayout ll = (LinearLayout) findViewById(R.id.goals);

        // Check to see if the user has primary goals to add to the screen
        DBTools dbTools = new DBTools(this);
        User user = dbTools.getUser(username);
        ArrayList<Goal> goals = dbTools.getPrimaryGoals(user);
        while (!goals.isEmpty()) {
            // goal is used in inner class so it has to be a final
            final Goal GOAL = goals.remove(0);
            final Button goalButton = new Button(this);
            goalButton.setText(GOAL.message);
            goalButton.setId((int) GOAL.goalKey);
            // Dynamically set the on click method
            goalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get the child goals to traverse
                    Intent intent = new Intent(GoalAcitivity.this, AddGoalActivity.class);
                    DBTools dbTools = new DBTools(GoalAcitivity.this);
                    ArrayList<Goal> goals = new ArrayList<Goal>();
                    goals = dbTools.getGoals(GOAL);
                    long[] goalKeys = new long[goals.size()];
                    for(int i = 0; i < goals.size(); ++i) {
                        goalKeys[i] = goals.get(i).goalKey;
                    }
                    intent.putExtra(SINGLE_GOAL, GOAL.goalKey);
                    intent.putExtra(GOALS, goalKeys);
                    startActivity(intent);
                }
            });
            goalButton.setLayoutParams(ll.getLayoutParams());
            ll.addView(goalButton);
        }
    }

    /** Called when user clicks add a goal **/
    public void addGoal(View view) {
        Intent intent = new Intent(this, AddGoalActivity.class);
        intent.putExtra(LoginActivity.USER, username);
        startActivity(intent);
    }
}
