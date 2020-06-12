package com.example.appproyecto;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.appproyecto.api.RestService;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.textfield.TextInputLayout;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PrincipalActivity extends AppCompatActivity {

    EditText mResultEt, mResultEtFact, mResultEtVal;
    TextInputLayout mResultEtLy;
    ImageView mPreviewIv, mPreviewIvFact;
    Button mButtonVal;
    RequestQueue requestQueue;

    private  static final int CAMERA_REQUEST_CODE = 200;
    private  static final int STORAGE_REQUEST_CODE = 400;
    private  static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private  static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle("Capturar imagen/archivo -->");

        mResultEt = findViewById(R.id.resultEt);
        mPreviewIv = findViewById(R.id.imageIv);

        mResultEtLy = findViewById(R.id.resultEtLy);
        mResultEtLy.setCounterEnabled(true);
        mResultEtLy.setCounterMaxLength(11);

        mResultEtFact = findViewById(R.id.resultEtFact);
        mPreviewIvFact = findViewById(R.id.imageIvFact);

        mButtonVal = findViewById(R.id.buttonVal);
        mResultEtVal = findViewById(R.id.resultEtVal);

        //camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        mResultEt.addTextChangedListener(validationButton);
        mResultEtFact.addTextChangedListener(validationButton);

        mButtonVal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //documentValidation ("https://personalwebdb.000webhostapp.com/validacion.php?nro_ruc="+mResultEt.getText()+"&nro_doc="+mResultEtFact.getText()+"");
                RestService.getInstance(PrincipalActivity.this).documentValidation(mResultEt.getText().toString(), mResultEtFact.getText().toString(), new RestService.ResponseListener() {
                    @Override
                    public void onResponse(String response) {
                        mResultEtVal.setText(response);
                    }

                    @Override
                    public void onError(String error) {
                        mResultEtVal.setText("");
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    //actionbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //handle actionbar item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addImageScanner) {
            showImageImportDialog();
        }
        if (id == R.id.addImageTrash) {
            cleanField();
        }
        if (id == R.id.addConfiguracion) {
            Toast.makeText(this, "Configuración", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    // Association with modal display option

    private void showImageImportDialog() {
        //items to display in dialog
        String[] items = {"Cámara", "Galeria"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // set tittle
        dialog.setTitle("Obtener Imagen desde:");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //camera option clicked
                    if (!checkCameraPermission()) {
                        //camera permission not allowed, request it
                        requestCameraPermission();
                    }
                    else {
                        //permission allowed, take picture
                        pickCamera();
                    }
                }
                if (which == 1) {
                    //gallery option clicked
                    if (!checkStoragePermission()) {
                        //storage permission not allowed, request it
                        requestStoragePermission();
                    }
                    else {
                        //permission allowed, take picture
                        pickGallery();
                    }
                }

            }
        });
        dialog.create().show(); //show dialog
    }


    // Association with clean Icon

    private void cleanField() {
        mResultEt.setText("");
        mPreviewIv.setImageResource(0);

        mResultEtFact.setText("");
        mPreviewIvFact.setImageResource(0);

        mResultEtVal.setText("");

        mButtonVal.setEnabled(false);
    }

    // Association with button validation

    private TextWatcher validationButton = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String mResultEtInput = mResultEt.getText().toString().trim();
            String mResultEtFactInput = mResultEtFact.getText().toString().trim();

            mButtonVal.setEnabled(!mResultEtInput.isEmpty() && !mResultEtFactInput.isEmpty());
            //    mButtonVal.setTextColor(Color.parseColor("#ffffff"));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    // Association with Validation Result
    private void documentValidation(String URL){
        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        mResultEtVal.setText(jsonObject.getString("estado"));
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mResultEtVal.setText("");
                Toast.makeText(getApplicationContext(), "NO SE ENCONTRÓ DOCUMENTO", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    // Association find image gallery
    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    // Association find Camera
    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NuevaImagen");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Imagen a Texto");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions( this, cameraPermission, CAMERA_REQUEST_CODE );
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //handle permission result

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this,"Permiso Denegado", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this,"Permiso  Denegado", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    //handle image result

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
        if (requestCode == IMAGE_PICK_CAMERA_CODE) {
            CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
        }

        // get cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                if (mPreviewIv.getDrawable() == null) {

                    Uri resultUri = result.getUri();
                    mPreviewIv.setImageURI(resultUri);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIv.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                    if (!recognizer.isOperational()) {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    } else {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = recognizer.detect(frame);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock myItem = items.valueAt(i);
                            sb.append(myItem.getValue());
                            sb.append("\n");
                        }
                        mResultEt.setText(sb.toString());
                    }
                } else if (mPreviewIv.getDrawable() != null) {

                    Uri resultUri = result.getUri();
                    mPreviewIvFact.setImageURI(resultUri);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIvFact.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                    if (!recognizer.isOperational()) {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    } else {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = recognizer.detect(frame);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock myItem = items.valueAt(i);
                            sb.append(myItem.getValue());
                            sb.append("\n");
                        }
                        mResultEtFact.setText(sb.toString());
                    }
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
