package com.miniai.miniai;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class JSONParser {

    public int getFaceCount(String jsonString){
        int faceCount = 0;
        try {
            // Convert the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);
            // Get the "face_state" array from the JSON object
            JSONArray faceStateArray = jsonObject.getJSONArray("face_state");
            // Loop through the "face_state" array
            for (int i = 0; i < faceStateArray.length(); i++) {
                JSONObject faceStateObject = faceStateArray.getJSONObject(i);
                if (faceStateObject.has("faceCount")) {
                    faceCount = faceStateObject.getInt("faceCount");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return faceCount;
    }
    private JSONObject getObjectFromArray(String jsonString, String arrayName, int faceID){
        try {
            // Convert the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);
            // Get the "face_state" array from the JSON object
            JSONArray jsonArray = jsonObject.getJSONArray(arrayName);
            // Loop through the "face_state" array
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject resultObject = jsonArray.getJSONObject(i);
                if (resultObject.has("FaceID") && resultObject.getInt("FaceID") == faceID) {
                    return resultObject;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public ArrayList<TextData> getFaceStateData(String jsonString, int faceID){
        try {
            JSONObject jsonObject = getObjectFromArray(jsonString, "face_state", faceID);
            if(jsonObject != null){
                return getTextDataList(jsonObject, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public String getKeyValueFromFaceStateData(String jsonString, int faceID, String keyName){
        try {
            JSONObject jsonObject = getObjectFromArray(jsonString, "face_state", faceID);
            if(jsonObject != null){
                return getKeyValue(jsonObject, keyName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public ArrayList<TextData> getFaceDetailData(String jsonString, int faceID){
        try {
            JSONObject jsonObject = getObjectFromArray(jsonString, "faces", faceID);
            if(jsonObject != null){
                return getTextDataList(jsonObject, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public String getKeyValueFromFaceDetailData(String jsonString, int faceID, String keyName){
        try {
            JSONObject jsonObject = getObjectFromArray(jsonString, "faces", faceID);
            if(jsonObject != null){
                return getKeyValue(jsonObject, keyName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public String getKeyValue(JSONObject jsonObject, String keyName) {
        String keyValue = null;
        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);

                if (value instanceof JSONObject) {
                    // Handle nested JSONObject
                    keyValue = getKeyValue((JSONObject) value, keyName);
                    if(keyValue != null){
                        return keyValue;
                    }
                } else if (value instanceof JSONArray) {
                    // Handle JSONArray
                    // Get the "face_state" array from the JSON object
                    JSONArray jsonArray = (JSONArray) value;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        keyValue = getKeyValue(jsonArray.getJSONObject(i), keyName);
                        if(keyValue != null){
                            return keyValue;
                        }
                    }
                } else {
                    if (key.equals(keyName)){
                        keyValue = value.toString();
                        return keyValue;
                    }
                }
            }
        } catch (JSONException e) {
            keyValue = null;
            e.printStackTrace();
        }
        return keyValue;
    }
    public ArrayList<TextData> getTextDataList(String jsonString, String objectName) {
        ArrayList<TextData> textData = new ArrayList<TextData>();
        try {
            // Convert the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject faceDataListObject = jsonObject.getJSONObject(objectName);
            if(faceDataListObject != null){
                return getTextDataList(faceDataListObject, null);
            }
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public ArrayList<TextData> getTextDataList(JSONObject jsonObject, String objectName) {
        ArrayList<TextData> textData = new ArrayList<TextData>();
        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);

                if (value instanceof JSONObject) {
                    // Handle nested JSONObject
                    if(key.equals("Images")){
                        ;
                    }
                    else{
                        ArrayList<TextData> nestedTextData  = getTextDataList((JSONObject) value, key);
                        if(nestedTextData  != null)
                            textData.addAll(nestedTextData);
                    }
                } else if (value instanceof JSONArray) {
                    // Handle JSONArray
                } else {
                    TextData item = null;
                    if(objectName != null)
                        item = new TextData(objectName, key, value.toString());
                    else
                        item = new TextData(key, value.toString());
                    if(item != null)
                        textData.add(item);
                }
            }
        }catch (JSONException e) {
            textData = null;
            e.printStackTrace();
        }
        return textData;
    }
    public ArrayList<ImageData> getImageDataList(JSONObject jsonObject) {
        ArrayList<ImageData> imageDataList = new ArrayList<ImageData>();
        try {
            JSONObject imageDataListObject = jsonObject.getJSONObject("Images");
            Iterator<String> keys = imageDataListObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject imageDataObject = imageDataListObject.getJSONObject(key);
                ImageData imageData = new ImageData(key);
                imageDataList.add(imageData);
                if (imageDataObject.has("Position")) {
                    JSONObject positionDataObject = imageDataObject.getJSONObject("Position");
                    imageData.setX1(positionDataObject.getInt("x1"));
                    imageData.setX2(positionDataObject.getInt("x2"));
                    imageData.setY1(positionDataObject.getInt("y1"));
                    imageData.setY2(positionDataObject.getInt("y2"));
                }
                if (imageDataObject.has("image")) {
                    imageData.setImage(imageDataObject.getString("image"));
                }
            }
        }catch (JSONException e) {
                imageDataList = new ArrayList<ImageData>();
            e.printStackTrace();
        }
        return imageDataList;
    }
}