package com.xiaoxian.wxredpack;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Path;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xiaoxian.wxredpack.util.ScreenUtil;

import java.util.List;

public class WXRedPackService extends AccessibilityService  {
    private int serviceState = -1;
    int FWZT=-1;//服务状态 小于等于0为关闭 大于0开启
    int FTZLZT=-1;//非通知栏状态 小于等于0为关闭 大于0为开启
    private static final String ACTIVITY_DIALOG_LUCKYMONEY = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";

    private int screenWidth = ScreenUtil.SCREEN_WIDTH;
    private int screenHeight = ScreenUtil.SCREEN_HEIGHT;
    /**
     * 红包的开字在屏幕中的比例
     */
    private static final float POINT_OPEN_Y_SCAL = 0.65F;
    /**
     * 等待弹窗弹出时间
     */
    public static int waitWindowTime=150;
    /**
     * 当前机型是否需要配置时间，是否能获取到弹窗
     */
    public static int needSetTime=-1;


    @Override
    public void onCreate(){
        super.onCreate();
    }

    private void showLog(int i,String string){
        System.out.println("消息"+i+string);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            //当系统检测到与Accessibility服务指定的事件过滤参数
            // 匹配的AccessibilityEvent时调用
            if ( event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED ) {
                //通知栏事件
                showLog(1,"通知栏");
                triggerNoticeBar(event);
            }else{
                if(serviceState<=0){
                    triggerWinChange(event);
                }
                serviceState=-1;

            }
        }catch (Exception e){

        }


    }

    @Override
    public void onInterrupt() {
        //服务中断，如授权关闭或者将服务杀死
    }

    /**
     *Service被启动的时候会调用这个API
     */
    @Override
    protected void onServiceConnected() {

        //设置关心的事件类型
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;//两个相同事件的超时时间间隔
        setServiceInfo(info);
    }

    //窗口改变
    private  void triggerWinChange(AccessibilityEvent event) throws Exception{

        showLog(100,"窗口改变");

        AccessibilityNodeInfo nodeInfo =  getRootInActiveWindow();
        List<AccessibilityNodeInfo> chatListNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nk");//聊天列表
        List<AccessibilityNodeInfo> titleNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ipt");//微信页标题名字

        List<AccessibilityNodeInfo> redPackNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/f42");//红包页面

        //获取开按钮
        List<AccessibilityNodeInfo> kaiNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/f4f");
        //获取 手慢了 提示语句的控件
        List<AccessibilityNodeInfo> slowNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cye");
        //获取关闭按钮
        List<AccessibilityNodeInfo> closeNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cv0");

        showLog(100,"窗口改变,获取控件完毕,chatListNodes:"+chatListNodes.isEmpty()+",titleNodes:"+titleNodes.isEmpty()+",redPackNodes:"+redPackNodes.isEmpty()+",kaiNodes:"+kaiNodes.isEmpty()+",slowNodes:"+slowNodes.isEmpty());

        //在聊天列表页
        if(!chatListNodes.isEmpty()){
            List<AccessibilityNodeInfo> lastMsgNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/e7t");//最后一条消息
            if(lastMsgNodes!=null){
                for ( AccessibilityNodeInfo lastMsgNode : lastMsgNodes ) {
                    if ( lastMsgNode.getText().toString().contains("[微信红包]") ) {
                        //还要判断是否有未读消息
                        AccessibilityNodeInfo parent = lastMsgNode.getParent();
                        if ( parent != null ) {
                            List<AccessibilityNodeInfo> unReadNodes =  parent.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/iot");//头像上未读消息的标志
                            if ( !unReadNodes.isEmpty() ) {
                                CharSequence text = unReadNodes.get(0).getText();
                                if ( text != null ) {
                                    if ( Integer.parseInt(text.toString()) != 0 ) {
                                        //此时才能跳转
                                        try {
                                            //System.out.println("消息" + parent.toString());
                                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击事件
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //在私聊页面
        else if (!titleNodes.isEmpty()) {
            showLog(3,"在私聊页面");
                List<AccessibilityNodeInfo> redPackList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/auf");//红包消息id  u5
            if ( redPackList == null )
                return;
            if ( redPackList.isEmpty() ){

            }else {//有 但是要检查是不是红包
                showLog(4,"但是要检查是不是红包"+redPackList.size() );
                for (int i = redPackList.size() - 1; i >= 0; i--) {
                    AccessibilityNodeInfo redPack = redPackList.get(i);
                    if (redPack.toString() != null) {
                        List<AccessibilityNodeInfo> listNodes = redPack.findAccessibilityNodeInfosByText("微信红包");
                        if (!listNodes.isEmpty()) {
                            //是红包消息 再判断是否已被领取
                            showLog(5,"再判断是否已被领取");
                            List<AccessibilityNodeInfo> lqListNodes = redPack.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/tt");//com.tencent.mm:id/auf
                            if (lqListNodes.isEmpty()) {//红包还没被领取
                                redPack.performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击事件

                                //等待红包弹窗完成，直接使用模拟点击比较快，根据手机性能等待响应的时长
                                SystemClock.sleep(100);
                                for (int x = 0; x < 20; x++) {
                                    SystemClock.sleep(10);
                                    //计算了一下这个開字在屏幕中的位置，按照屏幕比例计算
                                    clickOnScreen(screenWidth / 2, screenHeight * POINT_OPEN_Y_SCAL, 1, null);
                                }
                            }
                        }
                    }
                }
            }

        }
        //在准备开红包页面
        else if(!redPackNodes.isEmpty()){
            showLog(6,"准备自动开红包");
            kaiNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/f4f");
            for(AccessibilityNodeInfo kaiNode:kaiNodes){
                showLog(7,"开红包");
                kaiNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击事件
            }
        }
        else if(!slowNodes.isEmpty()){
            for (AccessibilityNodeInfo slowNode:slowNodes){
                slowNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);//点击事件
            }
        }

    }

    //触发通知栏
    private void triggerNoticeBar(AccessibilityEvent event){
        List<CharSequence> texts = event.getText();
        if(!texts.isEmpty()){
            for(CharSequence text : texts){
                String content = text.toString();
                //如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
                if(content.contains("微信红包")) {
                    if(event.getParcelableData() == null || !(event.getParcelableData().toString().isEmpty())) {

                    }
                    Notification notification = (Notification)event.getParcelableData();
                    PendingIntent pendingIntent =notification.contentIntent;
                    try{
                        pendingIntent.send();
                    }catch
                    (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 模拟触摸事件
     *
     * @param x
     * @param y
     * @param duration
     * @param callback
     */
    protected void clickOnScreen(float x, float y, int duration, AccessibilityService.GestureResultCallback callback) {
        Path path = new Path();
        path.moveTo(x, y);
        gestureOnScreen(path, 0, duration, callback);
    }

    /**
     * 模拟触摸
     *
     * @param path
     * @param startTime
     * @param duration
     * @param callback
     */
    protected void gestureOnScreen(Path path, long startTime, long duration,
                                   AccessibilityService.GestureResultCallback callback) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            GestureDescription.Builder builde = new GestureDescription.Builder();
            builde.addStroke(new GestureDescription.StrokeDescription(path, startTime, duration));
            GestureDescription gestureDescription = builde.build();
            dispatchGesture(gestureDescription, callback, null);
        }
        /*GestureDescription.Builder builde = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            builde = new GestureDescription.Builder();
        }*/

    }
}
