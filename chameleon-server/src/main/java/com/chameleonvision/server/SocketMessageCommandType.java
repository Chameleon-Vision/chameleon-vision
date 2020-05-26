package com.chameleonvision.server;

public enum SocketMessageCommandType {
    SMCT_DELETECURRENTPIPELINE("deleteCurrentPipeline"),
    SMCT_SAVE("save");

    public final String entryValue;

    SocketMessageCommandType(String entryValue) {
        this.entryValue = entryValue;
    }
}
