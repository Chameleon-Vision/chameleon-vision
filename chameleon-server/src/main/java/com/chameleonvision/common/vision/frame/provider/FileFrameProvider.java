package com.chameleonvision.common.vision.frame.provider;

import com.chameleonvision.common.vision.frame.Frame;
import com.chameleonvision.common.vision.frame.FrameProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
* A {@link FrameProvider} that will read and provide an image from a {@link java.nio.file.Path
* path}.
*/
public class FileFrameProvider implements FrameProvider {
    private Frame m_frame;
    private Path m_path;

    private boolean m_reloadImage;

    /**
    * Instantiates a new FileFrameProvider.
    *
    * @param path The path of the image to read from.
    */
    public FileFrameProvider(Path path) {
        if (!Files.exists(path)) throw new RuntimeException("Invalid path for image!");
        m_path = path;

        loadImage();
    }

    /**
    * Instantiates a new File frame provider.
    *
    * @param pathAsString The path of the image to read from as a string.
    */
    public FileFrameProvider(String pathAsString) {
        this(Paths.get(pathAsString));
    }

    private void loadImage() {
        Mat image = Imgcodecs.imread(m_path.toString());

        if (image.cols() > 0 && image.rows() > 0) {
            m_frame = new Frame(image);
        } else {
            throw new RuntimeException("Image loading failed!");
        }
    }

    /**
    * Set image reloading. If true this will reload the image from the path set in the constructor
    * every time {@link FileFrameProvider#getFrame()} is called.
    *
    * @param reloadImage True to enable image reloading.
    */
    public void setImageReloading(boolean reloadImage) {
        m_reloadImage = reloadImage;
    }

    /**
    * Returns if image reloading is enabled.
    *
    * @return True if image reloading is enabled.
    */
    public boolean isImageReloading() {
        return m_reloadImage;
    }

    @Override
    public Frame getFrame() {
        if (m_reloadImage) {
            loadImage();
        }

        return m_frame;
    }
}
