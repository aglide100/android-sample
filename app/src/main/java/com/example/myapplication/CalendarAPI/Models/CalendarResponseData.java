package com.example.myapplication.CalendarAPI.Models;

import com.google.gson.Gson;

public class CalendarResponseData {
    private CalendarActivityRequestCode activityRequestCode;
    private CalendarRequestCode requestCode;
    private CalendarTaskResponse response;
    private Object data;

    public CalendarRequestCode getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(CalendarRequestCode requestCode) {
        this.requestCode = requestCode;
    }

    public CalendarActivityRequestCode getActivityRequestCode() {
        return activityRequestCode;
    }

    public void setActivityRequestCode(CalendarActivityRequestCode activityRequestCode) {
        this.activityRequestCode = activityRequestCode;
    }

    public CalendarTaskResponse getResponse() {
        return response;
    }

    public void setResponse(CalendarTaskResponse response) {
        this.response = response;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
