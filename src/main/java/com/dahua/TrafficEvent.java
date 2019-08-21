package com.dahua;

import com.dahua.common.Res;
import com.dahua.lib.NetSDKLib;
import com.dahua.lib.ToolKits;
import com.dahua.module.TrafficEventModule;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import javax.swing.*;
import java.io.UnsupportedEncodingException;

public class TrafficEvent {

    private AnalyzerDataCB m_AnalyzerDataCB = new AnalyzerDataCB();

    private class TRAFFIC_INFO {
        private String m_EventName;         	  // 事件名称
        private String m_PlateNumber;       	  // 车牌号
        private String m_PlateType;               // 车牌类型
        private String m_PlateColor;      	  	  // 车牌颜色
        private String m_VehicleColor;    	  	  // 车身颜色
        private String m_VehicleType;       	  // 车身类型
        private String m_VehicleSize;     	  	  // 车辆大小
        private String m_FileCount;				  // 文件总数
        private String m_FileIndex;				  // 文件编号
        private String m_GroupID;				  // 组ID
        private String m_IllegalPlace;			  // 违法地点
        private String m_LaneNumber;              // 通道号
        private NetSDKLib.NET_TIME_EX m_Utc;      // 事件时间
        private int m_bPicEnble;       	  		  // 车牌对应信息，BOOL类型
        private int m_OffSet;          	  		  // 车牌偏移量
        private int m_FileLength;                 // 文件大小
        private NetSDKLib.DH_RECT m_BoundingBox;  // 包围盒
    }

    private final TRAFFIC_INFO trafficInfo = new TRAFFIC_INFO();

    /*
     * 智能报警事件回调
     */
    private class AnalyzerDataCB implements NetSDKLib.fAnalyzerDataCallBack {
        public int invoke(NetSDKLib.LLong lAnalyzerHandle, int dwAlarmType,
                          Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize,
                          Pointer dwUser, int nSequence, Pointer reserved) {
            if (lAnalyzerHandle.longValue() == 0) {
                return -1;
            }

            if(dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFICJUNCTION
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_RUNREDLIGHT
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_OVERLINE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_RETROGRADE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_TURNLEFT
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_TURNRIGHT
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_UTURN
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_OVERSPEED
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_UNDERSPEED
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_PARKING
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_WRONGROUTE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_CROSSLANE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_OVERYELLOWLINE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_YELLOWPLATEINLANE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_PEDESTRAINPRIORITY
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_MANUALSNAP
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_VEHICLEINROUTE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_VEHICLEINBUSROUTE
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_BACKING
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_PARKINGSPACEPARKING
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_PARKINGSPACENOPARKING
                    || dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFIC_WITHOUT_SAFEBELT) {

                // 获取识别对象 车身对象 事件发生时间 车道号等信息
                GetStuObject(dwAlarmType, pAlarmInfo);

                // 保存图片，获取图片缓存
//                savePlatePic(pBuffer, dwBufSize, trafficInfo);
                System.out.println(trafficInfo.m_PlateNumber);

            }

            return 0;
        }

