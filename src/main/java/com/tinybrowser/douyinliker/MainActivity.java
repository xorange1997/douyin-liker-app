package com.tinybrowser.douyinliker;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * 🥯 主界面
 */
public class MainActivity extends AppCompatActivity {

    private EditText likeCountInput;
    private Button startButton;
    private Button stopButton;
    private Button settingsButton;
    private Button commentButton;
    private Button manageCommentButton;
    private TextView statusText;
    private TextView commentStatusText;
    
    private boolean commentEnabled = false;
    private int commentInterval = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        likeCountInput = findViewById(R.id.likeCountInput);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        settingsButton = findViewById(R.id.settingsButton);
        commentButton = findViewById(R.id.commentButton);
        manageCommentButton = findViewById(R.id.manageCommentButton);
        statusText = findViewById(R.id.statusText);
        commentStatusText = findViewById(R.id.commentStatusText);

        // 默认值
        likeCountInput.setText("100");

        // 检查无障碍服务
        if (!isAccessibilityServiceEnabled()) {
            showToast("请先开启无障碍服务");
            settingsButton.setVisibility(Button.VISIBLE);
        }

        // 评论按钮
        commentButton.setOnClickListener(v -> {
            commentEnabled = !commentEnabled;
            updateCommentStatus();
        });

        // 评论管理按钮
        manageCommentButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CommentManagerActivity.class);
            startActivity(intent);
        });

        // 开始按钮
        startButton.setOnClickListener(v -> {
            if (!isAccessibilityServiceEnabled()) {
                showToast("请先开启无障碍服务");
                openAccessibilitySettings();
                return;
            }

            String countStr = likeCountInput.getText().toString();
            if (countStr.isEmpty()) {
                showToast("请输入点赞数量");
                return;
            }

            int count = Integer.parseInt(countStr);
            if (count <= 0) {
                showToast("点赞数量必须大于 0");
                return;
            }

            // 启动服务
            startLikeTask(count);
        });

        // 停止按钮
        stopButton.setOnClickListener(v -> {
            stopLikeTask();
        });

        // 设置按钮
        settingsButton.setOnClickListener(v -> {
            openAccessibilitySettings();
        });

        updateStatus();
        updateCommentStatus();
    }

    /**
     * 检查无障碍服务是否启用
     */
    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().contains(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打开无障碍设置
     */
    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    /**
     * 开始点赞任务
     */
    private void startLikeTask(int count) {
        showToast("开始点赞：" + count + " 个");
        if (commentEnabled) {
            showToast("评论功能已开启，每 " + commentInterval + " 个评论一次");
        }
        statusText.setText("状态：运行中...");
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    /**
     * 停止任务
     */
    private void stopLikeTask() {
        showToast("任务已停止");
        statusText.setText("状态：已停止");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        commentEnabled = false;
        updateCommentStatus();
    }

    /**
     * 更新状态
     */
    private void updateStatus() {
        if (isAccessibilityServiceEnabled()) {
            statusText.setText("状态：就绪");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        } else {
            statusText.setText("状态：无障碍服务未启用");
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
    }

    /**
     * 更新评论状态
     */
    private void updateCommentStatus() {
        if (commentEnabled) {
            commentStatusText.setText("💬 评论：开启 (每 " + commentInterval + " 个评论一次)");
            commentButton.setText("❌ 关闭评论");
        } else {
            commentStatusText.setText("💬 评论：关闭");
            commentButton.setText("💬 开启评论");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    /**
     * 显示提示
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
