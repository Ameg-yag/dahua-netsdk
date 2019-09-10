package com.dahua.bean;

public class CaptureBean {

    private String plateNumber;

    private String plateColor;

    private String vehicleSize;

    private String photoPath;

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getPlateColor() {
        return plateColor;
    }

    public void setPlateColor(String plateColor) {
        this.plateColor = plateColor;
    }

    public String getVehicleSize() {
        return vehicleSize;
    }

    public void setVehicleSize(String vehicleSize) {
        this.vehicleSize = vehicleSize;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @Override
    public String toString() {
        return "CaptureBean{" +
                "plateNumber='" + plateNumber + '\'' +
                ", plateColor='" + plateColor + '\'' +
                ", vehicleSize='" + vehicleSize + '\'' +
                ", photoPath='" + photoPath + '\'' +
                '}';
    }
}
