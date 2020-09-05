package com.hx.mt.common;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hx.mt.service.MeiTuanAppAccessibilityService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Dusan (duqian) on 2017/5/8 - 16:28.
 * E-mail: duqian2010@gmail.com
 * Description:辅助功能通用方法库
 * remarks:
 */
public class AccessibilityHelper {
    private static final String TAG = AccessibilityHelper.class.getSimpleName();

    /**
     * 判断辅助服务是否正在运行
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isServiceRunning(MeiTuanAppAccessibilityService service) {
        if (service == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = service.getServiceInfo();
        if (info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if (i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if (!isConnect) {
            return false;
        }
        return true;
    }

    public static boolean checkPermission(Context context) {
        int ok = 0;
        try {
            ok = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }

        TextUtils.SimpleStringSplitter ms = new TextUtils.SimpleStringSplitter(':');
        if (ok == 1) {
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                ms.setString(settingValue);
                while (ms.hasNext()) {
                    String accessibilityService = ms.next();
                    if (accessibilityService.contains(MeiTuanAppAccessibilityService.class.getSimpleName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 打开辅助服务的设置
     */
    public static void openAccessibilityServiceSettings(Activity context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 自动点击按钮
     *
     * @param event
     * @param nodeText 按钮文本
     */
    public static void handleEvent(AccessibilityEvent event, String nodeText) {
        List<AccessibilityNodeInfo> unintall_nodes = event.getSource().findAccessibilityNodeInfosByText(nodeText);
        if (unintall_nodes != null && !unintall_nodes.isEmpty()) {
            AccessibilityNodeInfo node;
            for (int i = 0; i < unintall_nodes.size(); i++) {
                node = unintall_nodes.get(i);
                if (node.getClassName().equals("android.widget.Button") && node.isEnabled()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }


    private AccessibilityHelper() {
    }


    //通过id查找
    public static AccessibilityNodeInfo findNodeInfosById(AccessibilityNodeInfo nodeInfo, String resId) {
        if (nodeInfo == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    public static List<AccessibilityNodeInfo> findNodeInfosByResId(AccessibilityNodeInfo nodeInfo, String resId) {
        if (nodeInfo == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            if (list != null && !list.isEmpty()) {
                return list;
            }
        }
        return null;
    }

    //返回指定位置的node
    public static AccessibilityNodeInfo findNodeInfosByIdAndPosition(AccessibilityNodeInfo nodeInfo, String resId, int position) {
        if (nodeInfo == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
            for (int i = 0; i < list.size(); i++) {
                if (i == position) {
                    return list.get(i);
                }
            }
            Log.e(TAG, "size=" + list.size());
        }
        return null;
    }

    //通过某个文本查找
    public static AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) return null;
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    //通过多个关键字查找
    public static AccessibilityNodeInfo findNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String... texts) {
        if (nodeInfo == null) return null;
        for (String key : texts) {
            AccessibilityNodeInfo info = findNodeInfosByText(nodeInfo, key);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    /**
     * 根据控件id 获取相关控件集合
     *
     * @param root
     * @param id
     * @return
     */
    public static List<AccessibilityNodeInfo> getList(AccessibilityNodeInfo root, String id) {
        List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByViewId(id);
        return list;
    }

    /**
     * 根据 控件的className 获取相关控件的集合
     *
     * @param root
     * @param className
     * @return
     */
    public static List<AccessibilityNodeInfo> traverseNodeByClassList(AccessibilityNodeInfo root, String className) {
        List<AccessibilityNodeInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(className) || root == null) {
            return Collections.EMPTY_LIST;
        }
        List<AccessibilityNodeInfo> list2 = new ArrayList<>();
        traverseNodeClassToList(root, list2);
        for (AccessibilityNodeInfo nodeInfo : list2) {
            if (nodeInfo.getClassName() != null && className.equals(nodeInfo.getClassName().toString())) {
                list.add(nodeInfo);
            }
        }
        return list;
    }

    public static void traverseNodeClassToList(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> list) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                list.add(child);
                if (child.getChildCount() > 0) {
                    traverseNodeClassToList(child, list);
                }
            }
        }
    }

    public static boolean findNodeBySomeText(AccessibilityNodeInfo root, String className, String text) {
        try {
            AccessibilityNodeInfo clickNode = null;
            List<AccessibilityNodeInfo> nodeInfos = traverseNodeByClassList(root, className);
            for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
                if (nodeInfo.getText() != null && nodeInfo.getText().toString().contains(text)) {
                    clickNode = nodeInfo;
                }
            }

            while (clickNode != null && !clickNode.isClickable()) {
                clickNode = clickNode.getParent();
            }
            if (clickNode != null) {
                boolean result = false;
                try {
                    //wait some times
                    result = clickNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
        } catch (Exception e) {
            Log.e("Exception", e.toString());
            e.printStackTrace();
        }
        Log.e("null", "clickNode is null");
        return false;

    }


    public static boolean findNodeByTextAndPerformClick(AccessibilityNodeInfo root, String text) {
        if (root == null && text == null) {
            return false;
        }
        try {
            List<AccessibilityNodeInfo> txtNodeInfoList = root.findAccessibilityNodeInfosByText(text);
            if (txtNodeInfoList == null || txtNodeInfoList.isEmpty()) {
                Log.e("process", "没有找到" + text + "按钮");
                //   findNodeByTextAndPerformClick(root,text);
                return false;
            }
            AccessibilityNodeInfo clickNode = null;
            for (AccessibilityNodeInfo nodeInfo : txtNodeInfoList) {
                if (nodeInfo.getText() != null && text.equals(nodeInfo.getText().toString())) {
                    clickNode = nodeInfo;
                    Log.e("process", "text= " + nodeInfo.getText());
                }
            }

            while (clickNode != null && !clickNode.isClickable()) {
                clickNode = clickNode.getParent();
            }
            if (clickNode != null) {
                boolean result = false;
                try {
                    //wait some times
                    result = clickNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
        } catch (Exception e) {
            Log.e("process", e.toString());
            e.printStackTrace();
        }
        Log.e("null", "clickNode is null");
        return false;
    }

    //通过ClassName查找
    public static AccessibilityNodeInfo findNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if (className.equals(node.getClassName())) {
                return node;
            }
        }
        return null;
    }

    //通过ClassName查找
    public static AccessibilityNodeInfo findNodeInfosByDes(AccessibilityNodeInfo nodeInfo, String des) {
        if (TextUtils.isEmpty(des)) {
            return null;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            CharSequence contentDescription = node.getContentDescription();
            Log.e("tag-contentDescription", "----" + contentDescription + "----");
            if (des.equals(node.getContentDescription())) {
                return node;
            }
        }
        return null;
    }

    /**
     * 找父组件
     */
    public static AccessibilityNodeInfo findParentNodeInfosByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (nodeInfo == null) {
            return null;
        }
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        if (className.equals(nodeInfo.getClassName())) {
            return nodeInfo;
        }
        return findParentNodeInfosByClassName(nodeInfo.getParent(), className);
    }

    private static final Field sSourceNodeField;

    static {
        Field field = null;
        try {
            field = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sSourceNodeField = field;
    }

    public static long getSourceNodeId(AccessibilityNodeInfo nodeInfo) {
        if (sSourceNodeField == null) {
            return -1;
        }
        try {
            return sSourceNodeField.getLong(nodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getViewIdResourceName(AccessibilityNodeInfo nodeInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return nodeInfo.getViewIdResourceName();
        }
        return null;
    }

    //返回HOME界面
    public static void performHome(AccessibilityService service) {
        if (service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    //返回
    public static void performBack(AccessibilityService service) {
        if (service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 点击事件
     */
    public static boolean performClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        if (nodeInfo.isClickable()) {
            return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            return performClick(nodeInfo.getParent());
        }
    }

    //长按事件
    public static void performLongClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
    }

    //move 事件
    @TargetApi(23)
    public static boolean performMoveDown(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.getId());
    }


    //ACTION_SCROLL_FORWARD 事件
    public static boolean perform_scroll_forward(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean perform_scroll_position(AccessibilityNodeInfo nodeInfo, int position) {
        Bundle arguments = new Bundle();
        arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, position);
        if (nodeInfo == null) {
            return false;
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.getId(), arguments);
    }


    //ACTION_SCROLL_BACKWARD 后退事件
    public static boolean perform_scroll_backward(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    //粘贴
    @TargetApi(18)
    public static void performPaste(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    //设置editview text
    @TargetApi(21)
    public static void performSetText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) {
            return;
        }
        CharSequence className = nodeInfo.getClassName();
        if ("android.widget.EditText".equals(className)) {//||"android.widget.TextView".equals(className)
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
    }


    public static List<AccessibilityNodeInfo> findNodeByClassList(AccessibilityNodeInfo root, String className) {
        List<AccessibilityNodeInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(className) || root == null) {
            return Collections.EMPTY_LIST;
        }
        int childCount = root.getChildCount();
        Log.e("childCount", "" + childCount);
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo rootChild = root.getChild(i);
            Log.e("rootChild", rootChild.toString());
            Log.e("rootChild-", i + "");
            if (rootChild != null) {
                String s = rootChild.getClassName().toString();
                Log.e("rootChildClassName", s);
                if (className.equals(rootChild.getClassName().toString().trim())) {
                    list.add(rootChild);
                }
            }
        }
        return list;
    }

    public static List<AccessibilityNodeInfo> traverseNodeByClassToList(AccessibilityNodeInfo root, String className) {
        List<AccessibilityNodeInfo> list = new ArrayList<>();
        if (TextUtils.isEmpty(className) || root == null) {
            return Collections.EMPTY_LIST;
        }
        List<AccessibilityNodeInfo> list2 = new ArrayList<>();
        traverseNodeClassToList1(root, list2);
        for (AccessibilityNodeInfo nodeInfo : list2) {
            if (nodeInfo.getClassName() != null && className.equals(nodeInfo.getClassName().toString())) {
                list.add(nodeInfo);
            }
        }
        return list;
    }

    public static void traverseNodeClassToList1(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> list) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                list.add(child);
                if (child.getChildCount() > 0) {
                    traverseNodeClassToList(child, list);
                }
            }
        }
    }


    /**
     * edit内 粘贴内容
     *
     * @param root
     * @param message
     * @param accessibilityService
     */
    public static void pasteText(AccessibilityNodeInfo root, String message, AccessibilityService accessibilityService) {
        AccessibilityNodeInfo target = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        Bundle arguments = new Bundle();
        arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
        arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                true);
        target.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                arguments);
        target.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        ClipData clip = ClipData.newPlainText("message", message);
        ClipboardManager clipboardManager = (ClipboardManager) accessibilityService.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clip);
        target.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }
}
