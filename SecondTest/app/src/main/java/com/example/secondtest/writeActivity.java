package com.example.secondtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class writeActivity extends AppCompatActivity implements View.OnClickListener{
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    String userName = "";
    String userTheme = "";
    String userDate = "";
    String userData = "";
    String userPicturePath = "0";
    private Uri imageUri;
    private ImageView picture;
    private String key;

    //创建数据库
    private MyDatabaseHelper_user dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write);
        //从sharepreference中获取作者信息
        SharedPreferences editor_1 = this.getSharedPreferences("user", MODE_PRIVATE);
        userName = editor_1.getString("name",null);

        dbHelper = new MyDatabaseHelper_user(this, "note.db", null, 1);

        EditText name = findViewById(R.id.xingming);
        Button button = findViewById(R.id.butfinish);
        button.setOnClickListener(this);
        Button button_1 = findViewById(R.id.butCarmera);
        button_1.setOnClickListener(this);
        Button button_2 = findViewById(R.id.butPictures);
        button_2.setOnClickListener(this);
        picture = findViewById(R.id.rijitupian);


        //获取上个界面传送来的date信息
        Intent intent = getIntent();
        key = intent.getStringExtra("id");
        if(key!=null){
            //查看信息
            look(key);
        }
        name.setText(userName);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.butfinish:
                //判断是否输入了日期
                EditText date2 = findViewById(R.id.riqi);
                userDate = date2.getText().toString();
                if(userDate.equals("")){
                    Toast.makeText(this, "请输入日期", Toast.LENGTH_SHORT).show();
                }
                else{write();}
                break;


            case R.id.butCarmera:
                //判断日期是否为空
                EditText date = findViewById(R.id.riqi);
                userDate = date.getText().toString();
                if(userDate.equals("")){
                    Toast.makeText(this, "请输入日期", Toast.LENGTH_SHORT).show();
                }
                else{
                    //启动相机
                    camera(key);
                }
                break;
            case R.id.butPictures:
                //判断是否授予读写权限
                if(ContextCompat.checkSelfPermission(writeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(writeActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
                EditText date1 = findViewById(R.id.riqi);
                userDate = date1.getText().toString();
                if(userDate.equals("")){
                    Toast.makeText(this, "请输入日期", Toast.LENGTH_SHORT).show();
                }
                else{
                    //启动相册
                    openAlbum();
                }
                break;
        }
    }

    public void delete(View view) {
        delete();
        Intent intent = new Intent(writeActivity.this,listActivity.class);
        startActivity(intent);
    }

    //打开相册，筛选照片
    public void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);//打开相册
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int [] grantResults){
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }

    }


    //
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        //解析document类型的uri，并将真实的地址送到imagepath中
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            //进一步判断是否是media格式
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);

            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }
        else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);
        }
        else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        userPicturePath = imagePath;
        displayImage(imagePath);
    }

    //显示图片
    private void displayImage(String imagePath) {
        //安卓10规定，只能访问当前应用目录下的文件，所以在清单文件重要加入android:requestLegacyExternalStorage="true"，用于访问全部内存
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }

    //获取图片的真是路径
    private String getImagePath(Uri uri, String selection ) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public void camera(String date){
        //在当前文件的目录下新建文件，用于存储照片
        File outputImage = new File(getExternalCacheDir(),date+".jpg");
        try{
            if(outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //如果SDK版本大于24，对照片地址进行封装
        if(Build.VERSION.SDK_INT >= 24){
            imageUri = FileProvider.getUriForFile(writeActivity.this,"com.bistu.secondtest.fileprovider",outputImage);
        }else{
            imageUri = Uri.fromFile(outputImage);
        }
        //打开相机
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.addFlags(intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, TAKE_PHOTO);

        userPicturePath = outputImage.getPath();
    }


    //对action的结果进行分析
    protected  void onActivityResult(int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //将拍摄的照片解析成bitmap，显示出来
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //如果SDK版本大于19，则调用方法解析封装过的uri
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(data);
                    }
                }
                break;
            default:
                break;
        }

    }

    //往数据库写入数据
    public void write(){

        EditText name = findViewById(R.id.xingming);
        EditText theme = findViewById(R.id.biaoti);
        EditText date = findViewById(R.id.riqi);
        EditText data = findViewById(R.id.rijineirong);

        userTheme = theme.getText().toString();
        userData = data.getText().toString();
        userDate = date.getText().toString();
        userName = name.getText().toString();


        if(userDate.equals("")){
            Toast.makeText(this, "请输入日期", Toast.LENGTH_SHORT).show();
        }

        else{
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String s = "select * from note where id ='"+key+"'";
            Cursor cursor = db.rawQuery(s,null);
            cursor.moveToFirst();

            ContentValues values = new ContentValues();
            values.put("name", userName);
            values.put("data", userData);
            values.put("date", userDate);
            values.put("theme", userTheme);
            values.put("picture",userPicturePath);

            //判断是更新还是新增
            if(cursor.getCount() != 0){
                db.update("note", values, "id = ?", new String[]{key});
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
            }
            else{
                db.insert("note", null, values);
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
            }

            values.clear();
            Intent intent = new Intent(writeActivity.this,listActivity.class);
            startActivity(intent);
        }
    }
    public void delete() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("note", "id = " + key, null);
        finish();
    }




    public void look(String str){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        EditText theme = findViewById(R.id.biaoti);
        EditText date = findViewById(R.id.riqi);
        EditText data = findViewById(R.id.rijineirong);
        ImageView image = findViewById(R.id.rijitupian);
        String path = "";

        String s = "select * from note where id ='"+str+"'";
        Cursor cursor = db.rawQuery(s,null);
        cursor.moveToFirst();
        userTheme = cursor.getString(cursor.getColumnIndex("theme"));
        userData = cursor.getString(cursor.getColumnIndex("data"));
        userDate = cursor.getString(cursor.getColumnIndex("date"));
        path = cursor.getString(cursor.getColumnIndex("picture"));
        image.setImageBitmap(BitmapFactory.decodeFile(path));
        theme.setText(userTheme);
        data.setText(userData);
        date.setText(userDate);
        cursor.close();
    }

}
