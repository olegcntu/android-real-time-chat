package com.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chatapp.attributes.ChatStatus;
import com.chatapp.model.Message;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout relativeLayoutMain;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatStatus.SIGN_IN_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(relativeLayoutMain, ChatStatus.AUTHORIZATION_SUCCESS, Snackbar.LENGTH_LONG).show();
                displayAllMessages();
            } else {
                Snackbar.make(relativeLayoutMain, ChatStatus.AUTHORIZATION_NO_SUCCESS, Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relativeLayoutMain = findViewById(R.id.activity_main);
        FloatingActionButton sendBtn = findViewById(R.id.btnSend);
        FloatingActionButton sendLogOut = findViewById(R.id.btn_log_out);

        sendBtn.setOnClickListener(view -> {
            EditText textField = findViewById(R.id.message);
            if (textField.getText().toString().equals(""))
                return;
            FirebaseDatabase.getInstance().getReference().push().setValue(
                    new Message(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(),
                            textField.getText().toString()));
            textField.setText("");
        });

        sendLogOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), ChatStatus.SIGN_IN_CODE);
        } else
            Snackbar.make(relativeLayoutMain, ChatStatus.AUTHORIZATION_SUCCESS + FirebaseAuth.getInstance().getCurrentUser(), Snackbar.LENGTH_LONG).show();
        displayAllMessages();
    }

    private void displayAllMessages() {

        ListView messages = findViewById(R.id.list);
        FirebaseListOptions.Builder<Message> builder = new FirebaseListOptions.Builder<>();

        builder.setLayout(R.layout.list_item)
                .setQuery(FirebaseDatabase.getInstance().getReference(), Message.class)
                .setLifecycleOwner(this);

        FirebaseListAdapter<Message> firebaseListAdapter = new FirebaseListAdapter<Message>(builder.build()) {
            @Override
            protected void populateView(@NonNull View v, @NonNull Message model, int position) {
                TextView messUser, messTime;
                BubbleTextView messText;
                messUser = v.findViewById(R.id.message_user);
                messTime = v.findViewById(R.id.message_time);
                messText = v.findViewById(R.id.message_text);

                messUser.setText(model.getUserName());
                messText.setText(model.getTextMessage());
                messTime.setText(DateFormat.format("HH:mm:ss", model.getMessageTime()));

            }
        };
        messages.setAdapter(firebaseListAdapter);

    }
}