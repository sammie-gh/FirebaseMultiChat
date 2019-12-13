package info.devexchanges.firebasechatapplication;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int SIGN_IN_REQUEST_CODE = 111;
    private FirebaseListAdapter<ChatMessage> adapter;
    private ListView listView;
    private String loggedInUserName = "";
    private String colorCode;

    public int getRandomColor() {
        // create object of Random class
        Random obj = new Random();
        int rand_num = obj.nextInt(0xffffff + 1);
// format it as hexadecimal string and print
        colorCode = String.format("#%06x", rand_num);

//        Log.d("color", "onCreate: " + colorCode);

        return rand_num;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getRandomColor();
        //find views by Ids
        FloatingActionButton fab = findViewById(R.id.fab);
        final EditText input = findViewById(R.id.input);
        listView = findViewById(R.id.list);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(), SIGN_IN_REQUEST_CODE);
        } else {
            // User is already signed in, show list of messages
            showAllOldMessages();
        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                view.setBackgroundColor(color);

                if (input.getText().toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter some texts!", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Forum")
                            .push()
                            .setValue(new ChatMessage(input.getText().toString(),
                                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(), colorCode)
                            );
                    input.setText("");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, "You have logged out!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in successful!", Toast.LENGTH_LONG).show();
                showAllOldMessages();
            } else {
                Toast.makeText(this, "Sign in failed, please try again later", Toast.LENGTH_LONG).show();

                // Close the app
                finish();
            }
        }
    }

    private void showAllOldMessages() {
        loggedInUserName = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("Main", "user id: " + loggedInUserName);

        Query query = FirebaseDatabase.getInstance().getReference().child("Forum");
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.item_in_message)
                .build();

        // Format the date before showing it
        //generating view
        // return the total number of view types. this value should never change
        // at runtime
        // return a value between 0 and (getViewTypeCount - 1)
        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);

                View avatar = v.findViewById(R.id.avatar);
                GradientDrawable drawable = (GradientDrawable) avatar.getBackground();
                drawable.setColor(Color.parseColor(model.getColor()));

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
//                Toast.makeText(MainActivity.this, "" + model.getMessageUser() + model.getMessageText(), Toast.LENGTH_SHORT).show();
                Log.d("messageUser", "populateView: " + model.getMessageText());
                Log.d("messageText ", "populateView: " + model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                ChatMessage chatMessage = getItem(position);
                if (chatMessage.getMessageUserId().equals(getLoggedInUserName()))
                    convertView = getLayoutInflater().inflate(R.layout.item_out_message, parent, false);
                else
                    convertView = getLayoutInflater().inflate(R.layout.item_in_message, parent, false);


                //generating view
                populateView(convertView, chatMessage, position);

                return super.getView(position, convertView, parent);
            }

            @Override
            public int getViewTypeCount() {
                // return the total number of view types. this value should never change
                // at runtime
                return 2;
            }

            @Override
            public int getItemViewType(int position) {
                // return a value between 0 and (getViewTypeCount - 1)
                return position % 2;
            }
        };

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
//    private void showAllOldMessageold() {
//        loggedInUserName = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        Log.d("Main", "user id: " + loggedInUserName);
//
//        adapter = new MessageAdapter(this, ChatMessage.class, R.layout.item_in_message,
//                FirebaseDatabase.getInstance().getReference());
//        listView.setAdapter(adapter);
//    }

    public String getLoggedInUserName() {
        return loggedInUserName;
    }
}
