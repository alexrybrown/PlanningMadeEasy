package com.example.alex.planningmadeeasy;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.example.alex.planningmadeeasy.utils.DBTools;
import com.example.alex.planningmadeeasy.utils.User;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    /** Called when user clicks submit button **/
    public void register(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        // Get all of the views of our fields
        EditText mFirstNameView = (EditText) findViewById(R.id.first_name);
        EditText mLastNameView = (EditText) findViewById(R.id.last_name);
        EditText mEmailView = (EditText) findViewById(R.id.email);
        EditText mUsernameView = (EditText) findViewById(R.id.username);
        EditText mPasswordView = (EditText) findViewById(R.id.password);
        EditText mReenterPasswordView = (EditText) findViewById(R.id.reenter_password);
        // Get the String values to create the user
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String reenterPassword = mReenterPasswordView.getText().toString();
        // Check to make sure everything is valid
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError(getString(R.string.empty_first_name));
            focusView = mFirstNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.empty_last_name));
            focusView = mLastNameView;
            cancel = true;
        } else if (!this.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.empty_username));
            focusView = mUsernameView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.empty_password));
            focusView = mPasswordView;
            cancel = true;
        } else if (TextUtils.isEmpty(reenterPassword)) {
            mReenterPasswordView.setError(getString(R.string.empty_reenter_password));
            focusView = mReenterPasswordView;
            cancel = true;
        } else if(!password.equals(reenterPassword)) {
            mPasswordView.setError(getString(R.string.invalid_password));
            mReenterPasswordView.setError(getString(R.string.invalid_password));
            focusView = mPasswordView;
            focusView = mReenterPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Create user and store in db
            User newUser = new User(0, firstName, lastName, email, username, password);
            DBTools dbTools = null;

            try {
                dbTools = new DBTools(this);
                newUser = dbTools.createUser(newUser);
            } catch (SQLiteConstraintException e) {
                mUsernameView.setError(getString(R.string.non_unique_username));
                focusView = mUsernameView;
                cancel = true;
            } finally {
                if (dbTools != null) {
                    dbTools.close();
                }
            }
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            // Pass user on to next activity
            startActivity(intent);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
}
