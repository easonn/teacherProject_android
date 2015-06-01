package cn.cuit.wlgc.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

/**
 * suitang test
 */
@SuppressLint("ResourceAsColor")
public class CalendarFragment extends Fragment {

    private View parentView;
    private RadioGroup radioGroup;
    private TextView title1;
    private TextView title2;
    @SuppressWarnings("serial")
    private ArrayList<String> option = new ArrayList<String>() {
        {
            add("A");
            add("B");
            add("C");
            add("D");
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.calendar, container, false);
        radioGroup = (RadioGroup) parentView.findViewById(R.id.radioGroup);
        title1 = (TextView) parentView.findViewById(R.id.title1);
        title2 = (TextView) parentView.findViewById(R.id.title2);
        initView();
        return parentView;
    }

    @SuppressLint("RtlHardcoded")
    private void initView() {
        final MenuActivity parentActivity = (MenuActivity) getActivity();
        Bundle bundle = parentActivity.getIntent().getExtras();
        // ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
        // parentActivity, android.R.layout.simple_list_item_1,
        // getCalendarData(bundle));
        String msg = bundle.getString("cn.jpush.android.ALERT");
        title1.setText(bundle
                .getString("cn.jpush.android.NOTIFICATION_CONTENT_TITLE"));

        Gson gson = new Gson();
        @SuppressWarnings("unchecked")
        Map<String, String> testInfo = gson.fromJson(msg, HashMap.class);
        title2.setText("        " + testInfo.get("testInfo"));
        int i = option.size() - 1;
        for (; i != -1;) {
            int j = (int) Math.random() * i;
            String o = option.get(j);
            option.remove(j);
            i--;
            RadioButton button = new RadioButton(parentActivity);
            button.setGravity(Gravity.RIGHT);
            button.setText(testInfo.get("option" + o));
            button.setTextColor(R.color.text_color);
            radioGroup.addView(button);
        }
    }
}
