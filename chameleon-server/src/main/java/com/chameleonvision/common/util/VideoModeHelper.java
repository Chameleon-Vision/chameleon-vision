package com.chameleonvision.common.util;

import edu.wpi.cscore.VideoMode;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class VideoModeHelper {
    public static List<HashMap<String, Object>> videoModeToHashMapList(List<VideoMode> videoModes) {

        return videoModes.stream()
                .map(VideoModeHelper::VideoModeToHashMap)
                .collect(Collectors.toList());
    }

    public static HashMap<String, Object> VideoModeToHashMap(VideoMode videoMode) {
        return new HashMap<String, Object>() {
            {
                put("width", videoMode.width);
                put("height", videoMode.height);
                put("fps", videoMode.fps);
                put("pixelFormat", videoMode.pixelFormat.toString());
            }
        };
    }
}
