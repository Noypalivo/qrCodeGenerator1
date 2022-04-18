package com.example.cloudqrcodeservise;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.service.carrier.CarrierMessagingService;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLOutput;

public class MainActivity extends AppCompatActivity {


    private FloatingActionButton fabHideFab, photo,audio,video,text,file,DeleteUploadedFile,UploadedFile,ShareButton;
    private Animation animHideFab, animShowFab;
    Boolean VisibleButtonsLVL1 = true, ConvertToPdf = false;


    Button convert_to_pdf;
    TextView notification;
    EditText etInput;
    ImageView ivOutput;
    Uri pdfUri;
    Bitmap bitmap;
    String pdfFiletoConvert, FileType = "*/*";
    File myPDFFileToPdf;
    PdfDocument pdfDocument;



    FirebaseStorage storage;
    FirebaseDatabase database;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        convert_to_pdf = findViewById(R.id.convert_to_pdf);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PackageManager.PERMISSION_GRANTED);
        convert_to_pdf.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(convert_to_pdf,"x", 250);
                ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(convert_to_pdf,"y", 0);
                // Animation animation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.zoomout);
           //  convert_to_pdf.startAnimation(animation);
                objectAnimator.setDuration(2000);
                objectAnimator2.setDuration(2000);
                objectAnimator.start();
                objectAnimator2.start();
           //     pdfConvert();
            }
        });


        animHideFab = AnimationUtils.loadAnimation(this, R.anim.fab_hide);
        animShowFab = AnimationUtils.loadAnimation(this, R.anim.fab_show);

        audio = findViewById(R.id.audio);
        video = findViewById(R.id.video);
        text = findViewById(R.id.text);
        photo = findViewById(R.id.photo);
        file = findViewById(R.id.file);
        UploadedFile = findViewById(R.id.UploadFile);
        ShareButton = findViewById(R.id.ShareButton);
        DeleteUploadedFile = findViewById(R.id.DeleteUploadedFile);
        fabHideFab = (FloatingActionButton) findViewById(R.id.fabHideFab);
        audio = (FloatingActionButton) findViewById(R.id.audio);
        video = (FloatingActionButton) findViewById(R.id.video);
        text = (FloatingActionButton) findViewById(R.id.text);
        photo = (FloatingActionButton) findViewById(R.id.photo);
        file = (FloatingActionButton) findViewById(R.id.file);

        etInput = findViewById(R.id.et_input);
        ivOutput = findViewById(R.id.iv_output);



        notification = findViewById(R.id.notification);
        hideButton();
        fabHideFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideButton();
                hideChooseButton();
                etInput.setVisibility(View.INVISIBLE);
                }
        });


        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileType = "video/*";
                selectContent(FileType);
            }
        });
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileType = "image/*";
                selectContent(FileType);
            }
        });
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileType = "audio/*";
                selectContent(FileType);
            }
        });
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileType = "*/*";
                selectContent(FileType);
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileType="text";
                selectText();
            }
        });

        UploadedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (FileType == "text"){
                    generateText();
                    hideChooseButton();
                    VisibleButtonsLVL1 = true;
                    hideButton();
                    ShareButton.setVisibility(View.VISIBLE);
                }
                else if(pdfUri != null){
                    uploadFile(pdfUri);
                }
                else {
                    hideChooseButton();
                    VisibleButtonsLVL1 = true;
                    hideButton();
                    Toast.makeText(MainActivity.this, "Выберите файл", Toast.LENGTH_SHORT).show();
                }
            }
        });

        DeleteUploadedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfUri=null;
                notification.setText("Файл пока не выбран");
                hideChooseButton();
                VisibleButtonsLVL1 = true;
                hideButton();
                etInput.setVisibility(View.INVISIBLE);
            }
        });

        ShareButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                pdfConvert();
                if (ConvertToPdf) {
                    Toast.makeText(MainActivity.this,"Файл конвертирован в PDF", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(pdfFiletoConvert)); // НЕ ОТПРАВЛЯЕТ ФАЙЛ
                    // intent.putExtra(Intent.EXTRA_SUBJECT, myPDFFileToPdf);
                    startActivity(Intent.createChooser(intent, "Способ отправки:"));
                }
                else {
                    Toast.makeText(MainActivity.this,"Не удалось создать PDF файл", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    public void generateText(){
        String sText = etInput.getText().toString().trim();

        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            BitMatrix matrix = writer.encode(sText, BarcodeFormat.QR_CODE,500,500);

            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);
            ivOutput.setImageBitmap(bitmap);

            InputMethodManager manager = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);

            manager.hideSoftInputFromWindow(etInput.getApplicationWindowToken(),0);

        } catch (WriterException e){
            e.printStackTrace();
        }
    }

    public void hideChooseButton(){
        UploadedFile.setVisibility(View.INVISIBLE);
        DeleteUploadedFile.setVisibility(View.INVISIBLE);
    }
    public void visibleChooseButton(){
        UploadedFile.setVisibility(View.VISIBLE);
        DeleteUploadedFile.setVisibility(View.VISIBLE);
    }

    public void hideButton(){

        if (VisibleButtonsLVL1) {
          //  audio.startAnimation(animHideFab);
            video.startAnimation(animHideFab);
            photo.startAnimation(animHideFab);
            file.startAnimation(animHideFab);
            text.startAnimation(animHideFab);
            VisibleButtonsLVL1 = false;
        } else {
            audio.startAnimation(animShowFab);
            video.startAnimation(animShowFab);
            photo.startAnimation(animShowFab);
            file.startAnimation(animShowFab);
            text.startAnimation(animShowFab);
            VisibleButtonsLVL1 = true;
        }
    }







    private void uploadFile(Uri pdfUri) {

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Загрузка файла...");
        progressDialog.setProgress(0);
        progressDialog.show();


        final String fileName = System.currentTimeMillis()+"";
        StorageReference storageReference = storage.getReference();


        storageReference.child("Uploads").child(fileName).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                        DatabaseReference reference = database.getReference();

                        progressDialog.dismiss();


                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String urlToDownload = uri.toString();
                                System.out.println("____________________________________________________________________");
                                System.out.println(result.toString());
                                System.out.println(urlToDownload);
                                System.out.println("____________________________________________________________________");
                                System.out.println("____________________________________________________________________");
                                hideChooseButton();
                                ShareButton.setVisibility(View.VISIBLE);

                                String sText = urlToDownload;
                                MultiFormatWriter writer = new MultiFormatWriter();
                                try {
                                    BitMatrix matrix = writer.encode(sText, BarcodeFormat.QR_CODE, 350, 350);

                                    BarcodeEncoder encoder = new BarcodeEncoder();
                                    bitmap = encoder.createBitmap(matrix);
                                    ivOutput.setImageBitmap(bitmap);
                                    InputMethodManager manager = (InputMethodManager) getSystemService(
                                            Context.INPUT_METHOD_SERVICE);

                                    manager.hideSoftInputFromWindow(etInput.getApplicationWindowToken(), 0);

                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }
                           

                            }
                        });




                        reference.child(fileName).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){
                                    Toast.makeText(MainActivity.this,"Файл успешно загружен!",Toast.LENGTH_SHORT).show();

                                }
                                else {
                                    Toast.makeText(MainActivity.this,"Не удалось загрузить файл.",Toast.LENGTH_SHORT).show();
                                }



                            }

                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this,"Не удалось загрузить файл.",Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                int currentProgress= (int) (100*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        } //else Toast.makeText(MainActivity.this,"Нет необходимых разрешений", Toast.LENGTH_SHORT).show();

    }




    private void selectContent(String FileType) {
        Intent intent = new Intent();
        intent.setType(FileType);                  // pdf ?
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,86);     // 86 ???
        hideButton();
        visibleChooseButton();

    }

    private void selectText() {
        //появляется текстовое поле и кнопка сохранить.
        etInput.setVisibility(View.VISIBLE);
        hideButton();
        visibleChooseButton();


    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


         if (requestCode == 86 && resultCode == RESULT_OK && data != null){
            pdfUri = data.getData();
           notification.setText("Выбран файл: "+ data.getData().getLastPathSegment());
       }else {
           Toast.makeText(MainActivity.this,"Выберите файл",Toast.LENGTH_SHORT).show();
             hideChooseButton();
             VisibleButtonsLVL1 = false;
             hideButton();
       }
        super.onActivityResult(requestCode, resultCode, data);
   }


   @RequiresApi(api = Build.VERSION_CODES.KITKAT)
   public void pdfConvert(){
       System.out.println("PDF конвертор запущен");
       pdfDocument = new PdfDocument();
       PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(500,500,1).create();
       PdfDocument.Page page = pdfDocument.startPage(myPageInfo);

       page.getCanvas().drawBitmap(bitmap,0,0,null);
       pdfDocument.finishPage(page);

       pdfFiletoConvert = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/myPDFFile.pdf";
       myPDFFileToPdf = new File(pdfFiletoConvert);
       System.out.println("PDF создан");
       System.out.println(pdfFiletoConvert);

       try {
           pdfDocument.writeTo(new FileOutputStream(myPDFFileToPdf));

           System.out.println("myPDFFile"+myPDFFileToPdf.toString());
           System.out.println("myPageInfo"+myPageInfo.toString());
           System.out.println("PDF записан");
           ConvertToPdf = true;
       } catch (IOException e){
           System.out.println("Ошибка");
           e.printStackTrace();
           ConvertToPdf = false;
       }
        pdfDocument.close();

   }
















}