package ca.ualberta.cmput301f20t08.libraryTracker.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.cmput301f20t08.libraryTracker.R;
import ca.ualberta.cmput301f20t08.libraryTracker.models.User;
import ca.ualberta.cmput301f20t08.libraryTracker.tools.FirebaseHandler;

/**
 * A userProfile screen that offers Username, userEmail, userImage, and can be edited
 */
public class UserProfile extends AppCompatActivity {


    private ImageView viewUserImage;
    private EditText viewUserName;
    private EditText viewUserEmail;
    private FirebaseUser user;
    ProgressDialog progressDialog;
    private DatabaseReference reference;
    private FirebaseHandler firebaseHandler;
    private Toolbar editProfileToolBar;
    private String TAG = "EditProfile";
    private ImageButton editOrSave;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final TextView textView = (TextView) v;
            View view = View.inflate(UserProfile.this, R.layout.content_edit, null);
            final EditText userInput = view.findViewById(R.id.user_input);
            userInput.setText(textView.getText());
            userInput.requestFocus();
            userInput.selectAll();

            new AlertDialog.Builder(UserProfile.this)
                    .setTitle("edit " + textView.getTag()).setView(view)
                    .setPositiveButton("submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (textView.getTag().equals("username")) {
                                progressDialog.setMessage("updating your username...");
                                showDialog();

                                Query query = reference
                                        .orderByChild("username")
                                        .equalTo(userInput.getText().toString());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() == null) {
                                            updateUsername(userInput.getText().toString());
                                        } else {
                                            hideDialog();
                                            Toast.makeText(UserProfile.this, "username exists", Toast.LENGTH_SHORT).show();
                                            progressDialog.setMessage(null);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(UserProfile.this, databaseError.toString(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            } else if (textView.getTag().equals("email")) {
                                progressDialog.setMessage("updating your email...");
                                showDialog();
                                Query query = reference
                                        .orderByChild("email")
                                        .equalTo(userInput.getText().toString());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue() == null) {
                                            updateEmail(userInput.getText().toString());
                                        } else {
                                            hideDialog();
                                            Toast.makeText(UserProfile.this, "update email failed", Toast.LENGTH_SHORT).show();
                                            progressDialog.setMessage(null);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(UserProfile.this, databaseError.toString(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                            }


                        }
                    }).setNegativeButton("cancel", null).show();
        }
    };

    private void updateEmail(final String toString) {
        user.updateEmail(toString)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideDialog();
                        Toast.makeText(UserProfile.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideDialog();
                        Log.d(TAG, "onComplete: update email to " + toString);
                        reference.child(user.getUid()).child("email").setValue(toString);
                        firebaseHandler.updateUserToBooks(toString, "email");
                        viewUserEmail.setText(toString);
                    }
                });


    }

    private void updateUsername(final String username) {

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        user.updateProfile(profileChangeRequest)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfile.this, e.toString(), Toast.LENGTH_LONG).show();

                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideDialog();
                        Log.d(TAG, "onComplete: update username to " + username);
                        reference.child(user.getUid()).child("username").setValue(username);
                        firebaseHandler.updateUserToBooks(username, "username");
                        viewUserName.setText(username);
                    }
                });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.db_username_email_tuple));
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        firebaseHandler = new FirebaseHandler(UserProfile.this);


        viewUserImage = findViewById(R.id.UserImage);
        viewUserName = findViewById(R.id.UserName);
        viewUserEmail = findViewById(R.id.UserEmail);
        editProfileToolBar = findViewById(R.id.edit_profile_toolbar);
        editOrSave = findViewById(R.id.edit_or_save);
        Intent intent = getIntent();
        final User owner = intent.getParcelableExtra("owner");
        if (owner != null) {

            viewUserName.setText(owner.getUsername());
            viewUserEmail.setText(owner.getEmail());
            final TextView userScore = findViewById(R.id.UserRate);
//            userScore.setVisibility(View.VISIBLE);
            firebaseHandler.getMyRef().child(getString(R.string.db_username_email_tuple))
                    .child(owner.getUserID()).child("rates").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Long> temp = (ArrayList<Long>) dataSnapshot.getValue();
                    if (temp != null && !temp.isEmpty()) {
                        owner.setRates((ArrayList<Long>) temp);
                        userScore.setText(String.format("rate: %.2f/10 (%d)", owner.average(), owner.getRates().size()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } else {
            editOrSave.setVisibility(View.VISIBLE);
            editOrSave.setImageResource(R.drawable.ic_create_black_18dp);
            viewUserName.setText(user.getDisplayName());
            viewUserEmail.setText(user.getEmail());
            setSupportActionBar(editProfileToolBar);


//            viewUserEmail.setOnClickListener(onClickListener);
//            viewUserName.setOnClickListener(onClickListener);
        }


    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public void toggleEditState(View view) {


        boolean CurrentImageState = !editOrSave.isSelected();
        Log.d(TAG, "toggleEditState: " + CurrentImageState);
        viewUserName.setEnabled(CurrentImageState);
        viewUserEmail.setEnabled(CurrentImageState);
        editOrSave.setSelected(CurrentImageState);
        if (CurrentImageState) {
            editOrSave.setImageResource(R.drawable.ic_save_24px);
        } else {
            editOrSave.setImageResource(R.drawable.ic_create_black_18dp);
            progressDialog.setMessage("updating your profile...");
            showDialog();

            reference
                    .orderByChild("username")
                    .equalTo(viewUserName.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        updateUsername(viewUserName.getText().toString());
                        reference
                                .orderByChild("email")
                                .equalTo(viewUserEmail.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) {
                                    updateEmail(viewUserEmail.getText().toString());
                                } else {
                                    hideDialog();
                                    Toast.makeText(UserProfile.this, "update email failed", Toast.LENGTH_SHORT).show();
                                    progressDialog.setMessage(null);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(UserProfile.this, databaseError.toString(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    } else {
                        hideDialog();
                        Toast.makeText(UserProfile.this, "username exists", Toast.LENGTH_SHORT).show();
                        progressDialog.setMessage(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UserProfile.this, databaseError.toString(), Toast.LENGTH_SHORT).show();

                }
            });


        }

    }
}
