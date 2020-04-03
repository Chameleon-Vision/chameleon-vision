package com.chameleonvision.common.vision.frame.provider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.chameleonvision.common.vision.frame.Frame;
import edu.wpi.cscore.CameraServerCvJNI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FileFrameProviderTest {

    static Path imagesPath;

    @BeforeAll
    public static void initPath() {
        imagesPath = new File(Objects.requireNonNull(FileFrameProvider.class.getClassLoader().getResource("testimages")).getFile()).toPath();

        try {
            CameraServerCvJNI.forceLoad();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestImagesExist() {
        assertTrue(imagesPath.endsWith("testimages"));
    }

    @Test
    public void Load2019ImageOnceTest() {
        var goodFilePath = Paths.get(imagesPath + "\\2019\\WPI\\LoadingStraight36in.jpg");

        assertTrue(Files.exists(goodFilePath));

        FileFrameProvider goodFrameProvider = new FileFrameProvider(goodFilePath, 68.5);

        Frame goodFrame = goodFrameProvider.getFrame();

        int goodFrameCols = goodFrame.image.cols();
        int goodFrameRows = goodFrame.image.rows();

        assertEquals(320, goodFrameCols);
        assertEquals(240, goodFrameRows);

        var badFilePath = Paths.get(imagesPath + "bad.jpg"); // this file does not exist

        FileFrameProvider badFrameProvider = null;

        try {
            badFrameProvider = new FileFrameProvider(badFilePath, 68.5);
        } catch (Exception e) {
            // ignored
        }

        assertNull(badFrameProvider);
    }
}
