package com.example.set12ma.ui.main;

import android.hardware.usb.UsbManager;
import android.os.Parcel;
import android.os.Parcelable;
import com.moxa.mxuportapi.MxUPort;

import java.util.List;

public class SpaceStatus implements Parcelable {

    private int statusCommunication = 0;

    public int getStatusCommunication() {
        return statusCommunication;
    }

    public void setStatusCommunication(int statusCommunication) {
        this.statusCommunication = statusCommunication;
    }

    private Communication communication;

    public Communication getCommunication() {
        return communication;
    }

    public void setCommunication(Communication communication) {
        this.communication = communication;
    }

    private UsbManager mgr;

    public UsbManager getMgr() {
        return mgr;
    }

    public void setMgr(UsbManager mgr) {
        this.mgr = mgr;
    }

    private int addressOfDevice = 0;
    public int getAddressOfDevice() {
        return addressOfDevice;
    }
    public void setAddressOfDevice(int addressOfDevice) {
        this.addressOfDevice = addressOfDevice;
    }

    private boolean readyFlagToExchangeData = false;
    public boolean isReadyFlagToExchangeData() {
        return readyFlagToExchangeData;
    }
    public void setReadyFlagToExchangeData(boolean readyFlagToExchangeData) { this.readyFlagToExchangeData = readyFlagToExchangeData; }

    private boolean readyFlagRecordingInitialValues;
    public boolean isReadyFlagRecordingInitialValues() {
        return readyFlagRecordingInitialValues;
    }
    public void setReadyFlagRecordingInitialValues(boolean readyFlagRecordingInitialValues) { this.readyFlagRecordingInitialValues = readyFlagRecordingInitialValues; }

    // переходит в true если нажали кнопку "Загрузить в память", смогли прочитать файл и начался процесс загрузки в память TMS
    // переходит в false если получили ответ от процессора об успешном завершении ВНИМАНИЕ!! загруки ПО в переферию.
    private boolean readyFlagToLoadSoftware = false;
    public boolean isReadyFlagToLoadSoftware() {
        return readyFlagToLoadSoftware;
    }
    public void setReadyFlagToLoadSoftware(boolean readyFlagToLoadSoftware) { this.readyFlagToLoadSoftware = readyFlagToLoadSoftware; }

    // переходит в true если нажали кнопку "Обновить ПО"
    // переходит в false если получили ответ от процессора об успешном завершении ВНИМАНИЕ!! загруки ПО в переферию.
    private boolean readyFlagToUpdateSoftware = false;
    public boolean isReadyFlagToUpdateSoftware() {
        return readyFlagToUpdateSoftware;
    }
    public void setReadyFlagToUpdateSoftware(boolean readyFlagToUpdateSoftware) {
        this.readyFlagToUpdateSoftware = readyFlagToUpdateSoftware;
    }

    private boolean statusProcessOfLoadingSoftware = false;
    public boolean isStatusProcessOfLoadingSoftware() { return statusProcessOfLoadingSoftware; }
    public void setStatusProcessOfLoadingSoftware(boolean statusProcessOfLoadingSoftware) { this.statusProcessOfLoadingSoftware = statusProcessOfLoadingSoftware; }

    private boolean statusProcessOfUpdatingSoftware = false;
    public boolean isStatusProcessOfUpdatingSoftware() { return statusProcessOfUpdatingSoftware; }
    public void setStatusProcessOfUpdatingSoftware(boolean statusProcessOfUpdatingSoftware) { this.statusProcessOfUpdatingSoftware = statusProcessOfUpdatingSoftware; }

    private boolean readyFlagToFinishOfLoadingSoftware = false;
    public boolean isReadyFlagToFinishOfLoadingSoftware() { return readyFlagToFinishOfLoadingSoftware; }
    public void setReadyFlagToFinishOfLoadingSoftware(boolean readyFlagToFinishOfLoadingSoftware) { this.readyFlagToFinishOfLoadingSoftware = readyFlagToFinishOfLoadingSoftware; }

    private boolean readyFlagToFinishOfUpdatingSoftware = false;
    public boolean isReadyFlagToFinishOfUpdatingSoftware() { return readyFlagToFinishOfUpdatingSoftware; }
    public void setReadyFlagToFinishOfUpdatingSoftware(boolean readyFlagToFinishOfUpdatingSoftware) { this.readyFlagToFinishOfUpdatingSoftware = readyFlagToFinishOfUpdatingSoftware; }

    public boolean isReadyFlagToDownloadLog() {
        return readyFlagToDownloadLog;
    }

    public void setReadyFlagToDownloadLog(boolean readyFlagToDownloadLog) {
        this.readyFlagToDownloadLog = readyFlagToDownloadLog;
    }

    private boolean readyFinishFlagToDownloadLog = false;

    public boolean isReadyFinishFlagToDownloadLog() {
        return readyFinishFlagToDownloadLog;
    }

    public void setReadyFinishFlagToDownloadLog(boolean readyFinishFlagToDownloadLog) {
        this.readyFinishFlagToDownloadLog = readyFinishFlagToDownloadLog;
    }

    private boolean readyFlagToDownloadLog = false;

    public boolean isReadyFlagToFinishOfDownloadingLogs() {
        return readyFlagToFinishOfDownloadingLogs;
    }

    public void setReadyFlagToFinishOfDownloadingLogs(boolean readyFlagToFinishOfDownloadingLogs) {
        this.readyFlagToFinishOfDownloadingLogs = readyFlagToFinishOfDownloadingLogs;
    }

    private boolean readyFlagToFinishOfDownloadingLogs = false;

    private int lastNumberError = 0;

    public int getLastNumberError() {
        return lastNumberError;
    }

    public void setLastNumberError(int lastNumberError) {
        this.lastNumberError = lastNumberError;
    }

    private String device = "";
    public String getDevice() {
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
    }

    private int progressBarDownload = 0;

    public int getProgressBarDownload() {
        return progressBarDownload;
    }

    public void setProgressBarDownload(int progressBarDownload) {
        this.progressBarDownload = progressBarDownload;
    }

    public SpaceStatus() {
    }

    public SpaceStatus(Parcel in) {
    }

    public static final Creator<SpaceStatus> CREATOR = new Creator<SpaceStatus>() {
        @Override
        public SpaceStatus createFromParcel(Parcel in) {
            return new SpaceStatus(in);
        }

        @Override
        public SpaceStatus[] newArray(int size) {
            return new SpaceStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
