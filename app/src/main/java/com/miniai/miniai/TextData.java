package com.miniai.miniai;

public class TextData { // Response Text data Class
    private String ObjectName;  // object name
    private String Key;         // key name
    private String Value;       // value name
    public TextData(){
        ObjectName = null;
        Key = null;
        Value = null;
    }
    public TextData(String objectName){
        ObjectName = objectName;
    }
    public TextData(String key, String value){
        Key = key;
        Value = value;
    }
    public TextData(String objectName, String key, String value){
        ObjectName = objectName;
        Key = key;
        Value = value;
    }
    public void setObjectName(String objectName){
        ObjectName = objectName;
    }
    public void setKey(String key){
        Key = key;
    }
    public void setValue(String value){
        Value = value;
    }
    public String getObjectName(){
        return ObjectName;
    }
    public String getKey(){
        return Key;
    }
    public String getValue(){
        return Value;
    }
    public String getKeyField(){
        String keyField = "";
        if(ObjectName != null) {
            keyField = ObjectName + "     ";
        }
        if(Key != null)
            keyField += Key;
        return keyField;
    }
}
