package com.chameleonvision.common.vision.processes;

import static org.mockito.Mockito.*;

import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.util.TestUtils;
import com.chameleonvision.common.vision.camera.USBCameraSource;
import edu.wpi.cscore.UsbCameraInfo;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.videoio.VideoCapture;

public class VisionSourceManagerTest {
    @BeforeEach
    public void init() {
        TestUtils.loadLibraries();
    }

    final List<UsbCameraInfo> usbCameraInfos =
            List.of(new UsbCameraInfo(0, "", "meme", new String[] {""}, 1, 1));

    final List<CameraConfiguration> camConfig =
            List.of(new CameraConfiguration("meme", "meme", "nickname", "0"));

    @Test
    public void visionSourceTest() {
        var usbSources = new HashMap<UsbCameraInfo, VideoCapture>();

        for (var camInfo : usbCameraInfos) {
            var videoCapture = new VideoCapture(camInfo.dev);
            var spiedCapture = spy(videoCapture);
            when(spiedCapture.isOpened()).thenReturn(true);
            usbSources.put(camInfo, spiedCapture);
        }
        VisionSourceManager visionSourceManager = new VisionSourceManager();
        var i = visionSourceManager.LoadAllSources(camConfig, usbSources);

        var firstSource = (USBCameraSource)i.get(0);

        Assertions.assertFalse(firstSource.isPS3Eye);

    }
}
