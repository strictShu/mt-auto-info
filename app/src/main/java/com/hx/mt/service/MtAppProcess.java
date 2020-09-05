package com.hx.mt.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.hx.mt.MyApp;
import com.hx.mt.common.AccessibilityHelper;
import com.hx.mt.common.MtAppConst;
import com.hx.mt.common.ProcessStatus;
import com.hx.mt.common.ShopInfo;
import com.hx.mt.util.HttpRequestUtil;
import com.hx.mt.util.ShopCloseException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hx.mt.common.MtAppConst.LocateManuallyActivity;
import static com.hx.mt.common.MtAppConst.MainActivity;
import static com.hx.mt.common.MtAppConst.SCSuperMarketActivity;
import static com.hx.mt.common.MtAppConst.TakeoutActivity;
import static com.hx.mt.common.MtAppConst.WMRestaurantActivity;
import static com.hx.mt.common.MtAppConst.WmRNActivity;
import static com.hx.mt.common.MtAppConst.cancelButton;
import static com.hx.mt.common.MtAppConst.cityName;
import static com.hx.mt.common.MtAppConst.nearShop;
import static com.hx.mt.common.MtAppConst.noGPS;
import static com.hx.mt.common.MtAppConst.searchCtiyClick;
import static com.hx.mt.common.MtAppConst.shopAddressEditId;
import static com.hx.mt.common.MtAppConst.shopAddressRecycleViewId;


/*
 * 美团app  外卖流程控制辅助
 * 美团 v11.1
 */
public class MtAppProcess implements HttpRequestUtil.OnResultCallback {

    private MeiTuanAppAccessibilityService accessibilityService;

    private ShopInfo shopInfo;

    private AtomicInteger atomicInteger = new AtomicInteger();

    private ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();

    private ConcurrentHashMap<String, Long> concurrentPage = new ConcurrentHashMap<String, Long>();

    private HttpRequestUtil httpRequestUtil;

    private ProcessStatus processStatus;

    private int pageSize = 500;

    private boolean chenck = true;

