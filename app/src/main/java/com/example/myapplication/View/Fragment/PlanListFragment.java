package com.example.myapplication.View.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.MainModel;
import com.example.myapplication.Model.Plan;
import com.example.myapplication.MyApplication;
import com.example.myapplication.Presenter.Contract;
import com.example.myapplication.Presenter.MainPresenter;
import com.example.myapplication.R;
import com.example.myapplication.View.Activity.ItemListActivity;
import com.example.myapplication.View.Activity.NewPlanActivity;
import com.example.myapplication.View.Basic.BasicFragment;

import java.util.ArrayList;

// 현재 가지고 있는 플랜 리스트를 보여주는 프래그먼트
// 플랜을 선택시 아이템 리스트 액티비티를 불러옴

//  Fragment를 상속하며 Contract.View를 구현하는 BasicFragment를 상속
public class PlanListFragment extends BasicFragment {

    Contract.Presenter presenter;
    MainModel mainModel;

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Plan> myDataset;

    Button moveToNewPlan;

    ConstraintLayout innerLayout;

    @Override
    public void onResume() {
        super.onResume();

        myDataset = new ArrayList<>();
        myDataset = mainModel.getPlan();

        if (myDataset.size() == 0) {
            innerLayout.setVisibility(View.VISIBLE);
        } else {
            innerLayout.setVisibility(View.GONE);
        }

        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_planlist, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new MainPresenter();
        mainModel = new MainModel();

        mRecyclerView = view.findViewById(R.id.recycler_view);
        moveToNewPlan = view.findViewById(R.id.moveToNewPlan);
        innerLayout = view.findViewById(R.id.planListInnerLayout);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(MyApplication.ApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        myDataset = new ArrayList<>();
        myDataset = mainModel.getPlan();

        if (myDataset.size() == 0) {
            innerLayout.setVisibility(View.VISIBLE);
        } else {
            innerLayout.setVisibility(View.GONE);
        }

        mAdapter = new MyAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        moveToNewPlan.setOnClickListener(v -> {
            Intent intent = new Intent(MyApplication.ApplicationContext(), NewPlanActivity.class);
            startActivity(intent);
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private final ArrayList<Plan> myDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView icon;
            public TextView titleText, commentText;

            public ViewHolder(View view) {
                super(view);
                icon = view.findViewById(R.id.card_view_icon);
                titleText = view.findViewById(R.id.card_view_title);
                commentText = view.findViewById(R.id.card_view_text);

                view.setOnClickListener(v -> {
                    Plan plan = new Plan();

                    int position = getAdapterPosition();
                    if (position != mRecyclerView.NO_POSITION) {
                        plan = myDataset.get(position);
                    }

                    Intent intent = new Intent(MyApplication.ApplicationContext(), ItemListActivity.class);
                    intent.putExtra("PlanID", plan.getPlanID());

                    startActivity(intent);
                });
            }
        }

        public MyAdapter(ArrayList<Plan> myDataset) {
            this.myDataset = myDataset;
        }

        @NonNull
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(MyApplication.ApplicationContext())
                    .inflate(R.layout.custom_card_view, parent , false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int postion) {
            holder.titleText.setText(myDataset.get(postion).getPlanName());
            String str = null;

            int count = myDataset.get(postion).getCountUnReadPost();
            if (count == 0) {
                str = "확인할 알림이 없습니다.";
            } else {
                str = count + "개의 알림이 있습니다.";
            }
            holder.commentText.setText(str);
//            holder.mImageView.setImageResource(mDataset.get(postion).img);
        }

        @Override
        public int getItemCount() {
            if (myDataset == null) {
                return 0;
            }

            return myDataset.size();
        }
    }

}


