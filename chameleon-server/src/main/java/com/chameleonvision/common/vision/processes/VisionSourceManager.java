package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.vision.camera.CameraType;
import com.chameleonvision.common.vision.frame.FrameProvider;
import com.chameleonvision.common.vision.frame.provider.NetworkFrameProvider;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.UsbCameraInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.opencv.videoio.VideoCapture;

public class VisionSourceManager {
    public HashMap<String, FrameProvider> LoadAllSources(
            List<CameraConfiguration> camerasConfiguration) {
        List<UsbCameraInfo> allActiveUsbCameras = loadUsbCameras();

        var UsbCamerasConfiguration =
                camerasConfiguration.stream()
                        .filter(configuration -> configuration.cameraType == CameraType.UsbCamera);
        // var HttpCamerasConfiguration = camerasConfiguration.stream().filter(configuration ->
        // configuration.cameraType == CameraType.HttpCamera);
        return new HashMap<>();
    }

    private List<UsbCameraInfo> loadUsbCameras() {
        List<UsbCameraInfo> activeCameras = new ArrayList<>();
        for (UsbCameraInfo info : UsbCamera.enumerateUsbCameras()) {
            VideoCapture cap = new VideoCapture(info.dev);
            if (cap.isOpened()) {
                cap.release();
                activeCameras.add(info);
            }
        }
        return activeCameras;
    }

    private NetworkFrameProvider loadHTTPCamera(CameraConfiguration config) {
        throw new NotImplementedException("");
    }

    private HashMap<UsbCameraInfo, CameraConfiguration> matchUsbCameras(
            List<UsbCameraInfo> activeCameras, List<CameraConfiguration> cameraConfigurations) {
        HashMap<UsbCameraInfo, CameraConfiguration> matchedCameras = new HashMap<>();
        // start by matching cameras by path
        for (CameraConfiguration config : cameraConfigurations) {
            UsbCameraInfo cameraInfo;
            if (!config.path.equals("")) {
                cameraInfo = activeCameras.stream()
                        .filter(usbCameraInfo -> usbCameraInfo.path.equals(config.path))
                        .findFirst()
                        .orElse(null);
            } else {
                // match by index
                cameraInfo = activeCameras.stream()
                        .filter(usbCameraInfo -> usbCameraInfo.dev == config.index)
                        .findFirst()
                        .orElse(null);
            }
            if (cameraInfo != null) {
                activeCameras.remove(cameraInfo);
                cameraConfigurations.remove(config);
                matchedCameras.put(cameraInfo, config);
            }
            // if any new cameras exist add them with a new configuration
            if (!activeCameras.isEmpty()) {
                for (UsbCameraInfo info : activeCameras) {
                    matchedCameras.put(info, new CameraConfiguration());
                }
            }
        }

        return matchedCameras;
    }
}
