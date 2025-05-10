package com.miniai.miniai;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ImageData { // Response Image data Class
    private String Type;    // Image type
    private Bitmap image;   // Image
    private int Position_X1;    // x1 position of Image
    private int Position_Y1;    // y1 position of Image
    private int Position_X2;    // x2 position of Image
    private int Position_Y2;    // y2 position of Image
    public ImageData(){
        Type = null;
        image = null;
        Position_X1 = Position_Y1 = Position_X2 = Position_Y2 = 0;
    }
    public ImageData(String type){
        Type = type;
        Position_X1 = Position_Y1 = Position_X2 = Position_Y2 = 0;
    }
    public ImageData(String type, Bitmap img){
        Type = type;
        image = img;
        Position_X1 = Position_Y1 = Position_X2 = Position_Y2 = 0;
    }
    public ImageData(String type, String imageBase64){
        Type = type;
        image = getImage(imageBase64);
        Position_X1 = Position_Y1 = Position_X2 = Position_Y2 = 0;
    }
    public void setType(String type){
        Type = type;
    }
    public void setImage(Bitmap img){
        image = img;
    }
    public void setImage(String imageBase64){
        image = getImage(imageBase64);
    }
    public String getType(){
        return Type;
    }
    public Bitmap getImage(){
        return image;
    }
    public void setX1(int value){
        Position_X1 = value;
    }
    public void setY1(int value){
        Position_Y1 = value;
    }
    public void setX2(int value){
        Position_X2 = value;
    }
    public void setY2(int value){
        Position_Y2 = value;
    }
    public int getX1(){
        return Position_X1;
    }
    public int getY1(){
        return Position_Y1;
    }
    public int getX2(){
        return Position_X2;
    }
    public int getY2(){
        return Position_Y2;
    }
    // Method to get the Bitmap image from a Base64 string
    private Bitmap getImage(String imageBase64) {
        try {
            if (imageBase64 != null) {
                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                return bitmap;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return null;
    }
}

