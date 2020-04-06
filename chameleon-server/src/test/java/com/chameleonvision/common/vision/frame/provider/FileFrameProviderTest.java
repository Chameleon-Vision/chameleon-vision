package com.chameleonvision.common.vision.frame.provider;

import static org.junit.jupiter.api.Assertions.*;

import com.chameleonvision.common.vision.TestUtils;
import com.chameleonvision.common.vision.frame.Frame;
import edu.wpi.cscore.CameraServerCvJNI;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FileFrameProviderTest {

    static Path imagesPath;

    @BeforeAll
    public static void initPath() {
        var folder =
                Objects.requireNonNull(FileFrameProvider.class.getClassLoader().getResource("testimages"));
        var path = folder.getFile();
        imagesPath = new File(path).toPath();

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

        // 2019 Images are at 320x240
        assertEquals(320, goodFrameCols);
        assertEquals(240, goodFrameRows);

        // TODO: find a way to skip this if a flag isn't set
        TestUtils.showImage(goodFrame.image, "2019");

        var badFilePath = Paths.get(imagesPath + "bad.jpg"); // this file does not exist

        FileFrameProvider badFrameProvider = null;

        try {
            badFrameProvider = new FileFrameProvider(badFilePath, 68.5);
        } catch (Exception e) {
            // ignored
        }

        assertNull(badFrameProvider);
    }

    @Test
    public void Load2020ImageOnceTest() {
        var goodFilePath = Paths.get(imagesPath + "\\2020\\WPI\\BlueGoal-060in-Center.jpg");

        assertTrue(Files.exists(goodFilePath));

        FileFrameProvider goodFrameProvider = new FileFrameProvider(goodFilePath, 68.5);

        Frame goodFrame = goodFrameProvider.getFrame();

        int goodFrameCols = goodFrame.image.cols();
        int goodFrameRows = goodFrame.image.rows();

        // 2020 Images are at 640x480
        assertEquals(640, goodFrameCols);
        assertEquals(480, goodFrameRows);

        // TODO: find a way to skip this if a flag isn't set
        TestUtils.showImage(goodFrame.image, "2020");

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
