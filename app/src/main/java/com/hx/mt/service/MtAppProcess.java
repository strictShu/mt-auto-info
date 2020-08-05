package com.hx.mt.service;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hx.mt.common.AccessibilityHelper;
import com.hx.mt.common.MtAppConst;
import com.hx.mt.common.ProcessStatus;
import com.hx.mt.common.ShopInfo;

/*
 * 美团app流程控制辅助
 */
public class MtAppProcess {


    private ShopInfo shopInfo;

    public void process(ProcessStatus processStatus, String mClassName, int eventType, AccessibilityNodeInfo root) throws InterruptedException {
        switch (mClassName) {
            case MtAppConst
                    .TakeoutHomeActivity:
                //点击商家
                clickStoreName(root);
                //获取商家信息
                shopInfo = getShopInfo(root);
                //获取电话
                clickPhoneButton(root);
                //保存商家信息;
                //saveShopInfo(shopInfo);
                break;
            case MtAppConst.PhoneDialog:
                getPhoneInfo(root, processStatus);
                break;
            case "android.widget.ListView":
//                if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED && processStatus.getCurrentPage().equals("getPhone")) {
//
//                }
                break;
        }
    }

    private void getPhoneInfo(AccessibilityNodeInfo root, ProcessStatus processStatus) {
        processStatus.setCurrentPage("getPhone");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //com.sankuai.meituan:id/dialog_list_view
        AccessibilityNodeInfo phoneButtoNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.PhoneListView);
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < phoneButtoNodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = phoneButtoNodeInfo.getChild(i);
            String phone = (String) child.getText();
            Log.e("process-", "----店铺信息--手机号:" + phone);
            stringBuffer.append(phone + "/");
        }
        shopInfo.setShopPhone(stringBuffer.toString());
        Log.e("process-", "----店铺信息--手机号:" + shopInfo);
    }


    private void clickPhoneButton(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo phoneButtoNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.PhoneButton);
        AccessibilityHelper.performClick(phoneButtoNodeInfo);
    }

    /**
     * 获取商家信息
     *
     * @param root
     */
    private ShopInfo getShopInfo(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo shopNameNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.ShopName);
        String shopName = (String) shopNameNodeInfo.getText();
        AccessibilityNodeInfo shopSalesNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.ShopSales);
        String shopSales = (String) shopSalesNodeInfo.getText();
        AccessibilityNodeInfo shopAddressNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.ShopAddress);
        String shopAddress = (String) shopAddressNodeInfo.getText();
        AccessibilityNodeInfo shopStarNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.ShopStar);
        String shopStar = (String) shopStarNodeInfo.getText();
        ShopInfo shopInfo = new ShopInfo();
        shopInfo.setShopName(shopName);
        shopInfo.setShopSales(shopSales);
        shopInfo.setShopAddress(shopAddress);
        shopInfo.setShopStar(shopStar);
        return shopInfo;
    }

    private void clickStoreName(AccessibilityNodeInfo root) throws InterruptedException {
        Thread.sleep(1000);
        AccessibilityHelper.findNodeByTextAndPerformClick(root, "商家");
    }
}
