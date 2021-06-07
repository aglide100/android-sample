package com.example.myapplication.Model;

import android.util.Log;

import com.example.myapplication.DB.DBmanager;
import com.example.myapplication.Presenter.Contract;

import java.util.ArrayList;

public class MainModel {
    private Post newPost;
    private Plan newPlan;
    private DBmanager dbManager;

    private ArrayList<Plan> planList;
    private ArrayList<Post> postList;

    public void makeNewPlan(Plan plan) {
        dbManager = new DBmanager();
        dbManager.addNewPlan(plan);

        Log.e("New", "Receive new Plan!"+ plan.getPlanName());
    }

    //   현재 작업 중인 plan 리스트를 가지고 온다.
    public ArrayList<Plan> getPlan() {
        dbManager = new DBmanager();
        ArrayList<Plan> planList = dbManager.getPlanList();
        if (planList == null){
            Log.e("MainModel", "리스트가 없습니다.!!!");
        }

        return planList;
    }

    public Post getPost(String ID) {
        dbManager = new DBmanager();

        return dbManager.getPost(ID);
    }

    public ArrayList<Post> getPost(Plan plan) {
        dbManager = new DBmanager();
        return dbManager.getPost(plan);
    }

    //    id값으로 post를 가져온다.
    public ArrayList<Post> getPostList(String parentID) {
        dbManager = new DBmanager();
        ArrayList<Post> postArrayList = new ArrayList<>();

        postArrayList = dbManager.getPost(dbManager.getPlan(parentID));
        return postArrayList;
    }


}
