package ca.ualberta.cmput301w19t05.sharebook.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import ca.ualberta.cmput301w19t05.sharebook.R;
import ca.ualberta.cmput301w19t05.sharebook.models.Book;
import ca.ualberta.cmput301w19t05.sharebook.tools.ISBNAdapter;
import ca.ualberta.cmput301w19t05.sharebook.tools.FirebaseHandler;

/**
 * A addBook screen Allow user adding books into their sheff
 */
public class AddBookActivity extends AppCompatActivity {

    private String titleText;
    private String authorText;
    private String ISBNText;
    private String descriptionText;

    private EditText editTitle;
    private EditText editAuthor;
    private EditText editISBN;
    private EditText editDescription;

    private FirebaseHandler firebaseHandler;
    private int IMAGE_REQUEST_CODE = 1;
    private int flag = 0;
    private Uri Uri ;
    private Bitmap Uploaded;
    private Book book;
    private boolean inProgress = false;
    private ProgressDialog progressDialog;
    //private int RESIZE_REQUEST_CODE =2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book_screen);

        firebaseHandler = new FirebaseHandler(AddBookActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("adding book...");

        editTitle = findViewById(R.id.title);
        editAuthor = findViewById(R.id.author);
        editISBN = findViewById(R.id.ISBN);
        editDescription = findViewById(R.id.description);
        Button uploadButton = findViewById(R.id.PhotoUpload);
        Button scanButton = findViewById(R.id.scan);
        Button submitButton = findViewById(R.id.submit);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddBookActivity.this, ScanActivity.class);
                startActivityForResult(intent, ScanActivity.SCAN_BOOK);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean valid = true;

                //test case for empty blank
                titleText = editTitle.getText().toString();
                if (TextUtils.isEmpty(titleText)) {
                    editTitle.setError("Please enter the Title");
                    valid = false;
                }

                authorText = editAuthor.getText().toString();
                if (TextUtils.isEmpty(authorText)) {
                    editAuthor.setError("Please enter the Author");
                    valid = false;
                }

                ISBNText = editISBN.getText().toString();
                if (TextUtils.isEmpty(ISBNText)) {
                    editISBN.setError("Please enter the ISBN");
                    valid = false;
                }

                descriptionText = editDescription.getText().toString();

                //check ok
                if (valid) {
                    book = new Book(titleText, authorText, ISBNText, firebaseHandler.getCurrentUser());
                    if (flag == 1){
                        showDialog();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Uploaded.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();
                        String filenames = "image/" + firebaseHandler.getCurrentUser().getUserID() + "/" + book.getBookId().hashCode() + ".png";
                        final StorageReference ref = firebaseHandler.getStorageRef().child(filenames);
                        UploadTask uploadTask = ref.putBytes(data);


                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                hideDialog();
                                // Handle unsuccessful uploads

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                getUriFrom(firebaseHandler.getStorageRef().child("image/" + firebaseHandler.getCurrentUser().getUserID() + "/" + book.getBookId().hashCode() + ".png"));
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                            }
                        });


                    }
                    else {
                        getUriFrom(firebaseHandler.getStorageRef().child("image/book_placeholder.png"));

                    }

                }

            }

        });

    }

    private void getUriFrom(StorageReference reference) {
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                book.setPhoto(String.valueOf(uri));
                //System.out.println(flag);

                if (!descriptionText.equals("")) {
                    book.setDescription(descriptionText);
                }
                firebaseHandler.addBook(book);
                //firebaseHandler.generateImageFromText(book.getTitle());
                hideDialog();
                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideDialog();
                        Toast.makeText(AddBookActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    ImageView photoUploaded = findViewById(R.id.photoUploaded);
                    Uri uri = ShowResizedImage(data);
                    Log.e("uri", uri.toString());
                    ContentResolver cr = this.getContentResolver();
                    try {
                        // get bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(cr
                                .openInputStream(uri));
                        photoUploaded.setImageBitmap(bitmap);
                        Uri = uri;
                        flag = 1;

                        Uploaded = bitmap;

                    } catch (Exception e) {
                        Log.e("Exception", e.getMessage(), e);
                    }
                }
            }
        }
        else if (requestCode == ScanActivity.SCAN_BOOK) {
            if (resultCode == RESULT_OK) {
                String ISBN = data.getStringExtra("ISBN");
                editISBN.setText(ISBN);
                new ISBNAdapter(ISBN, editTitle, editAuthor, editDescription).execute(ISBN);
            }
        }
    }

    public Uri ShowResizedImage(Intent data){

        Uri uri = data.getData();
        return uri;
    }
    private void showDialog() {
        inProgress = true;
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
    private void hideDialog() {
        inProgress = false;
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}