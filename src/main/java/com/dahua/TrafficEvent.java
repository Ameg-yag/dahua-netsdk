package com.dahua;

import com.dahua.bean.CaptureBean;
import com.dahua.common.Res;
import com.dahua.common.SavePath;
import com.dahua.lib.NetSDKLib;
import com.dahua.lib.ToolKits;
import com.dahua.module.TrafficEventModule;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class TrafficEvent {

    private Queue<CaptureBean> queue = new ArrayBlockingQueue<>(10);

    private AnalyzerDataCB m_AnalyzerDataCB = new AnalyzerDataCB();

    private BufferedImage snapImage = null;

    private BufferedImage plateImage = null;

    private class TRAFFIC_INFO {
        private String m_EventName;              // 事件名称
        private String m_PlateNumber;          // 车牌号
        private String m_PlateType;               // 车牌类型
        private String m_PlateColor;              // 车牌颜色
        private String m_VehicleColor;              // 车身颜色
        private String m_VehicleType;          // 车身类型
        private String m_VehicleSize;              // 车辆大小
        private String m_FileCount;                  // 文件总数
        private String m_FileIndex;                  // 文件编号
        private String m_GroupID;                  // 组ID
        private String m_IllegalPlace;              // 违法地点
        private String m_LaneNumber;              // 通道号
        private NetSDKLib.NET_TIME_EX m_Utc;      // 事件时间
        private int m_bPicEnble;                  // 车牌对应信息，BOOL类型
        private int m_OffSet;                      // 车牌偏移量
        private int m_FileLength;                 // 文件大小
        private NetSDKLib.DH_RECT m_BoundingBox;  // 包围盒

        @Override
        public String toString() {
            return "TRAFFIC_INFO{" +
                    "m_EventName='" + m_EventName + '\'' +
                    ", m_PlateNumber='" + m_PlateNumber + '\'' +
                    ", m_PlateType='" + m_PlateType + '\'' +
                    ", m_PlateColor='" + m_PlateColor + '\'' +
                    ", m_VehicleColor='" + m_VehicleColor + '\'' +
                    ", m_VehicleType='" + m_VehicleType + '\'' +
                    ", m_VehicleSize='" + m_VehicleSize + '\'' +
                    ", m_FileCount='" + m_FileCount + '\'' +
                    ", m_FileIndex='" + m_FileIndex + '\'' +
                    ", m_GroupID='" + m_GroupID + '\'' +
                    ", m_IllegalPlace='" + m_IllegalPlace + '\'' +
                    ", m_LaneNumber='" + m_LaneNumber + '\'' +
                    ", m_Utc=" + m_Utc +
                    ", m_bPicEnble=" + m_bPicEnble +
                    ", m_OffSet=" + m_OffSet +
                    ", m_FileLength=" + m_FileLength +
                    ", m_BoundingBox=" + m_BoundingBox +
                    '}';
        }
    }

    private final TRAFFIC_INFO trafficInfo = new TRAFFIC_INFO();

    private void setQueue(CaptureBean bean) {
        while (!queue.offer(bean)) {
            queue.offer(bean);
        }
    }

    public Queue<CaptureBean> getQueue() {
        return this.queue;
    }

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

            if (dwAlarmType == NetSDKLib.EVENT_IVS_TRAFFICJUNCTION) {
                CaptureBean bean = new CaptureBean();
                // 获取识别对象 车身对象 事件发生时间 车道号等信息
                GetStuObject(dwAlarmType, pAlarmInfo);

                // 保存图片，获取图片缓存
                if (SdkService.get("savePic", Boolean.class)) {
                    savePlatePic(pBuffer, dwBufSize, trafficInfo, bean);
                }
                bean.setPlateNumber(trafficInfo.m_PlateNumber);
                bean.setPlateColor(trafficInfo.m_PlateColor);
                bean.setVehicleSize(trafficInfo.m_VehicleType + ":" + trafficInfo.m_VehicleSize);
                setQueue(bean);
            }

            return 0;
        }

        // 获取识别对象 车身对象 事件发生时间 车道号等信息
        private void GetStuObject(int dwAlarmType, Pointer pAlarmInfo) {
            if (pAlarmInfo == null) {
                return;
            }

            switch (dwAlarmType) {
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
                    trafficInfo.m_GroupID = String.valueOf(msg.stuFileInfo.nGroupId);
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
                    trafficInfo.m_GroupID = String.valueOf(msg.stuFileInfo.nGroupId);
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

    private void savePlatePic(Pointer pBuffer, int dwBufferSize, TRAFFIC_INFO trafficInfo, CaptureBean bean) {

        String bigPicture; // 大图
        String platePicture; // 车牌图

        if (pBuffer == null || dwBufferSize <= 0) {
            return;
        }

        // 保存大图
        byte[] buffer = pBuffer.getByteArray(0, dwBufferSize);
        ByteArrayInputStream byteArrInput = new ByteArrayInputStream(buffer);

        bigPicture = SavePath.getSavePath().getSaveTrafficImagePath() + "Big_" + trafficInfo.m_Utc.toStringTitle() + "_" +
                trafficInfo.m_FileCount + "-" + trafficInfo.m_FileIndex + "-" + trafficInfo.m_GroupID + ".jpg";

        try {
            snapImage = ImageIO.read(byteArrInput);
            if (snapImage == null) {
                return;
            }
            File file = new File(bigPicture);
            bean.setPhotoPath(file.getAbsolutePath());
            ImageIO.write(snapImage, "jpg", file);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        if (bigPicture == null || bigPicture.equals("")) {
            return;
        }

        if (trafficInfo.m_bPicEnble == 1) {
            //根据pBuffer中数据偏移保存小图图片文件
            if (trafficInfo.m_FileLength > 0) {
                platePicture = SavePath.getSavePath().getSaveTrafficImagePath() + "plate_" + trafficInfo.m_Utc.toStringTitle() + "_" +
                        trafficInfo.m_FileCount + "-" + trafficInfo.m_FileIndex + "-" + trafficInfo.m_GroupID + ".jpg";

                int size = 0;
                if (dwBufferSize <= trafficInfo.m_OffSet) {
                    return;
                }

                if (trafficInfo.m_FileLength <= dwBufferSize - trafficInfo.m_OffSet) {
                    size = trafficInfo.m_FileLength;
                } else {
                    size = dwBufferSize - trafficInfo.m_OffSet;
                }
                byte[] bufPlate = pBuffer.getByteArray(trafficInfo.m_OffSet, size);
                ByteArrayInputStream byteArrInputPlate = new ByteArrayInputStream(bufPlate);
                try {
                    plateImage = ImageIO.read(byteArrInputPlate);
                    if (plateImage == null) {
                        return;
                    }
                    ImageIO.write(plateImage, "jpg", new File(platePicture));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (trafficInfo.m_BoundingBox == null) {
                return;
            }
            //根据大图中的坐标偏移计算显示车牌小图

            NetSDKLib.DH_RECT dhRect = trafficInfo.m_BoundingBox;
            //1.BoundingBox的值是在8192*8192坐标系下的值，必须转化为图片中的坐标
            //2.OSD在图片中占了64行,如果没有OSD，下面的关于OSD的处理需要去掉(把OSD_HEIGHT置为0)
            final int OSD_HEIGHT = 0;

            long nWidth = snapImage.getWidth(null);
            long nHeight = snapImage.getHeight(null);

            nHeight = nHeight - OSD_HEIGHT;
            if ((nWidth <= 0) || (nHeight <= 0)) {
                return;
            }

            NetSDKLib.DH_RECT dstRect = new NetSDKLib.DH_RECT();

            dstRect.left.setValue((long) ((double) (nWidth * dhRect.left.longValue()) / 8192.0));
            dstRect.right.setValue((long) ((double) (nWidth * dhRect.right.longValue()) / 8192.0));
            dstRect.bottom.setValue((long) ((double) (nHeight * dhRect.bottom.longValue()) / 8192.0));
            dstRect.top.setValue((long) ((double) (nHeight * dhRect.top.longValue()) / 8192.0));

            int x = dstRect.left.intValue();
            int y = dstRect.top.intValue() + OSD_HEIGHT;
            int w = dstRect.right.intValue() - dstRect.left.intValue();
            int h = dstRect.bottom.intValue() - dstRect.top.intValue();

            if (x == 0 || y == 0 || w <= 0 || h <= 0) {
                return;
            }

            try {
                plateImage = snapImage.getSubimage(x, y, w, h);
                platePicture = SavePath.getSavePath().getSaveTrafficImagePath() + "plate_" + trafficInfo.m_Utc.toStringTitle() + "_" +
                        trafficInfo.m_FileCount + "-" + trafficInfo.m_FileIndex + "-" + trafficInfo.m_GroupID + ".jpg";
                if (plateImage == null) {
                    return;
                }
                ImageIO.write(plateImage, "jpg", new File(platePicture));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startEvent() {
        Native.setCallbackThreadInitializer(m_AnalyzerDataCB,
                new CallbackThreadInitializer(false, false, "traffic callback thread"));
        TrafficEventModule.attachIVSEvent(0, m_AnalyzerDataCB);
    }
}
