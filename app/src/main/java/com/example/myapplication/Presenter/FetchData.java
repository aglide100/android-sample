package com.example.myapplication.Presenter;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.DB.DBmanager;
import com.example.myapplication.Model.AsyncResult;
import com.example.myapplication.Model.ErrorModel;
import com.example.myapplication.Model.Post;
import com.example.myapplication.MyApplication;
import com.example.myapplication.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class FetchData extends AsyncTask<ArrayList<String>, Integer, AsyncResult> {
    ConnectivityManager connectivityManager;

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mNotifyBuilder;
    private int mNotifyID = 10;

    private boolean ok = false;
    private int postTotalNum;
    String uri = "https://www.dongseo.ac.kr/kr/index.php?pCode=";

    private DBmanager dbManager;

    private void CheckState() {
        Context ctx = MyApplication.ApplicationContext();
        connectivityManager = (ConnectivityManager) ctx.getSystemService(CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
                new AlertDialog.Builder(ctx).setMessage("인터넷에 연결되어 있지 않습니다.").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            } else {
                ok = true;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        CheckState();
        CreateNotification();
        dbManager = new DBmanager();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
//        Log.e("While", "onProgressUpdate" + values[0] + values[1]);
        double unitPercent = (double)100 / (double)values[0];
        double pagePercent = unitPercent / (double)values[2];
        double pastPercent = unitPercent * (double)values[1];
        double nowPercent = pagePercent * (double)values[3];
        Log.e("pastPercent", pastPercent + "");
        Log.e("nowPercent", nowPercent + "");
        this.mNotifyBuilder.setProgress(100, (int)(pastPercent + nowPercent),false );
        this.mNotificationManager.notify(mNotifyID, mNotifyBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CreateNotification() {
        Context ctx = MyApplication.ApplicationContext();

        this.mNotifyBuilder = new NotificationCompat.Builder(ctx, "default");
        this.mNotifyBuilder.setContentTitle("데이터를 가져오고 있습니다!");
        this.mNotifyBuilder.setContentText("잠시만 기다려주세요.");
        this.mNotifyBuilder.setSmallIcon(R.mipmap.ic_launcher);
        this.mNotifyBuilder.setOngoing(true);
        this.mNotifyBuilder.setProgress(0,0,false);

        this.mNotificationManager = (NotificationManager) ctx.getSystemService(ctx.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.mNotificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        this.mNotificationManager.notify(mNotifyID, this.mNotifyBuilder.build());
    }

    private void removeNotification() {
        NotificationManagerCompat.from(MyApplication.ApplicationContext()).cancel(this.mNotifyID);
    }


    @SafeVarargs
    @Override
    protected final AsyncResult doInBackground(ArrayList<String>... arrayLists) {
        AsyncResult result = new AsyncResult();
        if (!ok) {
            return result;
        }

        ArrayList<Post> newPostList = new ArrayList<>();
        Document doc = null;
        int num;

        for (int i = 0; i < arrayLists[0].size(); i++) {
            num = 0;

            String code = arrayLists[0].get(i);
            Log.e("Start", code + " 시작");

            try {
                doc = Jsoup.connect(uri + code).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Get Total Page", code + "에서 문서 못가져옴");
                ErrorModel model = new ErrorModel(code, 0);
                result.addFailedItem(model);
                continue;
            }

            Elements pageInfo = doc.select("div[class=board-tot-wrap]");
            Element totalNum = pageInfo.select("span[class=tot-num]").select("span").first().children().select("span").first();
            String str = totalNum.text().replaceAll("[^0-9]", "");
            num = Integer.parseInt(str);
            Log.e("Result " + code, String.valueOf(num));

            Element pageNum = pageInfo.select("span[class=pg-num]").select("span").first().children().select("span").first();
            String pageStr = pageNum.text().replaceAll("[^0-9]", "");
            int finalPage = Integer.parseInt(pageStr);

            postTotalNum += num;
            // 기존의 데이터 비교 하는 코드

            // 없으면 처음부터
            Log.e("While", code + "중 페이지 수: " + finalPage);

            int nowpage = 1;
            do {
                publishProgress(arrayLists[0].size(), i, finalPage, nowpage);
                // 페이지 갯수 (1페이지에 15개의 글이 들어감)
                if (nowpage != 1) {
                    String getUri = uri + code + "&pg=" + nowpage;
                    try {
                        doc = Jsoup.connect(getUri).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Get Items", code + "의 " + nowpage + "페이지에서 문서 못가져옴");
                        ErrorModel model = new ErrorModel(code, nowpage);
                        result.addFailedItem(model);
                        continue;
                    }
                }

                Elements child = doc.select("tbody").select("tr[class=child_1], tr[class=child_2]");
                Log.e("While", nowpage + "페이지 안에 " + child.size() + "개 게시물");
                for (int l = 0; l < child.size(); l++) {
                    Element elements = child.get(l);
                    Post newPost = new Post();

                    Elements subjectItems = elements.select("td[class=f-tit subject]").select("p");
                    String title = subjectItems.select("span").text();
                    String postURL = subjectItems.select("a").attr("href");
                    String writer = elements.select("td[class=f-nm writer]").select("p").text();
                    String date = elements.select("td[class=f-date date]").select("p").text();
                    int postNum = Integer.parseInt(elements.select("td[class=f-num num]").select("p").text().replaceAll("[^0-9]", ""));
                    newPost.setParent(arrayLists[1].get(0));
                    newPost.setCode(code);
                    newPost.setTitle(title);
                    newPost.setDate(date);
                    newPost.setWriter(writer);
                    newPost.setUrl("https://www.dongseo.ac.kr/" + postURL);
                    newPost.setID();
                    newPost.setNum(postNum);
                    newPost.setUnRead();

                    newPostList.add(newPost);
                }

                nowpage++;
            } while (nowpage <= finalPage);

        }
        result.setSuccessItem(newPostList);

        return result;
    }

    @Override
    protected void onPostExecute(AsyncResult result) {
        super.onPostExecute(result);

        removeNotification();
        Log.e("End", "항목 끝!!! " + postTotalNum + "의 포스트를 찾았으며 " + result.getSuccessItem() + "갯수의 포스트 생성");

        dbManager.addPost(result.getSuccessItem());
    }
}