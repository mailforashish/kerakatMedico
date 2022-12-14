package com.medico.app.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;


import com.google.android.material.tabs.TabLayout;
import com.medico.app.R;
import com.medico.app.adapter.OrderItemAdapter;
import com.medico.app.databinding.ActivityOrderDetailBinding;
import com.medico.app.dialog.OrderCancelDialog;
import com.medico.app.response.Addcart.AddCartResponse;
import com.medico.app.response.OrderResponse.DrugList;
import com.medico.app.response.OrderResponse.OrderDataList;
import com.medico.app.retrofit.ApiManager;
import com.medico.app.retrofit.ApiResponseInterface;
import com.medico.app.utils.Constant;
import com.medico.app.utils.HideStatus;
import com.medico.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class OrderDetailActivity extends AppCompatActivity implements ApiResponseInterface {
    ActivityOrderDetailBinding binding;
    private String totalItems;
    private OrderDataList orderDataList;
    List<DrugList> orderItem = new ArrayList<>();
    SessionManager sessionManager;
    OrderItemAdapter orderItemAdapter;
    private String valid_reason = null;
    ApiManager apiManager;
    HideStatus hideStatus;
    private String typeName = "";
    private int trackStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_detail);
        hideStatus = new HideStatus(getWindow(), true);
        binding.setClickListener(new EventHandler(this));
        sessionManager = new SessionManager(this);
        apiManager = new ApiManager(this, this);

        orderDataList = (OrderDataList) getIntent().getSerializableExtra("LIST");
        List<DrugList> objectItemList = orderDataList.getDrugs().get(0).getDrug();
        totalItems = String.valueOf(objectItemList.size());
        if (orderDataList != null) {
            binding.tvPatientsNameInput.setText(orderDataList.getAddress().get(0).getName());
            binding.tvOrderNoInput.setText(String.valueOf("GOMEDICOS" + orderDataList.getId()));
            if (orderDataList.getAddress().get(0).getType() == 1) {
                typeName = "Home";
            } else if (orderDataList.getAddress().get(0).getType() == 2) {
                typeName = "Office";
            } else if (orderDataList.getAddress().get(0).getType() == 3) {
                typeName = "Others";
            }
            binding.tvAddressView.setText(Html.fromHtml("<b>" + typeName + "</b>" + "<br />" +
                    "<b>" + orderDataList.getAddress().get(0).getName() + "</b>" + "<br />" +
                    "<small>" + orderDataList.getAddress().get(0).getAddress1() + "</small>" + "<br />" +
                    "<small>" + orderDataList.getAddress().get(0).getPincode() + "</small>" + "<br />" +
                    "<small>" + orderDataList.getAddress().get(0).getMobile() + "</small>"));
        }

        binding.tvOrderItemCount.setText(totalItems + " Items");
        binding.tvOrderAmount.setText(String.valueOf(orderDataList.getCutAmount()));
        // binding.tvDeliverDate.setText(String.valueOf("Delivery Date " + orderDataList.getDeliveredDate()));
        binding.rvOrderItem.setLayoutManager(new LinearLayoutManager(OrderDetailActivity.this, LinearLayoutManager.VERTICAL, false));
        orderItemAdapter = new OrderItemAdapter(OrderDetailActivity.this, objectItemList);
        binding.rvOrderItem.setAdapter(orderItemAdapter);
        binding.tabOrder.addTab(binding.tabOrder.newTab().setText("Order Summary"));
        binding.tabOrder.addTab(binding.tabOrder.newTab().setText("Items"));
        binding.tabOrder.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.view.getTab().getText().equals("Order Summary")) {
                    binding.rootOrder.setVisibility(View.VISIBLE);
                    binding.rootItems.setVisibility(View.GONE);
                } else {
                    binding.rootItems.setVisibility(View.VISIBLE);
                    binding.rootOrder.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //show status according to api status data..
        trackStatus = objectItemList.get(0).getStatus();
        List<String> track = new ArrayList<>();
        track.add("Order \nPlace");
        track.add("Order \nProcessing");
        track.add("Out for \nDelivery");
        track.add("Order \nDelivered");
        binding.trackView.setSteps(track);
        binding.trackView.setTrackStatus(trackStatus);
        binding.trackView.done(true);
        registerReceiver(receiver, new IntentFilter("REASON_CHANGED_ACTION"));
    }


    public class EventHandler {
        Context mContext;

        public EventHandler(Context mContext) {
            this.mContext = mContext;
        }

        public void backOrderDetail() {
            onBackPressed();
            startActivity(new Intent(mContext, MainActivity.class));
            finish();
        }

        public void OrderCancel() {
            if (binding.tvCancel.getText().toString().equals("Cancel")) {
                new OrderCancelDialog(mContext, binding.tvOrderNoInput.getText().toString());
            } else {
                String product_id = "";
                String quantity = "";
                boolean is_reOderVerify = false;
                /*for (int i = 0; i < orderItem.size(); i++) {
                    product_id = orderItem.get(i).getProductId();
                    quantity = orderItem.get(i).getQuantity();
                    apiManager.addCart(product_id, quantity);
                    is_reOderVerify = true;
                }
                if (is_reOderVerify) {
                    startActivity(new Intent(mContext, CartActivity.class));
                }*/
                //print(orderItem, 0);

            }
        }

        public void needHelp() {
            startActivity(new Intent(mContext, NeedHelpActivity.class));
            finish();
        }


    }

  /*  void print(List<OrderItem> a, int index) {
        //using a recursive method:
        if ((a != null) && (index < a.size())) {
            Log.e("REORDER", "getProductId" + a.get(index).getProductId());
            Log.e("REORDER", "getQuantity" + a.get(index).getQuantity());
            print(a, ++index);
        }
    }*/

    @Override
    public void isError(String errorCode) {

    }

    @Override
    public void isSuccess(Object response, int ServiceCode) {
        if (ServiceCode == Constant.ADD_CART) {
            AddCartResponse rsp = (AddCartResponse) response;
            if (rsp != null) {
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                valid_reason = intent.getStringExtra("reason");
                if (valid_reason != null && !valid_reason.isEmpty() && !valid_reason.equals("null")) {
                    binding.clStatus.setVisibility(View.VISIBLE);
                    binding.viewStatus.setVisibility(View.VISIBLE);
                    binding.tvReason.setText(valid_reason);
                    binding.tvCancel.setText("Re Order");
                    binding.tvCancel.setTextColor(Color.WHITE);
                    binding.tvCancel.setBackgroundResource(R.drawable.button_re_order);
                } else {
                    binding.clStatus.setVisibility(View.GONE);
                    binding.viewStatus.setVisibility(View.GONE);

                }

            }
        }
    };
}