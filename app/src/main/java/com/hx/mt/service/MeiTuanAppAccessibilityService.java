package com.hx.mt.service;

import android.accessibilityservice.AccessibilityService;
import android.mtp.MtpConstants;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hx.mt.common.AccessibilityHelper;
import com.hx.mt.common.MtAppConst;

import java.util.List;

public class MeiTuanAppAccessibilityService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 此方法是在主线程中回调过来的，所以消息是阻塞执行的
        // 获取包名
        String mClassName = (String) event.getClassName();
        int eventType = event.getEventType();
        System.err.println("------------------------------------------------------");
        Log.e("tag-mTypelassName", "----" + mClassName + "----" + eventType);
        if (mClassName.equals(MtAppConst.MainActivity)) {
          //  AccessibilityHelper.findNodeByTextAndPerformClick(getRoot(), "骑车");
//            AccessibilityNodeInfo nodeInfo = AccessibilityHelper.findNodeInfosByDes(getRoot(), "外卖");
//            AccessibilityHelper.performClick(nodeInfo);

            List<AccessibilityNodeInfo> accessibilityNodeInfos = AccessibilityHelper.traverseNodeByClassList(getRoot(), "android.view.View");
            for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos) {
                Log.e("tag-", "----" + accessibilityNodeInfo.getContentDescription() + "----" + eventType);
                String contentDescription = (String) accessibilityNodeInfo.getContentDescription();
                if ("外卖".equals(contentDescription)){
                    AccessibilityHelper.performClick(accessibilityNodeInfo);
                }
            }
        }

    }

    @Override
    public void onInterrupt() {
        System.err.println("end--------------------");
    }


    public AccessibilityNodeInfo getRoot() {
        return getRootInActiveWindow();
    }
}