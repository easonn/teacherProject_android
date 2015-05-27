package cn.cuit.wlgc.example;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
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
 * User: special Date: 13-12-22 Time: 下午1:31 Mail: specialcyci@gmail.com
 */
public class ProfileFragment extends Fragment {

    private View parentView;
    private ResideMenu resideMenu;
//    private final String URL = getString(R.string.URL_LOGIN);
    private final String URL = "http://192.168.1.7:8080/example-server/student/login";

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
                        // if(test.get("status").toString() == "1"){
                        //
                        // } else{
                        //
                        // }
                        Toast.makeText(getActivity(), test.get("msg"),
                                Toast.LENGTH_SHORT).show();
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