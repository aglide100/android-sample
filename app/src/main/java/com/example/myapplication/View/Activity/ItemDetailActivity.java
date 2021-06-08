package com.example.myapplication.View.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.CalendarAPI.CalendarAPI;
import com.example.myapplication.CalendarAPI.Exceptions.CalendarCantNotUseException;
import com.example.myapplication.CalendarAPI.Exceptions.CalendarNeedUpdateGoogleServiceException;
import com.example.myapplication.CalendarAPI.Exceptions.CalendarNetworkException;
import com.example.myapplication.CalendarAPI.Exceptions.CalendarNotYetFinishBringDataException;
import com.example.myapplication.CalendarAPI.Interfaces.CalenderResultInterface;
import com.example.myapplication.CalendarAPI.Models.CalendarActivityRequestCode;
import com.example.myapplication.CalendarAPI.Models.CalendarInputEvent;
import com.example.myapplication.CalendarAPI.Models.CalendarResponseData;
import com.example.myapplication.DB.DBmanager;
import com.example.myapplication.Model.MainModel;
import com.example.myapplication.Model.Plan;
import com.example.myapplication.Model.Post;
import com.example.myapplication.MyApplication;
import com.example.myapplication.Presenter.Contract;
import com.example.myapplication.Presenter.MainPresenter;
import com.example.myapplication.R;
import com.example.myapplication.View.Basic.BasicActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Date;

public class ItemDetailActivity extends BasicActivity {
    Contract.Presenter presenter;
    MainModel mainModel;
    DBmanager dbManager;
    private Post post;
    private TextView titleView, dateView;
    private WebView contentView;


    private ConstraintLayout progressLayout, postInnerLayout;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemdetail);

        // UI Setting
        titleView = findViewById(R.id.postTitleView);
        dateView = findViewById(R.id.postDateView);
        progressLayout = findViewById(R.id.gettingData);
        postInnerLayout = findViewById(R.id.postDetailInnerLayout);
        contentView = findViewById(R.id.postContentView);

        Intent postIntent = getIntent();
        String postID = postIntent.getStringExtra("postID");

        Toolbar toolbar = findViewById(R.id.toolbar_custom);

        setSupportActionBar(toolbar);

        presenter = new MainPresenter();
        mainModel = new MainModel();
        dbManager = new DBmanager();

        post = mainModel.getPost(postID);

        getSupportActionBar().setTitle(post.getTitle());

        Plan plan = dbManager.getPlan(post.getParent());
        dbManager.decreaseUnReadPost(plan);
        contentView.getSettings().setLoadWithOverviewMode(true);
        contentView.getSettings().setUseWideViewPort(false);
        contentView.getSettings().setAllowContentAccess(true);
        contentView.setWebChromeClient(new WebChromeClient());
        contentView.getSettings().setLoadWithOverviewMode(true);

        if (post.isCustom()) {
            progressLayout.setVisibility(View.INVISIBLE);
            postInnerLayout.setVisibility(View.VISIBLE);

            titleView.setText(post.getTitle());
            dateView.setText(post.getDate());
            contentView.loadData(post.getContent(),"text/html", "UTF-8");
        } else {
            new getPostAsyncTask().execute(post);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.open_url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            Toast.makeText(MyApplication.ApplicationContext(), "Hello", Toast.LENGTH_SHORT).show();
        }

        if (id == R.id.add_calender) {
            try {
                long hour1 = 3600 * 1000;
                CalendarAPI.getInstance().addEvent(this, new CalenderResultInterface() {
                    @Override
                    public void getResult(CalendarResponseData responseData) {
                        Toast.makeText(MyApplication.ApplicationContext(), responseData.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void failedWithActivityResult(CalendarActivityRequestCode reason) {
                        Toast.makeText(MyApplication.ApplicationContext(), "error : "+reason.getCode(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void permissionRevoked() {
                        Toast.makeText(MyApplication.ApplicationContext(), "권한 없음", Toast.LENGTH_SHORT).show();
                    }
                }, new CalendarInputEvent("제목", "집", "설명", new Date(), new Date(new Date().getTime() + hour1)), new CalendarInputEvent("제목2", "집", "설명", new Date(new Date().getTime() + (hour1 * 2)), new Date(new Date().getTime() + (hour1 * 3))));
            } catch (CalendarNeedUpdateGoogleServiceException e) {
                e.printStackTrace();
            } catch (CalendarCantNotUseException e) {
                e.printStackTrace();
            } catch (CalendarNetworkException e) {
                e.printStackTrace();
            } catch (CalendarNotYetFinishBringDataException e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (CalendarAPI.getInstance().isRequestCode(requestCode)) {
            CalendarAPI.getInstance().progressRequest(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    private class getPostAsyncTask extends AsyncTask<Post, Void, Post> {
        private ConnectivityManager connectivityManager;

        private boolean ok = false;
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CheckState();
            dbManager = new DBmanager();

            postInnerLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            titleView.setText(post.getTitle());
            dateView.setText(post.getDate());
        }

        @Override
        protected Post doInBackground(Post... asyncPost) {
            Post updatePost = asyncPost[0];
            String url = updatePost.getUrl();
            Document doc = null;

            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            Element content = doc.select("div[class=board-view-contents]").first();
            Log.e("Detail", content.text());
            updatePost.setContent(content.text());

            return updatePost;
        }

        @Override
        protected void onPostExecute(Post asyncPost) {
            progressLayout.setVisibility(View.GONE);
            postInnerLayout.setVisibility(View.VISIBLE);

            if (asyncPost == null || asyncPost.getID() == null) {

            } else {
                Log.e("Detail", asyncPost.getContent());
                dbManager.updateContentInPost(asyncPost);
                contentView.loadData(asyncPost.getContent(),"text/html", "UTF-8");
            }
        }
    }

}
