package cn.cuit.wlgc.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import cn.cuit.wlgc.example.plugin.ui.GroupFragment;
import cn.jpush.android.api.JPushInterface;

import com.google.gson.Gson;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

@SuppressLint({ "NewApi", "CommitPrefEdits" })
public class MenuActivity extends FragmentActivity implements
        View.OnClickListener {

    private ResideMenu resideMenu;
    private MenuActivity mContext;
    private ResideMenuItem itemHome;
    private ResideMenuItem itemProfile;
    private ResideMenuItem itemCalendar;
    private ResideMenuItem itemTestPagedar;
    private ResideMenuItem itemSettings;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mContext = this;
        setUpMenu();
        JPushInterface.setDebugMode(true); // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this); // 初始化 JPush
        if (getIntent().getExtras() == null)
            changeFragment(new HomeFragment());
        else if ("随堂测评".equals(getIntent().getExtras().getString(
                "cn.jpush.android.NOTIFICATION_CONTENT_TITLE"))) {
            changeFragment(new CalendarFragment());
        } else if ("课后作业".equals(getIntent().getExtras().getString(
                "cn.jpush.android.NOTIFICATION_CONTENT_TITLE"))) {

            // 推送过来的消息存到手机存储上
            String msg = getIntent().getExtras().getString(
                    "cn.jpush.android.ALERT");
            @SuppressWarnings("unchecked")
            Map<String, String> pageMsg = new Gson().fromJson(msg,
                    HashMap.class);
            String pageId = pageMsg.get("pageId");
            SharedPreferences sp = this.getSharedPreferences("pageInfo",
                    Context.MODE_PRIVATE);
            Set<String> pageInfo = sp.getStringSet(pageId.substring(0, 10),
                    new HashSet<String>());
            pageInfo.add(pageId);
            Editor editor = sp.edit();
            if (pageInfo.size() != 0) {
                editor.remove(pageId.substring(0, 10));
                editor.commit();
            }
            editor.putStringSet(pageId.substring(0, 10), pageInfo);
            try {
                editor.commit();
            } catch (Exception ex) {
                Toast.makeText(this, "result:" + ex.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

            changeFragment(new GroupFragment());
        }
    }

    private void setUpMenu() {

        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        // resideMenu.setMenuListener(menuListener);
        // valid scale factor is between 0.0f and 1.0f. leftmenu'width is
        // 150dip.
        resideMenu.setScaleValue(0.6f);

        // create menu items;
        itemHome = new ResideMenuItem(this, R.drawable.icon_home, "Home");
        itemProfile = new ResideMenuItem(this, R.drawable.icon_profile,
                "Profile");
        itemCalendar = new ResideMenuItem(this, R.drawable.icon_calendar,
                "Calendar");
        itemSettings = new ResideMenuItem(this, R.drawable.icon_settings,
                "Settings");
        itemTestPagedar = new ResideMenuItem(this, R.drawable.icon_settings,
                "TestPage");

        itemHome.setOnClickListener(this);
        itemProfile.setOnClickListener(this);
        itemCalendar.setOnClickListener(this);
        itemSettings.setOnClickListener(this);
        itemTestPagedar.setOnClickListener(this);

        // resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        // resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
        // resideMenu.addMenuItem(itemCalendar, ResideMenu.DIRECTION_RIGHT);
        // resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_RIGHT);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemCalendar, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemTestPagedar, ResideMenu.DIRECTION_LEFT);

        // You can disable a direction by setting ->
        // resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        findViewById(R.id.title_bar_left_menu).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                    }
                });
        /** app右上的按钮 */
        // findViewById(R.id.title_bar_right_menu).setOnClickListener(
        // new View.OnClickListener() {
        // @Override
        // public void onClick(View view) {
        // resideMenu.openMenu(ResideMenu.DIRECTION_RIGHT);
        // }
        // });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {

        if (view == itemHome) {
            changeFragment(new HomeFragment());
        } else if (view == itemProfile) {
            changeFragment(new ProfileFragment());
        } else if (view == itemCalendar) {
            changeFragment(new CalendarFragment());
        } else if (view == itemSettings) {
            changeFragment(new SettingsFragment());
        } else if (view == itemTestPagedar) {
            changeFragment(new GroupFragment());
        }

        resideMenu.closeMenu();
    }

    /** 界面展开响应 */
    // private ResideMenu.OnMenuListener menuListener = new
    // ResideMenu.OnMenuListener() {
    // @Override
    // public void openMenu() {
    // Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT)
    // .show();
    // }
    //
    // @Override
    // public void closeMenu() {
    // Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT)
    // .show();
    // }
    // };

    private void changeFragment(Fragment targetFragment) {
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu() {
        return resideMenu;
    }
}
