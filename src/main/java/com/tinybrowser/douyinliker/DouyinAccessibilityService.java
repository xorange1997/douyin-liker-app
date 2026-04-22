package com.tinybrowser.douyinliker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * 🥯 抖音点赞无障碍服务
 * 独立实现，不依赖第三方库
 */
public class DouyinAccessibilityService extends AccessibilityService {

    private static final String TAG = "DouyinLiker";
    private static final String DOUYIN_PACKAGE = "com.ss.android.ugc.aweme";
    
    // 配置参数
    private int targetLikeCount = 100;
    private int currentLikeCount = 0;
    private boolean isRunning = false;
    private long delayMin = 1000;
    private long delayMax = 3000;
    
    // 评论功能
    private boolean enableComment = false;
    private int commentInterval = 10;  // 每点赞多少个评论一次
    private String[] comments = {
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
    
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "🥯 服务已连接");
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
                | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isRunning) return;
        
        if (event.getPackageName() == null || 
            !event.getPackageName().toString().equals(DOUYIN_PACKAGE)) {
            return;
        }
        
        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            performLikeAction();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "服务中断");
        isRunning = false;
    }

    /**
     * 开始点赞任务
     */
    public void startLikeTask(int count) {
        targetLikeCount = count;
        currentLikeCount = 0;
        isRunning = true;
        Log.d(TAG, "开始任务：目标 " + count + " 个赞");
        
        mainHandler.postDelayed(this::performLikeAction, 2000);
    }

    /**
     * 停止任务
     */
    public void stopTask() {
        isRunning = false;
        Log.d(TAG, "任务停止，已点赞：" + currentLikeCount);
    }

    /**
     * 执行点赞动作
     */
    private void performLikeAction() {
        if (!isRunning) return;
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // 查找爱心按钮
        boolean liked = findAndClickLikeButton(rootNode);
        
        if (liked) {
            currentLikeCount++;
            Log.d(TAG, "✅ 点赞 " + currentLikeCount + "/" + targetLikeCount);
            
            // 检查是否需要评论
            if (enableComment && currentLikeCount % commentInterval == 0) {
                Log.d(TAG, "💬 准备评论...");
                performComment(rootNode);
            }
            
            if (currentLikeCount >= targetLikeCount) {
                Log.d(TAG, "🎉 任务完成！");
                isRunning = false;
                return;
            }
        }

        // 延迟后滑动
        long delay = random(delayMin, delayMax);
        mainHandler.postDelayed(this::scrollUp, delay);
    }

    /**
     * 查找并点击爱心按钮
     */
    private boolean findAndClickLikeButton(AccessibilityNodeInfo node) {
        if (node == null) return false;

        // 方法 1：查找描述包含"赞"的按钮
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;

            CharSequence desc = child.getContentDescription();
            if (desc != null && desc.toString().contains("赞")) {
                if (child.isClickable()) {
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "点击爱心按钮");
                    return true;
                }
            }

            // 递归查找
            if (findAndClickLikeButton(child)) {
                return true;
            }
        }

        // 方法 2：根据位置点击（右侧爱心区域）
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        float clickX = screenWidth * 0.85f;
        float clickY = screenHeight * 0.55f;

        if (performGesture(clickX, clickY)) {
            Log.d(TAG, "手势点击");
            return true;
        }

        return false;
    }

    /**
     * 向上滑动（下一个视频）
     */
    private void scrollUp() {
        if (!isRunning) return;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        float startX = screenWidth / 2.0f;
        float startY = screenHeight * 0.7f;
        float endX = screenWidth / 2.0f;
        float endY = screenHeight * 0.3f;

        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));

        dispatchGesture(builder.build(), null, null);
        Log.d(TAG, "📱 滑动到下一个视频");
    }

    /**
     * 执行手势点击
     */
    private boolean performGesture(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 10));

        return dispatchGesture(builder.build(), null, null);
    }

    /**
     * 生成随机数
     */
    private long random(long min, long max) {
        return (long) (Math.random() * (max - min)) + min;
    }

    /**
     * 执行评论
     */
    private void performComment(AccessibilityNodeInfo rootNode) {
        // 查找评论按钮
        AccessibilityNodeInfo commentBtn = findNodeByText(rootNode, "评论");
        if (commentBtn == null) {
            Log.d(TAG, "未找到评论按钮");
            return;
        }
        
        // 点击评论按钮
        if (commentBtn.isClickable()) {
            commentBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Log.d(TAG, "点击评论按钮");
        }
        
        // 等待评论框弹出
        mainHandler.postDelayed(() -> {
            AccessibilityNodeInfo newRoot = getRootInActiveWindow();
            if (newRoot == null) return;
            
            // 查找评论输入框
            AccessibilityNodeInfo inputBox = findEditText(newRoot);
            if (inputBox == null) {
                Log.d(TAG, "未找到评论输入框");
                return;
            }
            
            // 随机选择一条评论
            String comment = comments[random(0, comments.length)];
            Log.d(TAG, "评论内容：" + comment);
            
            // 输入评论
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, comment);
            inputBox.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            Log.d(TAG, "输入评论完成");
            
            // 等待输入完成
            mainHandler.postDelayed(() -> {
                // 查找发送按钮
                AccessibilityNodeInfo sendBtn = findNodeByText(getRootInActiveWindow(), "发布");
                if (sendBtn == null) {
                    sendBtn = findNodeByText(getRootInActiveWindow(), "发送");
                }
                
                if (sendBtn != null && sendBtn.isClickable()) {
                    sendBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "✅ 评论发送成功");
                } else {
                    Log.d(TAG, "未找到发送按钮");
                }
            }, 1000);
        }, 500);
    }

    /**
     * 查找文本节点
     */
    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo root, String text) {
        if (root == null) return null;
        
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child == null) continue;
            
            CharSequence contentDesc = child.getContentDescription();
            CharSequence textContent = child.getText();
            
            if ((contentDesc != null && contentDesc.toString().contains(text)) ||
                (textContent != null && textContent.toString().contains(text))) {
                return child;
            }
            
            AccessibilityNodeInfo result = findNodeByText(child, text);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }

    /**
     * 查找编辑框
     */
    private AccessibilityNodeInfo findEditText(AccessibilityNodeInfo root) {
        if (root == null) return null;
        
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child == null) continue;
            
            if (child.getClassName() != null && 
                child.getClassName().toString().contains("EditText")) {
                return child;
            }
            
            AccessibilityNodeInfo result = findEditText(child);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }

    /**
     * 设置评论功能
     */
    public void setCommentEnabled(boolean enabled, int interval) {
        this.enableComment = enabled;
        this.commentInterval = interval;
        Log.d(TAG, "评论功能：" + (enabled ? "开启" : "关闭") + ", 间隔：" + interval);
    }

    /**
     * 添加自定义评论
     */
    public void addCustomComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) return;
        
        String[] newComments = new String[comments.length + 1];
        System.arraycopy(comments, 0, newComments, 0, comments.length);
        newComments[comments.length] = comment.trim();
        comments = newComments;
        Log.d(TAG, "添加自定义评论：" + comment);
    }

    /**
     * 获取评论列表
     */
    public String[] getComments() {
        return comments;
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
}
