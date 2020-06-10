package com.chameleonvision.common.vision.processes;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.vision.camera.CameraType;
import com.chameleonvision.common.vision.camera.USBCameraSource;
import com.chameleonvision.common.vision.frame.provider.NetworkFrameProvider;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.UsbCameraInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.opencv.videoio.VideoCapture;

public class VisionSourceManager {
    public List<VisionSource> LoadAllSources(List<CameraConfiguration> camerasConfiguration) {
        HashMap<UsbCameraInfo, VideoCapture> usbSources = new HashMap<>();

        for (var usbCam : UsbCamera.enumerateUsbCameras()) {
            usbSources.put(usbCam, new VideoCapture(usbCam.dev));
        }

        return LoadAllSources(camerasConfiguration, usbSources);
    }

    public List<VisionSource> LoadAllSources(
            List<CameraConfiguration> camerasConfiguration,
            HashMap<UsbCameraInfo, VideoCapture> usbCameraInfos) {
        List<UsbCameraInfo> allActiveUsbCameras = verifyUsbCameras(usbCameraInfos);
        var UsbCamerasConfiguration =
                camerasConfiguration.stream()
                        .filter(configuration -> configuration.cameraType == CameraType.UsbCamera)
                        .collect(Collectors.toList());
        // var HttpCamerasConfiguration = camerasConfiguration.stream().filter(configuration ->
        // configuration.cameraType == CameraType.HttpCamera);
        var matchedCameras = matchUSBCameras(allActiveUsbCameras, UsbCamerasConfiguration);
        return loadUSBCameraSources(matchedCameras);
    }

    private List<UsbCameraInfo> verifyUsbCameras(HashMap<UsbCameraInfo, VideoCapture> captures) {
        List<UsbCameraInfo> activeCameras = new ArrayList<>();
        for (var cap : captures.entrySet()) {
            if (cap.getValue().isOpened()) {
                cap.getValue().release();
                activeCameras.add(cap.getKey());
            }
        }
        return activeCameras;
    }

    private NetworkFrameProvider loadHTTPCamera(CameraConfiguration config) {
        throw new NotImplementedException("");
    }

    private List<CameraConfiguration> matchUSBCameras(
            List<UsbCameraInfo> infos, List<CameraConfiguration> cameraConfigurationList) {
        List<CameraConfiguration> cameraConfigurations = new ArrayList<>();

        for (CameraConfiguration config : cameraConfigurationList) {
            UsbCameraInfo cameraInfo;
            if (!StringUtils.isNumeric(config.path)) {
                // matching by path
                cameraInfo =
                        infos.stream()
                                .filter(usbCameraInfo -> usbCameraInfo.path.equals(config.path))
                                .findFirst()
                                .orElse(null);
            } else {
                // match by index
                cameraInfo =
                        infos.stream()
                                .filter(usbCameraInfo -> usbCameraInfo.dev == Integer.parseInt(config.path))
                                .findFirst()
                                .orElse(null);
            }

            if (cameraInfo != null) {
                infos.remove(cameraInfo);
                cameraConfigurations.add(config);
            }

            for (UsbCameraInfo info : infos) {
                // create new camera config for all new cameras
                String name = info.name.replaceAll("[^\\x00-\\x7F]", "");
                String uniqueName = name;
                int suffix = 0;

                while (containsName(cameraConfigurations, uniqueName)) {
                    suffix++;
                    uniqueName = String.format("%s (%d)", uniqueName, suffix);
                }

                CameraConfiguration configuration =
                        new CameraConfiguration(name, uniqueName, uniqueName, ((Integer) info.dev).toString());
                cameraConfigurations.add(configuration);
            }
        }
        return cameraConfigurations;
    }

    private List<VisionSource> loadUSBCameraSources(List<CameraConfiguration> configurations) {
        List<VisionSource> usbCameraSources = new ArrayList<>();
        configurations.forEach(
                configuration -> usbCameraSources.add(new USBCameraSource(configuration)));
        return usbCameraSources;
    }

    private boolean containsName(final List<CameraConfiguration> list, final String name) {
        return list.stream().anyMatch(configuration -> configuration.uniqueName.equals(name));
    }
}
