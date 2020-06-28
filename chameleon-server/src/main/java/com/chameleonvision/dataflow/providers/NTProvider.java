package com.chameleonvision.dataflow.providers;

import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import com.chameleonvision.dataflow.consumer.UIConsumer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;

public class NTProvider extends Observable {
    private final Logger logger = new Logger(UIConsumer.class, LogGroup.VisionProcess);

    public NTProvider(String name){
        NetworkTable table =
                NetworkTableInstance.getDefault()
                        .getTable("/chameleon-vision/" + name);

    }

    @Override
    protected void subscribeActual(@NonNull Observer observer) {

    }
}
