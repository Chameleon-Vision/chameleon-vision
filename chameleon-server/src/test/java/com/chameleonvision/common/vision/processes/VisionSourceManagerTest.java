package com.chameleonvision.common.vision.processes;


import com.chameleonvision.common.configuration.CameraConfiguration;
import com.chameleonvision.common.util.TestUtils;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.UsbCameraInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.videoio.VideoCapture;


import java.util.List;

import static org.mockito.Mockito.*;


public class VisionSourceManagerTest {
    @BeforeEach
    public void init() {
        TestUtils.loadLibraries();
    }

    @Test
    public void visionSourceTest() {
        var l = List.of(
                new UsbCameraInfo(0, "", "meme", new String[]{""}, 1, 1)
        );

        var camConfig = List.of(new CameraConfiguration("meme", "meme", "nickname", "0"));
        VideoCapture c = mock(VideoCapture.class);
        when(c.isOpened()).thenReturn(true);

        VisionSourceManager visionSourceManager = new VisionSourceManager();
        var i = visionSourceManager.LoadAllSources(camConfig,l);
        System.out.println("yay");

    }
}
