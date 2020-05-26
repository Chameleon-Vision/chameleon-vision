package com.chameleonvision.common.dataflow;

import com.chameleonvision.common.dataflow.events.DataChangeEvent;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("rawtypes")
public abstract class DataChangeSubscriber {
    public final List<DataChangeSource> wantedSources;
    public final List<DataChangeDestination> wantedDestinations;

    private final int hash;

    public DataChangeSubscriber(
            List<DataChangeSource> wantedSources, List<DataChangeDestination> wantedDestinations) {
        this.wantedSources = wantedSources;
        this.wantedDestinations = wantedDestinations;
        hash = Objects.hash(wantedSources, wantedDestinations);
    }

    public abstract void onDataChangeEvent(DataChangeEvent event);

    @Override
    public int hashCode() {
        return hash;
    }
}
