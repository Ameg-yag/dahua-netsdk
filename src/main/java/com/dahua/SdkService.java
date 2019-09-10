package com.dahua;

import com.dahua.bean.CaptureBean;
import com.dahua.common.Res;
import com.dahua.lib.NetSDKLib;
import com.dahua.module.LoginModule;
import com.sun.jna.Pointer;

import javax.swing.*;
import java.util.Queue;

public class SdkService {

    // 设备断线通知回调
    private static DisConnect disConnect = new DisConnect();

    // 网络连接恢复
    private static HaveReConnect haveReConnect = new HaveReConnect();

    // 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
    private static class DisConnect implements NetSDKLib.fDisConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
            // 断线提示
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.out.println(Res.string().getITSEvent() + " : " + Res.string().getDisConnectReconnecting());
                }
            });
        }
    }

    // 网络连接恢复，设备重连成功回调
    // 通过 CLIENT_SetAutoReconnect 设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
    private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);

            // 重连提示
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.out.println(Res.string().getITSEvent() + " : " + Res.string().getOnline());
                }
            });
        }
    }

    private void login(String ip, int port, String name, String pwd) {
        LoginModule.login(ip, port, name, pwd);
    }

    public void start(String ip, int port, String name, String pwd) {
        LoginModule.init(disConnect, haveReConnect);
        login(ip, port, name, pwd);
    }

    public static void main(String[] args) throws InterruptedException {
        SdkService service = new SdkService();
        service.start("zhengzhoutn1.wicp.vip", 20277, "admin", "tn123456");
        TrafficEvent event = new TrafficEvent();
        event.startEvent();
        Thread r = new Thread() {
            @Override
            public void run() {
                while (true) {
                    Queue<CaptureBean> queue = event.getQueue();
                    if (queue.size() > 0) {
                        System.out.println(queue.poll());
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        r.start();
    }
}
