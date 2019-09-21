package com.dahua;

import com.dahua.bean.CaptureBean;
import com.dahua.common.Res;
import com.dahua.lib.NetSDKLib;
import com.dahua.module.LoginModule;
import com.sun.jna.Pointer;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class SdkService {

    private static Map<String, Object> map = new HashMap<>();

    static {
        map.put("savePic", true);
        map.put("dllPath", "./src/main/resources/libs/win32/");
        map.put("logPath", "./sdklog/");
    }

    public class Config {

        private SdkService sdkService;

        public Config(SdkService sdkService) {
            this.sdkService = sdkService;
        }
        public Config set(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public SdkService build() {
            return sdkService;
        }

    }

    public Config builder() {
        return new Config(this);
    }

    public static String get(String key) {
        return get(key, String.class);
    }

    public static <T> T get(String key, Class<T> tClass) {
        if (null == map.get(key)) {
            throw new IllegalArgumentException(key + " is not found!");
        }
        return tClass.cast(map.get(key));
    }

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

    public boolean start() {
        if (LoginModule.init(disConnect, haveReConnect)) {
            return LoginModule.login(
                    get("ip"),
                    get("port", Integer.class),
                    get("name"),
                    get("password"));
        }
        return false;
    }

    public static void main(String[] args) {
        SdkService service = new SdkService().builder()
                .set("ip", "zhengzhoutn1.wicp.vip")
                .set("port", 20277)
                .set("name", "admin")
                .set("password", "tn123456")
                .set("savePic", true)
                .set("logPath", "C:\\Users\\Administrator\\Desktop\\core\\netsdk\\logs")
                .build();
        if (service.start()) {
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
        } else {
            System.out.println("login failed");
        }

    }
}
