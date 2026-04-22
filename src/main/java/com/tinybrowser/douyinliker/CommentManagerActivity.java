package com.tinybrowser.douyinliker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 🥯 评论管理界面
 * 用户可以自定义评论语料
 */
public class CommentManagerActivity extends AppCompatActivity {

    private ListView commentListView;
    private EditText newCommentInput;
    private Button addCommentButton;
    private Button resetButton;
    private Button backButton;
    private TextView countText;

    private List<String> comments;
    private CommentAdapter adapter;
    private SharedPreferences prefs;

    // 默认评论
    private static final String[] DEFAULT_COMMENTS = {
        "太棒了！👍",
        "支持一下！❤️",
        "厉害了！",
        "666！",
        "优秀！🌟",
        "赞赞赞！",
        "收藏了！",
        "学习了！",
        "感谢分享！",
        "好厉害！😍",
        "爱了爱了！",
        "必须支持！",
        "精彩！👏",
        "太实用了！",
        "已转发！",
        "牛逼！",
        "大佬牛逼！",
        "学到了！",
        "Mark 一下！",
        "🔥🔥🔥"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_manager);

        // 初始化
        prefs = getSharedPreferences("douyin_liker", MODE_PRIVATE);
        loadComments();

        // 绑定视图
        commentListView = findViewById(R.id.commentListView);
        newCommentInput = findViewById(R.id.newCommentInput);
        addCommentButton = findViewById(R.id.addCommentButton);
        resetButton = findViewById(R.id.resetButton);
        backButton = findViewById(R.id.backButton);
        countText = findViewById(R.id.countText);

        // 设置适配器
        adapter = new CommentAdapter(comments);
        commentListView.setAdapter(adapter);

        // 更新计数
        updateCount();

        // 添加评论按钮
        addCommentButton.setOnClickListener(v -> addComment());

        // 重置按钮
        resetButton.setOnClickListener(v -> showResetConfirm());

        // 返回按钮
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * 加载评论
     */
    private void loadComments() {
        String saved = prefs.getString("custom_comments", null);
        if (saved != null) {
            comments = new ArrayList<>(Arrays.asList(saved.split("\\|")));
        } else {
            comments = new ArrayList<>(Arrays.asList(DEFAULT_COMMENTS));
        }
    }

    /**
     * 保存评论
     */
    private void saveComments() {
        String joined = String.join("|", comments);
        prefs.edit().putString("custom_comments", joined).apply();
    }

    /**
     * 更新计数显示
     */
    private void updateCount() {
        countText.setText("📝 当前评论数：" + comments.size());
    }

    /**
     * 添加评论
     */
    private void addComment() {
        String text = newCommentInput.getText().toString().trim();
        if (text.isEmpty()) {
            showToast("请输入评论内容");
            return;
        }

        if (text.length() > 50) {
            showToast("评论内容不能超过 50 字");
            return;
        }

        comments.add(text);
        adapter.notifyDataSetChanged();
        saveComments();
        updateCount();

        newCommentInput.setText("");
        showToast("✅ 评论已添加");
    }

    /**
     * 删除评论
     */
    private void deleteComment(int position) {
        if (position < 0 || position >= comments.size()) return;

        comments.remove(position);
        adapter.notifyDataSetChanged();
        saveComments();
        updateCount();
        showToast("已删除");
    }

    /**
     * 重置为默认评论
     */
    private void resetToDefault() {
        comments = new ArrayList<>(Arrays.asList(DEFAULT_COMMENTS));
        adapter = new CommentAdapter(comments);
        commentListView.setAdapter(adapter);
        saveComments();
        updateCount();
        showToast("✅ 已重置为默认评论");
    }

    /**
     * 显示重置确认对话框
     */
    private void showResetConfirm() {
        new AlertDialog.Builder(this)
            .setTitle("重置评论")
            .setMessage("确定要重置为默认评论吗？\n\n自定义的评论将被删除！")
            .setPositiveButton("确定", (dialog, which) -> resetToDefault())
            .setNegativeButton("取消", null)
            .show();
    }

    /**
     * 显示提示
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 评论适配器
     */
    class CommentAdapter extends ArrayAdapter<String> {
        public CommentAdapter(List<String> comments) {
            super(CommentManagerActivity.this, android.R.layout.simple_list_item_2, comments);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView text1 = view.findViewById(android.R.id.text1);
            TextView text2 = view.findViewById(android.R.id.text2);

            String comment = comments.get(position);

            // 限制显示长度
            if (comment.length() > 30) {
                text1.setText(comment.substring(0, 30) + "...");
            } else {
                text1.setText(comment);
            }

            text2.setText("评论 #" + (position + 1));

            // 长按删除
            view.setOnLongClickListener(v -> {
                new AlertDialog.Builder(CommentManagerActivity.this)
                    .setTitle("删除评论")
                    .setMessage("确定删除这条评论吗？")
                    .setPositiveButton("删除", (dialog, which) -> deleteComment(position))
                    .setNegativeButton("取消", null)
                    .show();
                return true;
            });

            return view;
        }
    }
}