    public MtAppProcess(MeiTuanAppAccessibilityService meiTuanAppAccessibilityService) {
        accessibilityService = meiTuanAppAccessibilityService;
        httpRequestUtil = new HttpRequestUtil(this);
        processStatus = new ProcessStatus();
        processStatus.setCurrentStatus("get:address");
        handler.postDelayed(runnable, 2000);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e("process-", "当前页面保存数量:" + concurrentPage.keySet().size());
            for (String o : concurrentPage.keySet()) {
                Log.e("process-", "当前页面" + o);
                long time = concurrentPage.get(o);
                long currentTimeMillis = System.currentTimeMillis();
                long stopTime = currentTimeMillis - time;
                if (stopTime > 60 * 1000) {
                    Log.e("process-", "当前页面,已经停止超过 60s" + stopTime / 1000 + "-页面" + o);
                    performGlobalBackAction();
                }
            }
             handler.postDelayed(this, 2000);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void process(String mClassName, int eventType, AccessibilityNodeInfo root) throws InterruptedException {
        switch (mClassName) {
            case MainActivity:
                List<AccessibilityNodeInfo> accessibilityNodeInfos = AccessibilityHelper.traverseNodeByClassList(root, "android.view.View");
                for (AccessibilityNodeInfo accessibilityNodeInfo : accessibilityNodeInfos) {
                    Log.e("process-", "----" + accessibilityNodeInfo.getContentDescription() + "----" + eventType);
                    String contentDescription = (String) accessibilityNodeInfo.getContentDescription();
                    if ("外卖".equals(contentDescription)) {
                        processStatus.setCurrentStatus("get:address");
                        removePage(TakeoutActivity);
                        removePage(WMRestaurantActivity);
                        removePage(LocateManuallyActivity);
                        removePage(SCSuperMarketActivity);
                        AccessibilityHelper.performClick(accessibilityNodeInfo);
                    }
                }
                break;
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
                AccessibilityNodeInfo noGPSNodeInfo = AccessibilityHelper.findNodeInfosById(root, noGPS);
                AccessibilityNodeInfo cancelButtonNodeInfo = AccessibilityHelper.findNodeInfosById(root, cancelButton);
                if (noGPSNodeInfo != null && cancelButtonNodeInfo != null&&noGPSNodeInfo.getText().equals("定位失败")) {
                    AccessibilityHelper.performClick(cancelButtonNodeInfo);
                }
                getPhoneInfo(root);
                break;

            //商家列表页面
            case TakeoutActivity:
                if (currentPageIng(TakeoutActivity)) {
                    break;
                }
                removePage("shopInfo");
                removePage(SCSuperMarketActivity);
                removePage(LocateManuallyActivity);
                Log.e("process-", "当前状态:" + processStatus.getCurrentStatus());
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

            //区域内 少量或者没有外卖时会出现的页面
            case SCSuperMarketActivity:
                if (currentPageIng(SCSuperMarketActivity)) {
                    break;
                }
                //atomicInteger.set(0);
                // processStatus.setCurrentStatus("get:address");
                sleepMillsSeconds(5000);
                removePage(TakeoutActivity);
                removePage(WMRestaurantActivity);
                removePage(LocateManuallyActivity);
                performGlobalBackAction();
                break;
            //更换位置.
            case LocateManuallyActivity:
                if (currentPageIng(LocateManuallyActivity)) {
                    break;
                }
                httpRequestUtil.getAllAddress();
                sleepSeconds(1);
                AccessibilityNodeInfo nodeInfosById = AccessibilityHelper.findNodeInfosById(root, shopAddressEditId);
                //AccessibilityHelper.performClick(nodeInfosById);
                topGestureClick((float) 0.15);
                sleepMillsSeconds(500);
                if (concurrentPage.containsKey("search")) {
                    AccessibilityHelper.performSetText(nodeInfosById, processStatus.getSearchAddress());
                    clickSearchAddressResult(accessibilityService.getRoot(), nodeInfosById);
                    removePage("search");
                } else {
                    httpRequestUtil.getAddress(nodeInfosById, processStatus);
                }
                removeAll();
                break;
            case WmRNActivity:
                if (currentPageIng(WmRNActivity)) {
                    break;
                }
                sleepSeconds(3);
                topGestureClick((float) 0.06);
                // fix me
                break;
            case "android.widget.EditText":
                if (concurrentPage.containsKey(WmRNActivity)) {
                    removePage(WmRNActivity);
                    List<AccessibilityNodeInfo> editNodeInfoList = AccessibilityHelper.traverseNodeByClassToList(root, "android.widget.EditText");
                    if (editNodeInfoList.size() > 0) {
                        AccessibilityNodeInfo nodeInfo = editNodeInfoList.get(0);
                        Log.e("process-", "当前状态:" + nodeInfo);
                        //AccessibilityNodeInfo citySearchEditNodeInfo = AccessibilityHelper.findNodeInfosById(root, shopAddressEditId);
                        sleepMillsSeconds(500);
                        String citySearh = processStatus.getCurrentPage();
                        Log.e("process-", "当前状态:" + citySearh);
                        AccessibilityHelper.performSetText(nodeInfo, citySearh);
                        removeAll();
                        concurrentPage.put("search", System.currentTimeMillis());
                        sleepSeconds(2);
                        topGestureClick((float) 0.15);
                    } else {
                        Log.e("process-", "当前状态: 无法获取输入框");
                    }
                }
                break;
        }
    }

    private void removeAll() {
        removePage(TakeoutActivity);
        removePage(WMRestaurantActivity);
        removePage(LocateManuallyActivity);
        removePage(SCSuperMarketActivity);
    }

    private void clickNearShop(AccessibilityNodeInfo root, String mClassName, int eventType) {
        sleepMillsSeconds(1000);
        AccessibilityNodeInfo nearShopNode = AccessibilityHelper.findNodeInfosById(root, nearShop);
        try {
            if (nearShopNode != null) {
                int childCount = nearShopNode.getChildCount();
                AccessibilityNodeInfo child = nearShopNode.getChild(0);
                AccessibilityHelper.performClick(child);

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
            Log.e("process-", "搜索无结果");
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
        concurrentPage.put(page, System.currentTimeMillis());
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
            sleepSeconds(2);
            List<AccessibilityNodeInfo> nodeInfosByResId = AccessibilityHelper.findNodeInfosByResId(root, MtAppConst.shopListByNameClick);
            if (nodeInfosByResId == null) {
                Log.e("process-", "--当前列表为空 --");
                // clickShopItem(root, mClassName, eventType);
                topScroll(root, mClassName, eventType);
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
        sleepMillsSeconds(500);
        AccessibilityNodeInfo phoneButtoNodeInfo = AccessibilityHelper.findNodeInfosById(root, MtAppConst.PhoneButton);
        boolean isClickTellButton = AccessibilityHelper.performClick(phoneButtoNodeInfo);
        if (!isClickTellButton) {
            Log.e("process-", "点击电话按钮异常!!!!");
            if (i < 3) {
                i++;
                clickPhoneButton(root);
            } else {
                i = 0;
                performGlobalBackAction();
            }
        } else {
            Log.e("process-", "成功点击电话按钮");
        }
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
            Log.e("process-", "获取ShopStar fail");
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

    private int i = 0;

    private void clickStoreName(AccessibilityNodeInfo root) {
        sleepMillsSeconds(1500);
        boolean isClickShop = AccessibilityHelper.findNodeByTextAndPerformClick(root, "商家");
        if (!isClickShop) {
            i++;
            if (i < 3) {
                Log.e("process-", "点击商家异常,尝试重新点击" + i);
                sleepMillsSeconds(1500);
                clickStoreName(root);
            } else {
                i = 0;
                performGlobalBackAction();
            }
        } else {
            Log.e("process-", "成功点击商家");
        }

    }

    /**
     * 后退
     */
    private boolean performGlobalBackAction() {
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
    public void getResult(Object t, AccessibilityNodeInfo nodeInfo, ProcessStatus processStatus, List<String> allArrdressList) {
        if (allArrdressList != null) {
            allArrdressList.parallelStream().forEach(address -> concurrentHashMap.put(address, true));
            return;
        }
        String[] split = (String[]) t;
        //String searhAddress ="美联广场";
        String searhAddress = split[2];
        String citySearch = split[1];
        if (shopInfo == null) {
            shopInfo = new ShopInfo();
        }
        shopInfo.setShopAreaId(split[0]);
        shopInfo.setShopSearchAddress(searhAddress);
        //判断当前位置是否 与citySearch 相等
        AccessibilityNodeInfo root = accessibilityService.getRoot();
        AccessibilityNodeInfo cityNodeInfo = AccessibilityHelper.findNodeInfosById(root, cityName);
        if (cityNodeInfo != null) {
            String cityName = (String) cityNodeInfo.getText();
            if (!cityName.equals(citySearch)) {
                processStatus.setCurrentPage(citySearch);
                processStatus.setSearchAddress(searhAddress);
                //城市不同
                AccessibilityNodeInfo nodeInfosById = AccessibilityHelper.findNodeInfosById(root, searchCtiyClick);
                AccessibilityHelper.performClick(nodeInfosById);
                return;
            } else {
                AccessibilityHelper.performSetText(nodeInfo, searhAddress);
                clickSearchAddressResult(accessibilityService.getRoot(), nodeInfo);
            }
        }

    }
}
