package com.sdeport.logistics.driver.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sdeport.logistics.common.utils.MyToast;
import com.sdeport.logistics.common.utils.ViewHolder;
import com.sdeport.logistics.common.widgets.ProgressBar;
import com.sdeport.logistics.driver.BaseActivity;
import com.sdeport.logistics.driver.R;
import com.sdeport.logistics.driver.bean.Motorcade;
import com.sdeport.logistics.driver.bean.User;
import com.sdeport.logistics.driver.server.WebRequest;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class BindGroupActivity extends BaseActivity {

    private Unbinder unbinder;
    private LayoutInflater inflater;

    @BindView(R.id.motorcade_select)
    protected View motorcadeArea;
    @BindView(R.id.motorcade)
    protected EditText motorcade;
    @BindView(R.id.motorcade_clear)
    protected ImageView clear;
    @BindView(R.id.motorcade_info)
    protected View motorcadeInfo;
    @BindView(R.id.motorcade_info_name)
    protected TextView name;
    @BindView(R.id.motorcade_contact)
    protected TextView person;

    private PopupWindow selector;
    private ListView lister;
    private MotorAdapter adapter;
    private ArrayList<Motorcade> dataList;
    private Motorcade motorSelected;

    private ProgressBar progressBar;

    @Override
    protected void initUI(Bundle savedInstanceState) {
        addContentView(R.layout.activity_bindgroup);
        setTopBar(R.drawable.icon_back, R.string.title_bind_group, -1);

        unbinder = ButterKnife.bind(this);

        progressBar = new ProgressBar(this, clear);
        inflater = LayoutInflater.from(this);
        clear.setOnClickListener(this);

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                motorcade.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        emitter.onNext(s.toString());
                    }
                });
            }
        })
                .debounce(300, TimeUnit.MILLISECONDS, Schedulers.io())
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {

                        if (motorSelected != null &&
                                motorSelected.getName() != null &&
                                (motorSelected.getName().equals(s) ||
                                        TextUtils.isEmpty(s))) {//已选择的车队与输入框中车队名称一致，不查询，输入框中未输入关键字，不查询
                            return false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                clear.setBackgroundDrawable(progressBar);
                            }
                        });
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .switchMap(new Function<String, ObservableSource<JSONObject>>() {
                    @Override
                    public ObservableSource<JSONObject> apply(String s) throws Exception {
                        return WebRequest.getInstance().getMotorcadeList(s);
                    }
                })
                .flatMap(new Function<JSONObject, ObservableSource<ArrayList<Motorcade>>>() {
                    @Override
                    public ObservableSource<ArrayList<Motorcade>> apply(JSONObject object) throws Exception {
                        if (object == null) {
                            return Observable.error(new Throwable());
                        }
                        if (!object.getBooleanValue("success")) {
                            return Observable.error(new Throwable());
                        }
                        ArrayList<Motorcade> retArray = new ArrayList<>(JSON.parseArray(object.getString("data"), Motorcade.class));
                        return Observable.just(retArray);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("bindGroup", "doOnError: "+throwable);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<Motorcade>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ArrayList<Motorcade> motorcades) {
                        Log.e("bindGroup", "onNext: "+motorcades);
                        dataList = motorcades;
                        if (selector == null) {
                            initPopWindow();
                        }

                        selector.showAsDropDown(motorcadeArea, 0, 3);
                        adapter.setData(motorcades);
                        adapter.notifyDataSetChanged();

                        clear.setBackgroundResource(R.drawable.icon_close);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("bindGroup", "onError: " +e);
                        clear.setBackgroundResource(R.drawable.icon_close);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void initPopWindow() {
        if (selector == null) {
            selector = new PopupWindow(this);
            selector.setOutsideTouchable(false);
            selector.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_white_border_orange));
            selector.setWidth(motorcadeArea.getWidth());
            lister = (ListView) inflater.inflate(R.layout.listview_motorcade_select, null);
            adapter = new MotorAdapter();
            lister.setAdapter(adapter);
            lister.setOnItemClickListener(onItemClickListener);
            selector.setContentView(lister);
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            motorSelected = dataList.get(position);
            name.setText(motorSelected.getName());
            person.setText(motorSelected.getPerson());
            motorcade.setText(motorSelected.getName());

            if (motorcadeInfo.getVisibility() != View.VISIBLE) {
                motorcadeInfo.setVisibility(View.VISIBLE);
            }

            if (selector != null && selector.isShowing()) {
                selector.dismiss();
            }
        }
    };

    @OnClick(R.id.apply)
    protected void apply() {
        WebRequest.getInstance().applyAttach(User.getUser().getAccount(), motorSelected.getId(), new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                createDialog(false);
            }

            @Override
            public void onNext(JSONObject o) {
                if (o == null) {
                    onError(null);
                    return;
                }
                if (!o.getBooleanValue("success")) {
                    onError(new Throwable(o.getString("failReason")));
                    return;
                }

                dismissDialog();
                Intent resultIntent = new Intent(BindGroupActivity.this, BindResultActivity.class);
                resultIntent.putExtra("name", motorSelected.getName());
                startActivity(resultIntent);
                finish();
            }

            @Override
            public void onError(Throwable e) {
                dismissDialog();
                MyToast.show(BindGroupActivity.this, e == null || TextUtils.isEmpty(e.getMessage()) ?
                        getString(R.string.operation_failed) : e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void freeMe() {
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.base_top_left:
                finish();
                break;
            case R.id.motorcade_clear:
                motorcade.setText("");
                break;
        }
    }


    private class MotorAdapter extends BaseAdapter {

        private ArrayList<Motorcade> list;


        public void setData(ArrayList<Motorcade> motorcades) {
            this.list = motorcades;
        }

        @Override
        public int getCount() {
            return list == null ? 0: list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_motorcade_select, null);
            }
            ViewHolder.<TextView> get(convertView, R.id.name)
                    .setText(list.get(position).getName());
            return convertView;
        }
    }
}
