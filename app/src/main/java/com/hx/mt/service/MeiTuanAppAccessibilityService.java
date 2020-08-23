package com.hx.mt.service;

import android.accessibilityservice.AccessibilityService;
import android.mtp.MtpConstants;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hx.mt.common.AccessibilityHelper;
import com.hx.mt.common.MtAppConst;
import com.hx.mt.common.ProcessStatus;

import java.util.List;

public class MeiTuanAppAccessibilityService extends AccessibilityService {

    MtAppProcess mtAppProcess;
    private ProcessStatus processStatus;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mtAppProcess = new MtAppProcess(this);
        Log.e("process-", "sucess");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String mClassName = (String) event.getClassName();
        int eventType = event.getEventType();
        Log.e("process-", "----" + mClassName + "----" + eventType);
        if (mClassName.equals(MtAppConst.MainActivity)) {
            List<AccessibilityNodeInfo> accessibilityNodeInfos = AccessibilityHelper.traverseNodeByClassList(getRoot(), "android.view.View");
            for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos) {
                Log.e("process-", "----" + accessibilityNodeInfo.getContentDescription() + "----" + eventType);
                String contentDescription = (String) accessibilityNodeInfo.getContentDescription();
                if ("外卖".equals(contentDescription)) {
                    AccessibilityHelper.performClick(accessibilityNodeInfo);
                }
            }
        }
        try {
            mtAppProcess.process(mClassName, eventType, getRoot());
        } catch (Exception e) {
            e.printStackTrace();
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