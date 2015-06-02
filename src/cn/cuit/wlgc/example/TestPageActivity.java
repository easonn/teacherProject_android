package cn.cuit.wlgc.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class TestPageActivity extends Activity implements OnClickListener {

    // private final String URL = getString(R.string.URL_UPLOAD);
    private final String URL = "http://192.168.1.2:8080/example-server/score/shangchuan";

    @SuppressWarnings("serial")
    private ArrayList<String> option = new ArrayList<String>() {
        {
            add("A");
            add("B");
            add("C");
            add("D");
        }
    };
    @SuppressLint("UseSparseArrays")
    private Map<Integer, Integer> score = new HashMap<Integer, Integer>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);

        // super.setContentView(layout);
        layout.setOrientation(LinearLayout.VERTICAL);
        String testJson = getIntent().getExtras().getString("info");
        final String pageId = getIntent().getExtras().getString("pageId");
        try {
            JSONObject jsonObject = new JSONObject(testJson);
            final String msg = jsonObject.getString("msg");
            final String testName = jsonObject.getString("testName");
            final JSONArray jsonArray = jsonObject.getJSONArray("testInfo");
            SharedPreferences sp = this.getSharedPreferences("stuInfo",
                    Context.MODE_PRIVATE);
            String pageIdCheck = sp.getString(pageId, "none");
            Button button = new Button(this);
            if ("none".equals(pageIdCheck)) {
                button.setText("完成");
                button.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (score.size() < Integer.parseInt(msg)) {
                            Toast.makeText(TestPageActivity.this, "请做完选择",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        float number = 0;
                        for (Map.Entry<Integer, Integer> entry : score
                                .entrySet()) {
                            if (entry.getValue() == 1) {
                                number++;
                            }
                        }
                        try {
                            String teacherId = jsonArray.getJSONObject(0)
                                    .getString("teacherId");
                            float finalScore = (number * 100f) / score.size();
                            // Toast.makeText(TestPageActivity.this,
                            // "分数" + finalScore, Toast.LENGTH_SHORT).show();
                            upload(finalScore, pageId, teacherId, testName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                });
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                TextView show = new TextView(this);
                JSONObject test = jsonArray.getJSONObject(i);
                show.setText((i + 1) + "." + test.getString("testInfo"));
                RadioGroup radioGroup = new RadioGroup(this);
                int optionSize = option.size() - 1;
                for (int t = 0; optionSize != -1;) {
                    int j = (int) (Math.random() * optionSize);

                    String o = option.get(j);
                    option.remove(j);
                    optionSize--;
                    RadioButton rbutton = new RadioButton(this);
                    // rbutton.setGravity(Gravity.RIGHT);
                    rbutton.setText(test.getString("option" + o));
                    // button.setTextColor(R.color.text_color);
                    if (o.equals("A")) {
                        rbutton.setId((int) Math.pow(10, i + 1) + 6);
                    } else {
                        rbutton.setId((int) Math.pow(10, i + 1) + (t + 2));
                    }
                    t++;
                    rbutton.setOnClickListener(this);
                    radioGroup.addView(rbutton);
                }
                option.add("A");
                option.add("B");
                option.add("C");
                option.add("D");
                layout.addView(show);
                layout.addView(radioGroup);
            }
            if ("none".equals(pageIdCheck)) {
                layout.addView(button);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        super.setContentView(scrollView);
    }

    @Override
    public void onClick(View v) {
        // if (((RadioButton) v).isChecked()) {
        // Toast.makeText(this, "已被选择" + v.getId(), Toast.LENGTH_SHORT).show();
        // } else {
        // Toast.makeText(this, "未被选择" + v.getId(), Toast.LENGTH_SHORT).show();
        // }
        int i = v.getId() / 10;
        switch (v.getId() % 5) {
        case (1):
            score.put(i, 1);
            break;
        default:
            score.put(i, 0);
            break;
        }
    }

    public void upload(final float score, final String pageId,
            final String teacherId, final String testName) {
        HttpUtils http = new HttpUtils();
        RequestParams params = new RequestParams();
        // 获取学生基本信息
        SharedPreferences sp = this.getSharedPreferences("stuInfo",
                Context.MODE_PRIVATE);
        String stuId = sp.getString("stuId", "none");
        if ("none".equals(stuId)) {
            Toast.makeText(TestPageActivity.this, "请先登陆", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        params.addBodyParameter("stuId", stuId);
        params.addBodyParameter("score", Float.toString(score));
        params.addBodyParameter("pageId", pageId);
        params.addBodyParameter("teacherId", teacherId);
        params.addBodyParameter("testName", testName);
        http.send(HttpRequest.HttpMethod.POST, URL, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onLoading(long total, long current,
                            boolean isUploading) {
                        System.out.println();
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            JSONObject jsonObject = new JSONObject(
                                    responseInfo.result);
                            if (jsonObject.getBoolean("check")) {
                                // 存入数据
                                SharedPreferences sp = TestPageActivity.this
                                        .getSharedPreferences("stuInfo",
                                                Context.MODE_PRIVATE);
                                Editor editor = sp.edit();
                                editor.putString(pageId, Float.toString(score));
                                editor.commit();
                                Toast.makeText(TestPageActivity.this,
                                        "提交成功，正确率：" + score + "%",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                Toast.makeText(TestPageActivity.this,
                                        "提交失败，请稍后再", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStart() {
                        System.out.println();
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Toast.makeText(TestPageActivity.this, "联网失败，请检查您的网络!",
                                1).show();
                    }
                });
    }
}
