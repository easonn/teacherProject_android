package cn.cuit.wlgc.example;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.cuit.wlgc.example.R;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.special.ResideMenu.ResideMenu;

/**
 * login
 */
public class ProfileFragment extends Fragment {

    private View parentView;
    private ResideMenu resideMenu;
//    private final String URL = getString(R.string.URL_LOGIN);
    private final String URL = "http://192.168.1.2:8080/example-server/student/login";

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.profile, container, false);
        setUpViews();
        return parentView;
    }

    private void setUpViews() {
        final MenuActivity parentActivity = (MenuActivity) getActivity();
        resideMenu = parentActivity.getResideMenu();
        usernameEditText = (EditText) parentView
                .findViewById(R.id.login_username_edittext);
        passwordEditText = (EditText) parentView
                .findViewById(R.id.login_password_edittext);
        parentView.findViewById(R.id.btn_open_menu).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        login(usernameEditText.getText().toString().trim(),
                                passwordEditText.getText().toString().trim());
                        // resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                    }
                });

        // add gesture operation's ignored views
        FrameLayout ignored_view = (FrameLayout) parentView
                .findViewById(R.id.ignored_view);
        
        TextView textView = (TextView) parentView.findViewById(R.id.textView1);
        SharedPreferences sp = getActivity().getSharedPreferences("stuInfo",
                Context.MODE_PRIVATE);
        String stuName = sp.getString("stuName", "none");
        String info = "";
        if ("none".equals(stuName)) {
            info = "当前没有登陆";
        } else {
            info = "当前学生： " + stuName;
        }
        textView.setText(info);
        resideMenu.addIgnoredView(ignored_view);

    }

    public void login(String stuId, String stuPass) {
        HttpUtils http = new HttpUtils();
        RequestParams params = new RequestParams();

        params.addBodyParameter("student.stuId", stuId);
        params.addBodyParameter("student.stuPass", stuPass);
        http.send(HttpRequest.HttpMethod.POST, URL, params,
                new RequestCallBack<String>() {
                    @Override
                    public void onLoading(long total, long current,
                            boolean isUploading) {
                        System.out.println();
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        Gson json = new Gson();
                        @SuppressWarnings("unchecked")
                        Map<String, String> test = json.fromJson(
                                responseInfo.result, HashMap.class);
                        if (test.get("status").toString().equals("1")) {// success
                            // 采用SharedPreferences方式存储基本信息
                            try {
                                JSONObject stuInfo = new JSONObject(test
                                        .get("stuInfo"));
                                SharedPreferences sp = getActivity()
                                        .getSharedPreferences("stuInfo",
                                                Context.MODE_PRIVATE);
                                // 存入数据
                                Editor editor = sp.edit();
                                editor.putString("stuId",
                                        stuInfo.getString("stuId"));
                                editor.putString("professionName",
                                        stuInfo.getString("professionName"));
                                editor.putString("stuName",
                                        stuInfo.getString("stuName"));
                                editor.putString("professionId",
                                        stuInfo.getString("professionId"));
                                editor.commit();
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(getActivity(), "登陆失败，学号密码错误",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onStart() {
                        System.out.println();
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Toast.makeText(getActivity(), "联网失败，请检查您的网络!", 1)
                                .show();
                    }
                });
    }
}