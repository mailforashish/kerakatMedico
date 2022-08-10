package com.medico.app.activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.medico.app.R;
import com.medico.app.databinding.ActivityPrescriptionBinding;
import com.medico.app.response.Cartlist.CartResult;
import com.medico.app.utils.HideStatus;
import com.medico.app.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class PrescriptionActivity extends AppCompatActivity {
    ActivityPrescriptionBinding binding;
    HideStatus hideStatus;
    private static final int PICK_IMAGE_CAMERA_REQUEST_CODE = 0;
    private static final int PICK_IMAGE_GALLERY_REQUEST_CODE = 1;
    private String eventValue;
    private SessionManager sessionManager;
    private String amount;
    private List<CartResult> cartLists = new ArrayList<>();
    boolean isExpandable;
    String prescriptionImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_prescription);
        hideStatus = new HideStatus(getWindow(), true);
        sessionManager = new SessionManager(this);
        eventValue = getIntent().getStringExtra("Event");
        cartLists = sessionManager.getListFromLocal("cart");
        if (cartLists != null) {
            binding.tvCartItem.setText(cartLists.size() + " Item in cart require prescription");
            StringBuilder builder = new StringBuilder();
            for (CartResult details : cartLists) {
                builder.append(details.getProductId().getDrugName() + "\n");
            }
            binding.tvDrugName.setText(builder.toString());
        }
        amount = getIntent().getStringExtra("pay_amount");
        if (eventValue.equals("Account")) {
            binding.clPrList.setVisibility(View.VISIBLE);
        }

        binding.setClickListener(new EventHandler(this));
        binding.tvPrice.setText("₹ " + "199");
        binding.tvPrice.setPaintFlags(binding.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }


    public class EventHandler {
        Context mContext;

        public EventHandler(Context mContext) {
            this.mContext = mContext;
        }

        public void backPage() {
            onBackPressed();
            finish();
        }

        public void camera() {
            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, PICK_IMAGE_CAMERA_REQUEST_CODE);
        }

        public void gallery() {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_GALLERY_REQUEST_CODE);
        }

        public void radioPrescription() {
            binding.rbPr.setSelected(true);
            binding.rbNoPr.setChecked(false);
        }

        public void radioNoPrescription() {
            binding.rbNoPr.setSelected(true);
            binding.rbPr.setChecked(false);

        }

        public void withPrescription() {
            if (prescriptionImage != null) {
                startActivity(new Intent(PrescriptionActivity.this, PaymentActivity.class)
                        .putExtra("pay_amount", amount)
                        .putExtra("UploadImage", prescriptionImage));
                finish();
            } else {
                Toast.makeText(mContext, "Please upload Prescription", Toast.LENGTH_SHORT).show();
            }

        }

        public void viewMore() {
            if (isExpandable) {
                binding.tvViewMore.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_child_up, 0);
                isExpandable = false;
                binding.tvDrugName.setMaxLines(cartLists.size());
                binding.tvViewMore.setText("View Less");
            } else {
                binding.tvViewMore.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_child_down, 0);
                binding.tvDrugName.setMaxLines(3);
                binding.tvViewMore.setText("View More");
                isExpandable = true;
            }

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null || requestCode == PICK_IMAGE_CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
                switch (requestCode) {
                    case 0:
                        if (resultCode == RESULT_OK) {
                            Bitmap photo = (Bitmap) data.getExtras().get("data");
                            Uri selectedCamera = getCameraUri(getApplicationContext(), photo);
                            final InputStream imageStream = getContentResolver().openInputStream(selectedCamera);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            String encodedImage = cameraImageBase64(selectedImage);
                            prescriptionImage = encodedImage;
                            //Log.e("selectedImage", "CameraImageFile:" + finalCameraImage);
                            //Log.e("selectedImage", "CameraImage:" + encodedImage);
                        }
                        break;
                    case 1:
                        if (resultCode == RESULT_OK) {
                            final Uri imageUri = data.getData();
                            Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                            ByteArrayOutputStream stream=new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                            byte[] bytes=stream.toByteArray();
                            String encodedImage= galleryImageBase64(Base64.encodeToString(bytes,Base64.DEFAULT));
                            prescriptionImage = encodedImage;
                            //Log.e("selectedImage", "GalleryImageFile:" + imageUri);
                            //Log.e("selectedImage", "GalleryImage:" + encodedImage);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            Log.e("pictureError", e.toString());
        }

    }
    public Uri getCameraUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Camera", null);
        return Uri.parse(path);
    }
    private String cameraImageBase64(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encImage;
    }
    public String galleryImageBase64(String base64image){
        byte [] encodeByte=Base64.decode(base64image.getBytes(),Base64.DEFAULT);
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPurgeable = true;
        Bitmap image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length,options);
        if(image.getHeight() <= 400 && image.getWidth() <= 400){
            return base64image;
        }
        image = Bitmap.createScaledBitmap(image, 300, 200, false);
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        System.gc();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }


}