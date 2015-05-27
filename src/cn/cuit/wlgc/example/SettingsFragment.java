package cn.cuit.wlgc.example;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.cuit.wlgc.example.tools.JpushUtils;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.special.ResideMenu.ResideMenu;

/**
 * User: special Date: 13-12-22 Time: 下午3:28 Mail: specialcyci@gmail.com
 */
public class SettingsFragment extends Fragment {
    private static final String TAG = "Jpush";
    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;

    private View parentView;
    private ResideMenu resideMenu;
    private EditText tagsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.settings, container, false);
        setUpViews();
        return parentView;
    }

    private void setUpViews() {
        final MenuActivity parentActivity = (MenuActivity) getActivity();
        resideMenu = parentActivity.getResideMenu();
        tagsText = (EditText) parentView.findViewById(R.id.jpush_tags_text);
        parentView.findViewById(R.id.btn_open_menu).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { // 检查 tag 的有效性
                        setTag(tagsText.getText().toString().trim());
                    }
                });
        TextView textView = (TextView) parentView.findViewById(R.id.textView1);
        // add gesture operation's ignored views
        // 采用SharedPreferences方式存储基本信息
        SharedPreferences sp = getActivity().getSharedPreferences("stuInfo",
                Context.MODE_PRIVATE);
        String tagInfo = sp.getString("JPUSH_TAG", "none");
        String info = "";
        if ("none".equals(tagInfo)) {
            info = "当前没有分组";
        } else {
            info = "当前分组： " + tagInfo;
        }
        textView.setText(info);
        FrameLayout ignored_view = (FrameLayout) parentView
                .findViewById(R.id.ignored_view);
        resideMenu.addIgnoredView(ignored_view);
    }

    private void setTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            Toast.makeText(getActivity(), R.string.error_tag_empty,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String[] sArray = tag.split(",");
        Set<String> tagSet = new LinkedHashSet<String>();
        for (String sTagItme : sArray) {
            if (!SettingsFragment.isValidTagAndAlias(sTagItme)) {
                Toast.makeText(getActivity(), R.string.error_tag_gs_empty,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            tagSet.add(sTagItme);
        }
        // 调用JPush API设置Tag
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TAGS, tagSet));

        // 采用SharedPreferences方式存储基本信息
        SharedPreferences sp = getActivity().getSharedPreferences("stuInfo",
                Context.MODE_PRIVATE);
        // 存入数据
        Editor editor = sp.edit();
        editor.putString("JPUSH_TAG", tag);
        editor.commit();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_SET_ALIAS:
                Log.d(TAG, "Set alias in handler.");
                JPushInterface.setAliasAndTags(getActivity()
                        .getApplicationContext(), (String) msg.obj, null,
                        mAliasCallback);
                break;

            case MSG_SET_TAGS:
                Log.d(TAG, "Set tags in handler.");
                JPushInterface.setAliasAndTags(getActivity()
                        .getApplicationContext(), null, (Set<String>) msg.obj,
                        mTagsCallback);
                break;

            default:
                Log.i(TAG, "Unhandled msg - " + msg.what);
            }
        }
    };
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
            case 0:
                logs = "Set tag and alias success";
                Log.i(TAG, logs);
                break;

            case 6002:
                logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                Log.i(TAG, logs);
                if (JpushUtils.isConnected(getActivity()
                        .getApplicationContext())) {
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_SET_ALIAS, alias),
                            1000 * 60);
                } else {
                    Log.i(TAG, "No network");
                }
                break;

            default:
                logs = "Failed with errorCode = " + code;
                Log.e(TAG, logs);
            }

            JpushUtils.showToast(logs, getActivity().getApplicationContext());
        }

    };

    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
            case 0:
                logs = "Set tag and alias success";
                Log.i(TAG, logs);
                break;

            case 6002:
                logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                Log.i(TAG, logs);
                if (JpushUtils.isConnected(getActivity()
                        .getApplicationContext())) {
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MSG_SET_TAGS, tags),
                            1000 * 60);
                } else {
                    Log.i(TAG, "No network");
                }
                break;

            default:
                logs = "Failed with errorCode = " + code;
                Log.e(TAG, logs);
            }

            JpushUtils.showToast(logs, getActivity().getApplicationContext());
        }

    };

    // 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }
}
