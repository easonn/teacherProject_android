package cn.cuit.wlgc.example.plugin.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.cuit.wlgc.example.R;
import cn.cuit.wlgc.example.plugin.ui.PinnedHeaderExpandableListView.OnHeaderUpdateListener;
import cn.cuit.wlgc.example.plugin.ui.StickyLayout.OnGiveUpTouchEventListener;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

public class GroupFragment extends Fragment implements
        ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener, OnHeaderUpdateListener,
        OnGiveUpTouchEventListener {
    private PinnedHeaderExpandableListView expandableListView;
    private StickyLayout stickyLayout;
    private ArrayList<Group> groupList;
    private ArrayList<List<TestPage>> childList;
    private View parentView;
    private MyexpandableListAdapter adapter;
    private Map<String, Set<String>> pageInfo;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.grouplist, container, false);
        expandableListView = (PinnedHeaderExpandableListView) parentView
                .findViewById(R.id.expandablelist);
        stickyLayout = (StickyLayout) parentView
                .findViewById(R.id.sticky_layout);
        initData();

        adapter = new MyexpandableListAdapter(getActivity());
        expandableListView.setAdapter(adapter);

        // 展开所有group
        for (int i = 0, count = expandableListView.getCount(); i < count; i++) {
            expandableListView.expandGroup(i);
        }

        expandableListView.setOnHeaderUpdateListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnGroupClickListener(this);
        stickyLayout.setOnGiveUpTouchEventListener(this);
        return parentView;
    }

    /***
     * InitData
     */
    void initData() {

        SharedPreferences sp = getActivity().getSharedPreferences("pageInfo",
                Context.MODE_PRIVATE);
        groupList = new ArrayList<Group>();
        childList = new ArrayList<List<TestPage>>();
        pageInfo = (Map<String, Set<String>>) sp.getAll();
        for (Map.Entry<String, Set<String>> entry : pageInfo.entrySet()) {
            Group group = new Group();
            group.setTitle(entry.getKey());
            groupList.add(group);
            ArrayList<TestPage> childTemp = new ArrayList<TestPage>();
            for (String str : entry.getValue()) {
                TestPage test = new TestPage();
                test.setName(str);
                childTemp.add(test);
            }
            childList.add(childTemp);
        }
    }

    /***
     * 数据源
     * 
     * @author Administrator
     * 
     */
    class MyexpandableListAdapter extends BaseExpandableListAdapter {
        private Context context;
        private LayoutInflater inflater;

        public MyexpandableListAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        // 返回父列表个数
        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        // 返回子列表个数
        @Override
        public int getChildrenCount(int groupPosition) {
            return childList.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {

            return groupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {

            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            GroupHolder groupHolder = null;
            if (convertView == null) {
                groupHolder = new GroupHolder();
                convertView = inflater.inflate(R.layout.group, null);
                groupHolder.textView = (TextView) convertView
                        .findViewById(R.id.group);
                // groupHolder.imageView = (ImageView) convertView
                // .findViewById(R.id.image);
                convertView.setTag(groupHolder);
            } else {
                groupHolder = (GroupHolder) convertView.getTag();

            }

            groupHolder.textView.setText(((Group) getGroup(groupPosition))
                    .getTitle());
            // if (isExpanded)// ture is Expanded or false is not isExpanded
            // groupHolder.imageView.setImageResource(R.drawable.expanded);
            // else
            // groupHolder.imageView.setImageResource(R.drawable.collapse);
            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition,
                final int childPosition, boolean isLastChild, View convertView,
                ViewGroup parent) {
            ChildHolder childHolder = null;
            if (convertView == null) {
                childHolder = new ChildHolder();
                convertView = inflater.inflate(R.layout.child, null);

                childHolder.textName = (TextView) convertView
                        .findViewById(R.id.name);
                childHolder.textAge = (TextView) convertView
                        .findViewById(R.id.age);
                childHolder.textAddress = (TextView) convertView
                        .findViewById(R.id.address);
                // childHolder.imageView = (ImageView) convertView
                // .findViewById(R.id.image);
                // final String info =
                // childHolder.textName.getText().toString();
                Button button = (Button) convertView.findViewById(R.id.button1);
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toast.makeText(
                        // parentView.getContext(),
                        // childList.get(groupPosition).get(childPosition)
                        // .getName(), 1).show();
                        String pageId = childList.get(groupPosition)
                                .get(childPosition).getName();
                        try {
                            FileInputStream fin = getActivity().openFileInput(
                                    pageId + ".json");
                            int length = fin.available();
                            byte[] buffer = new byte[length];
                            fin.read(buffer);
                            String info = EncodingUtils.getString(buffer,
                                    "UTF-8");
                            fin.close();
                            Toast.makeText(parentView.getContext(),
                                    "clicked pos=" + info, Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        downloadPage(pageId);
                        // Toast.makeText(parentView.getContext(),
                        // "clicked pos="+info,
                        // Toast.LENGTH_SHORT).show();
                    }
                });

                convertView.setTag(childHolder);
            } else {
                childHolder = (ChildHolder) convertView.getTag();
            }

            childHolder.textName.setText(((TestPage) getChild(groupPosition,
                    childPosition)).getName());
            // childHolder.textAge.setText(String.valueOf(((TestPage) getChild(
            // groupPosition, childPosition)).getAge()));
            // childHolder.textAddress.setText(((TestPage)
            // getChild(groupPosition,
            // childPosition)).getAddress());

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
            int groupPosition, final long id) {

        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        Toast.makeText(parentView.getContext(),
                childList.get(groupPosition).get(childPosition).getName(), 1)
                .show();

        return false;
    }

    class GroupHolder {
        TextView textView;
        ImageView imageView;
    }

    class ChildHolder {
        TextView textName;
        TextView textAge;
        TextView textAddress;
        ImageView imageView;
    }

    public View getPinnedHeader() {
        View headerView = (ViewGroup) getActivity().getLayoutInflater()
                .inflate(R.layout.group, null);
        headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        return headerView;
    }

    @Override
    public void updatePinnedHeader(View headerView, int firstVisibleGroupPos) {
        Group firstVisibleGroup = (Group) adapter
                .getGroup(firstVisibleGroupPos);
        TextView textView = (TextView) headerView.findViewById(R.id.group);
        textView.setText(firstVisibleGroup.getTitle());
    }

    @Override
    public boolean giveUpTouchEvent(MotionEvent event) {
        if (expandableListView.getFirstVisiblePosition() == 0) {
            View view = expandableListView.getChildAt(0);
            if (view != null && view.getTop() >= 0) {
                return true;
            }
        }
        return false;
    }

    public void downloadPage(final String pageId) {

        HttpUtils http = new HttpUtils();
        RequestParams params = new RequestParams();
        params.addBodyParameter("pageId", pageId);
        http.send(HttpRequest.HttpMethod.POST,
                getString(R.string.URL_DOWNLOAD), params,
                new RequestCallBack<String>() {
                    @Override
                    public void onLoading(long total, long current,
                            boolean isUploading) {
                        System.out.println();
                    }

                    @SuppressWarnings("static-access")
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        String fileName = pageId + ".json";
                        try {

                            FileOutputStream fout = getActivity()
                                    .openFileOutput(fileName,
                                            getActivity().MODE_PRIVATE);
                            byte[] bytes = responseInfo.result.getBytes();

                            fout.write(bytes);

                            fout.close();
                        }

                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getActivity(), responseInfo.result,
                                Toast.LENGTH_LONG).show();
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
