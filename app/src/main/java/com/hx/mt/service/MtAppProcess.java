package com.hx.mt.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hx.mt.MyApp;
import com.hx.mt.common.AccessibilityHelper;
import com.hx.mt.common.MtAppConst;
import com.hx.mt.common.ProcessStatus;
import com.hx.mt.common.ShopInfo;
import com.hx.mt.util.HttpRequestUtil;
import com.hx.mt.util.ShopCloseException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hx.mt.common.MtAppConst.*;

/*
 * 美团app  外卖流程控制辅助
 * 美团 v11.1
 */
public class MtAppProcess implements HttpRequestUtil.OnResultCallback {

    private MeiTuanAppAccessibilityService accessibilityService;

    private ShopInfo shopInfo;

    private AtomicInteger atomicInteger = new AtomicInteger();

    private ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();

    private ConcurrentHashMap concurrentPage = new ConcurrentHashMap();

    private HttpRequestUtil httpRequestUtil;

    private ProcessStatus processStatus;

    private int pageSize = 100;

    private boolean chenck = true;

    public MtAppProcess(MeiTuanAppAccessibilityService meiTuanAppAccessibilityService) {
        accessibilityService = meiTuanAppAccessibilityService;
        httpRequestUtil = new HttpRequestUtil(this);
        processStatus = new ProcessStatus();
        processStatus.setCurrentStatus("get:address");
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public void process(String mClassName, int eventType, AccessibilityNodeInfo root) throws InterruptedException {
        switch (mClassName) {
            //商家页面
            case MtAppConst
                    .WMRestaurantActivity:
                if (currentPageIng("shopInfo")) {
                    break;
                }
                removePage(TakeoutActivity);
                if (processStatus.getCurrentStatus().equals("getPhone")) {
                    // doSaveShopInfo();
                    break;
                }
                sleepMillsSeconds(2000);
                //防止重复点击 //记录当前商家名称不再点击
                saveCurrentShopName(root);
                //点击商家
                clickStoreName(root);
                //获取商家信息
                getShopInfo(root);
                //获取电话
                clickPhoneButton(root);
                break;
            case MtAppConst.PhoneDialog:
                getPhoneInfo(root);
                break;
            case TakeoutActivity:
                if (currentPageIng(TakeoutActivity)) {
                    break;
                }
                removePage("shopInfo");
                removePage(LocateManuallyActivity);
                if (processStatus.getCurrentStatus().equals("get:address")) {
                    sleepSeconds(1);
                    //点击右上角坐标
                    clickLocationText(root);
                    break;
                }
                //第一次进入滑动
                topScroll(root, mClassName, eventType);
                clickNearShop(root, mClassName, eventType);
                //点击条目
                clickShopItem(root, mClassName, eventType);
                break;
            case SCSuperMarketActivity:
                if (currentPageIng(SCSuperMarketActivity)) {
                    break;
                }
                sleepMillsSeconds(500);
                removePage(TakeoutActivity);
                removePage(WMRestaurantActivity);
                removePage(LocateManuallyActivity);
                processStatus.setCurrentStatus("get:address");
                performGlobalBackAction();
                performGlobalBackAction();
                break;
            case LocateManuallyActivity:
                if (currentPageIng(LocateManuallyActivity)) {
                    break;
                }
                sleepSeconds(1);
                AccessibilityNodeInfo nodeInfosById = AccessibilityHelper.findNodeInfosById(root, shopAddressEditId);
                //AccessibilityHelper.performClick(nodeInfosById);
                topGestureClick((float) 0.15);
                sleepMillsSeconds(500);
                httpRequestUtil.getAddress(nodeInfosById, processStatus);
                removePage(TakeoutActivity);
                removePage(WMRestaurantActivity);
                removePage(LocateManuallyActivity);
                removePage(SCSuperMarketActivity);
                break;
        }
    }

    private void clickNearShop(AccessibilityNodeInfo root, String mClassName, int eventType) {
        sleepMillsSeconds(1000);
        AccessibilityNodeInfo nearShopNode = AccessibilityHelper.findNodeInfosById(root, nearShop);
        try {
            if (nearShopNode != null) {
                AccessibilityNodeInfo child = nearShopNode.getChild(0);
                if (child != null && child.getChildCount() > 0) {
                    AccessibilityNodeInfo child1 = child.getChild(0);
                    AccessibilityHelper.performClick(child1);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSaveShopInfo() {
        saveShopInfo(shopInfo);
        addOne();
        processStatus.setCurrentStatus("scroll");
        Log.e("process-", "当前任务上传数量:" + atomicInteger.get());
        if (atomicInteger.get() >= pageSize) {
            processStatus.setCurrentStatus("get:address");
            atomicInteger.set(0);
        }
    }

    /**
     * 点击第一个搜索地址结果
     *
     * @param root
     * @throws InterruptedException
     */
    private void clickSearchAddressResult(AccessibilityNodeInfo root, AccessibilityNodeInfo editNodeInfo) {
        sleepMillsSeconds(500);
        Log.e("process-", "当前页面:" + root.getClassName());
        AccessibilityNodeInfo searchResultRecyclviewNodeInfo = AccessibilityHelper.findNodeInfosById(root, shopAddressRecycleViewId);
        if (searchResultRecyclviewNodeInfo != null && searchResultRecyclviewNodeInfo.getChildCount() > 0) {
            AccessibilityNodeInfo nodeInfoChild = searchResultRecyclviewNodeInfo.getChild(0);
            removePage(TakeoutActivity);
            AccessibilityHelper.performClick(nodeInfoChild);
            processStatus.setCurrentStatus("address:success");

        } else {
            // TODO: 2020/8/17   搜索无结果 需要重新请求接口
            // if (editNodeInfo.getClassName().equals(LocateManuallyActivity))
            httpRequestUtil.getAddress(editNodeInfo, processStatus);
        }
    }

    private void clickLocationText(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo nodeInfosById = AccessibilityHelper.findNodeInfosById(root, MtAppConst.shopAddressId);
        AccessibilityHelper.performClick(nodeInfosById);
    }

    private boolean currentPageIng(String page) {
        if (concurrentPage.containsKey(page)) {
            return true;
        }
        concurrentPage.put(page, true);
        Log.e("process-", "获取concurrentPage" + concurrentPage + concurrentPage.containsKey(page));
        return false;
    }

    private void saveShopInfo(ShopInfo shopInfo) {
        httpRequestUtil.insertShopInfo(shopInfo);
    }

    private void saveCurrentShopName(AccessibilityNodeInfo root) {
        try {
            String shopName = getShopName(root);
            concurrentHashMap.put(shopName, true);
        } catch (Exception e) {
            if (e instanceof ShopCloseException) {
                Log.e("process-", "获取shop-name fail ,店铺打烊重新获取");
                saveCurrentShopName(root);
            } else {
                Log.e("process-", "获取shop-name fail ,进入店铺获取店铺名称失败");
            }


        }
    }

    private void addOne() {
        atomicInteger.getAndAdd(1);
    }


    private void clickShopItem(AccessibilityNodeInfo root, String mClassName, int eventType) throws InterruptedException {
        try {
            sleepSeconds(3);
            List<AccessibilityNodeInfo> nodeInfosByResId = AccessibilityHelper.findNodeInfosByResId(root, MtAppConst.shopListByNameClick);
            if (nodeInfosByResId == null) {
                Log.e("process-", "--当前列表为空 --");
                clickShopItem(root, mClassName, eventType);
                return;
            }
            for (int i = 0; i < nodeInfosByResId.size(); i++) {
                AccessibilityNodeInfo nodeInfo = nodeInfosByResId.get(i);
                String shopName = (String) nodeInfo.getText();
                if (!concurrentHashMap.containsKey(shopName)) {
                    Log.e("process-", "--点击列表 --" + i + "---" + shopName);
                    processStatus.setCurrentStatus("clickItem");
                    concurrentHashMap.put(shopName, true);
                    AccessibilityHelper.performClick(nodeInfo);
                    break;
                }
                //滑动
                if (i == nodeInfosByResId.size() - 1) {
                    AccessibilityNodeInfo phoneButtoNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.shopListRecycleView);
                    boolean scrollable = AccessibilityHelper.perform_scroll_forward(phoneButtoNodeInfo);
                    Log.e("process-", "--点击列表 滑动" + scrollable);
                    if (scrollable) {
                        clickShopItem(root, mClassName, eventType);
                    } else {
                        //todo 点错时的处理 需要更换地址
                        //不可滑动
                        topGestureClick((float) 0.85);
                        sleepSeconds(1);
                        clickShopItem(root, mClassName, eventType);
                   /*     boolean canScroll = phoneButtoNodeInfo.isScrollable();
                        Log.e("process-", "--再次点击列表 滑动" + canScroll);
                        if (canScroll) {
                            // accessibilityService.dispatchGesture()
                            AccessibilityNodeInfo scroll = AccessibilityHelper.findNodeInfosById(root, MtAppConst.shopListRecycleView);
                            boolean b = scroll.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                            Log.e("process-", "--再次点击列表 滑动 结果" + b);
                        }*/
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sleepSeconds(long seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }

    private void sleepMillsSeconds(long seconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void topScroll(AccessibilityNodeInfo root, String mClassName, int eventType) {
        AccessibilityNodeInfo phoneButtoNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.topRecycleView);
        boolean topScrollAble = AccessibilityHelper.perform_scroll_forward(phoneButtoNodeInfo);
        if (topScrollAble) {
            topScroll(root, mClassName, eventType);
        }
        Log.e("process-", "----店铺列表--滑动");
        if (!"clickItem".equals(processStatus.getCurrentStatus())) {
            processStatus.setCurrentStatus("item");
        }

    }

    private void getPhoneInfo(AccessibilityNodeInfo root) {
        try {
            processStatus.setCurrentStatus("getPhone");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AccessibilityNodeInfo phoneButtoNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.PhoneListView);
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < phoneButtoNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo child = phoneButtoNodeInfo.getChild(i);
                String phone = (String) child.getText();
                Log.e("process-", "----店铺信息--手机号:" + phone);
                stringBuffer.append(phone + "/");
            }
            String substring = stringBuffer.toString().substring(0, stringBuffer.toString().length() - 1);
            shopInfo.setShopPhone(substring);
            Log.e("process-", "----店铺信息--手机号:" + shopInfo);
            removePage(TakeoutActivity);
            sleepMillsSeconds(500);
            doSaveShopInfo();
            performGlobalBackAction();
            sleepMillsSeconds(2000);
            performGlobalBackAction();
        } catch (Exception e) {
            e.printStackTrace();
            removePage(WMRestaurantActivity);
            Log.e("process-", "获取shop-sales fail");
        }
    }

    private void removePage(String page) {
        concurrentPage.remove(page);
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
        if (shopInfo == null) {
            shopInfo = new ShopInfo();
        }
        try {
            String shopName = getShopName(root);
            shopInfo.setShopName(shopName);
        } catch (Exception e) {
            Log.e("process-", "获取shop-name fail");
        }
        try {
            AccessibilityNodeInfo shopSalesNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.ShopSales);
            String shopSales = (String) shopSalesNodeInfo.getText();
            shopInfo.setShopSales(shopSales);
        } catch (Exception e) {
            Log.e("process-", "获取shop-sales fail");
        }

        try {
            AccessibilityNodeInfo shopAddressNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.ShopAddress);
            String shopAddress = (String) shopAddressNodeInfo.getText();
            shopInfo.setShopAddress(shopAddress);
        } catch (Exception e) {
            Log.e("process-", "获取shop-address fail");
        }

        try {
            AccessibilityNodeInfo shopStarNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.ShopStar);
            String shopStar = (String) shopStarNodeInfo.getText();
            shopInfo.setShopStar(shopStar);
        } catch (Exception e) {
            Log.e("process-", "获取shop-star fail");
        }
        return shopInfo;

    }

    private String getShopName(AccessibilityNodeInfo root) {
        try {
            AccessibilityNodeInfo shopNameNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.ShopName);
            return (String) shopNameNodeInfo.getText();
        } catch (Exception e) {
            AccessibilityNodeInfo timeOutNoInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.timeOut);
            if (timeOutNoInfo.getText().equals("本店已打烊")) {
                performGlobalBackAction();
                throw new ShopCloseException();
            }
            throw e;
        }
    }

    private void clickStoreName(AccessibilityNodeInfo root) throws InterruptedException {
        sleepMillsSeconds(1500);
        AccessibilityHelper.findNodeByTextAndPerformClick(root, "商家");
    }

    /**
     * 后退
     */
    private boolean performGlobalBackAction() {
        concurrentPage.put("start", true);
        return accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }


    private void topGestureClick(float heightRatio) {
        DisplayMetrics displayMetrics = MyApp.getContext().getResources().getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(widthPixels / 2, heightPixels * heightRatio);
        // path.lineTo(leftSideOfScreen, middleYValue);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 50));
        GestureDescription build = gestureBuilder.build();

        accessibilityService.dispatchGesture(build, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.e("process-", "--再次点击列表 滑动 结果  success--" + gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.e("process-", "--再次点击列表 滑动 结果 fail--" + gestureDescription);
            }
        }, null);
    }

    @Override
    public void getResult(Object t, AccessibilityNodeInfo nodeInfo, ProcessStatus processStatus) {
        String[] split = (String[]) t;
        //String searhAddress ="美联广场";
        String searhAddress = split[1];
        if (shopInfo == null) {
            shopInfo = new ShopInfo();
        }
        shopInfo.setShopAreaId(split[0]);
        shopInfo.setShopSearchAddress(searhAddress);
        AccessibilityHelper.performSetText(nodeInfo, searhAddress);
        clickSearchAddressResult(accessibilityService.getRoot(), nodeInfo);
    }


}
