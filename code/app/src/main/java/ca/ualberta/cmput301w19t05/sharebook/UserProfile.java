package ca.ualberta.cmput301w19t05.sharebook;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfile extends AppCompatActivity {


    private ImageView viewUserImage;
    private TextView viewUserName;
    private TextView viewUserEmail;
    private FirebaseHandler firebaseHandler;
    private User user;
    private String name;
    private String email;
    private Uri image;
    private String userID;
    private Boolean valid = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firebaseHandler = new FirebaseHandler(UserProfile.this);

        viewUserImage = (ImageView) findViewById(R.id.UserImage);
        viewUserName = (TextView) findViewById(R.id.UserName);
        viewUserEmail = (EditText) findViewById(R.id.UserEmail);
        Button submitButton = findViewById(R.id.submit);
        User user = firebaseHandler.getCurrentUser();
        image = user.getImage();
        userID = user.getUserID();
        name = user.getUsername();
        email = user.getEmail();

        if (user != null) {
            viewUserImage.setImageURI(image);
            viewUserName.setText(name);
            viewUserEmail.setText(email);
        }
        email = viewUserEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            viewUserEmail.setError("Need to fill");
            valid = false;
        }else if(!email.contains("@")){
            viewUserEmail.setError("wrong format");
            valid = false;
        }
        if (valid) {
            viewUserEmail.append(email);
            user = new User(userID, name, email, image);
            firebaseHandler.editUser(user);
            finish();
        }
    }
}

