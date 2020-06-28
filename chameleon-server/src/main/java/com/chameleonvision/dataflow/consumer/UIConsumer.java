package com.chameleonvision.dataflow.consumer;

import com.chameleonvision.common.logging.LogGroup;
import com.chameleonvision.common.logging.Logger;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public class UIConsumer implements Observer{
    private final Logger logger = new Logger(UIConsumer.class, LogGroup.VisionProcess);
    public UIConsumer(){

    }
    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(Object o) {

    }

    @Override
    public void onError(@NonNull Throwable e) {
        logger.error(e.getMessage());
    }

    @Override
    public void onComplete() {

    }
}
