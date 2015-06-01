package cn.cuit.wlgc.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
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
        try {
            JSONObject jsonObject = new JSONObject(testJson);
            final String msg = jsonObject.getString("msg");
            JSONArray jsonArray = jsonObject.getJSONArray("testInfo");

            Button button = new Button(this);
            button.setText("完成");
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (score.size() < Integer.parseInt(msg)) {
                        Toast.makeText(TestPageActivity.this, "请做完选择",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    float number = 0;
                    for (Map.Entry<Integer, Integer> entry : score.entrySet()) {
                        if (entry.getValue() == 1) {
                            number++;
                        }
                    }
                    float finalScore = (number * 100f) / score.size();
                    Toast.makeText(TestPageActivity.this, "分数" + finalScore,
                            Toast.LENGTH_SHORT).show();
                }
            });

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
            layout.addView(button);
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
}