        // 获取识别对象 车身对象 事件发生时间 车道号等信息
        private void GetStuObject(int dwAlarmType, Pointer pAlarmInfo)  {
            if(pAlarmInfo == null) {
                return;
            }

            switch(dwAlarmType) {
                case NetSDKLib.EVENT_IVS_TRAFFICJUNCTION: ///< 交通卡口事件
                {
                    NetSDKLib.DEV_EVENT_TRAFFICJUNCTION_INFO msg = new NetSDKLib.DEV_EVENT_TRAFFICJUNCTION_INFO();
                    ToolKits.GetPointerData(pAlarmInfo, msg);

                    trafficInfo.m_EventName = Res.string().getEventName(NetSDKLib.EVENT_IVS_TRAFFICJUNCTION);
                    try {
                        trafficInfo.m_PlateNumber = new String(msg.stuObject.szText, "GBK").trim();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    trafficInfo.m_PlateType = new String(msg.stTrafficCar.szPlateType).trim();
                    trafficInfo.m_FileCount = String.valueOf(msg.stuFileInfo.bCount);
                    trafficInfo.m_FileIndex = String.valueOf(msg.stuFileInfo.bIndex);
                    trafficInfo.m_GroupID =  String.valueOf(msg.stuFileInfo.nGroupId);
                    trafficInfo.m_IllegalPlace = ToolKits.GetPointerDataToByteArr(msg.stTrafficCar.szDeviceAddress);
                    trafficInfo.m_LaneNumber = String.valueOf(msg.nLane);
                    trafficInfo.m_PlateColor = new String(msg.stTrafficCar.szPlateColor).trim();
                    trafficInfo.m_VehicleColor = new String(msg.stTrafficCar.szVehicleColor).trim();
                    trafficInfo.m_VehicleType = new String(msg.stuVehicle.szObjectSubType).trim();
                    trafficInfo.m_VehicleSize = Res.string().getTrafficSize(msg.stTrafficCar.nVehicleSize);
                    trafficInfo.m_Utc = msg.UTC;
                    trafficInfo.m_bPicEnble = msg.stuObject.bPicEnble;
                    trafficInfo.m_OffSet = msg.stuObject.stPicInfo.dwOffSet;
                    trafficInfo.m_FileLength = msg.stuObject.stPicInfo.dwFileLenth;
                    trafficInfo.m_BoundingBox = msg.stuObject.BoundingBox;
                    break;
                }
                case NetSDKLib.EVENT_IVS_TRAFFIC_MANUALSNAP: ///< 交通手动抓拍事件
                {
                    JOptionPane.showMessageDialog(null, Res.string().getManualCaptureSucceed(), Res.string().getPromptMessage(), JOptionPane.INFORMATION_MESSAGE);
                    NetSDKLib.DEV_EVENT_TRAFFIC_MANUALSNAP_INFO msg = new NetSDKLib.DEV_EVENT_TRAFFIC_MANUALSNAP_INFO();
                    ToolKits.GetPointerData(pAlarmInfo, msg);

                    trafficInfo.m_EventName = Res.string().getEventName(NetSDKLib.EVENT_IVS_TRAFFIC_MANUALSNAP);
                    try {
                        trafficInfo.m_PlateNumber = new String(msg.stuObject.szText, "GBK").trim();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    trafficInfo.m_PlateType = new String(msg.stTrafficCar.szPlateType).trim();
                    trafficInfo.m_FileCount = String.valueOf(msg.stuFileInfo.bCount);
                    trafficInfo.m_FileIndex = String.valueOf(msg.stuFileInfo.bIndex);
                    trafficInfo.m_GroupID =  String.valueOf(msg.stuFileInfo.nGroupId);
                    trafficInfo.m_IllegalPlace = ToolKits.GetPointerDataToByteArr(msg.stTrafficCar.szDeviceAddress);
                    trafficInfo.m_LaneNumber = String.valueOf(msg.nLane);
                    trafficInfo.m_PlateColor = new String(msg.stTrafficCar.szPlateColor).trim();
                    trafficInfo.m_VehicleColor = new String(msg.stTrafficCar.szVehicleColor).trim();
                    trafficInfo.m_VehicleType = new String(msg.stuVehicle.szObjectSubType).trim();
                    trafficInfo.m_VehicleSize = Res.string().getTrafficSize(msg.stTrafficCar.nVehicleSize);
                    trafficInfo.m_Utc = msg.UTC;
                    trafficInfo.m_bPicEnble = msg.stuObject.bPicEnble;
                    trafficInfo.m_OffSet = msg.stuObject.stPicInfo.dwOffSet;
                    trafficInfo.m_FileLength = msg.stuObject.stPicInfo.dwFileLenth;
                    trafficInfo.m_BoundingBox = msg.stuObject.BoundingBox;

                    break;
                }
                default:
                    break;
            }
        }
    }

    public void startEvent() {
        Native.setCallbackThreadInitializer(m_AnalyzerDataCB,
                new CallbackThreadInitializer(false, false, "traffic callback thread"));
        TrafficEventModule.attachIVSEvent(0, m_AnalyzerDataCB);
    }
}
