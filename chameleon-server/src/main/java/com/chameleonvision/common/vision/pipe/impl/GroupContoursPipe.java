package com.chameleonvision.common.vision.pipe.impl;

import com.chameleonvision.common.vision.opencv.Contour;
import com.chameleonvision.common.vision.opencv.ContourGroupingMode;
import com.chameleonvision.common.vision.opencv.ContourIntersectionDirection;
import com.chameleonvision.common.vision.pipe.CVPipe;
import com.chameleonvision.common.vision.target.PotentialTarget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupContoursPipe
        extends CVPipe<List<Contour>, List<PotentialTarget>, GroupContoursPipe.GroupContoursParams> {

    private List<PotentialTarget> m_targets = new ArrayList<>();

    @Override
    protected List<PotentialTarget> process(List<Contour> input) {
        for (var target : m_targets) {
            target.release();
        }

        m_targets.clear();

        if (params.getGroup() == ContourGroupingMode.Single) {
            for (var contour : input) {
                m_targets.add(new PotentialTarget(contour));
            }
        } else {
            int groupingCount = params.getGroup().count;

            if (input.size() > groupingCount) {
                // todo: is it OK to mutate the input list?
                //  or should we clone it like before?
                //  what is the perf hit on cloning?
                input.sort(Contour.SortByMomentsX);
                // also why reverse? shouldn't the sort comparator just get reversed?
                Collections.reverse(input);
                // find out next time on Code Mysteries...

                for (int i = 0; i < input.size() - 1; i++) {
                    // make a list of the desired count of contours to group
                    List<Contour> groupingSet;
                    try {
                        groupingSet = input.subList(i, i + groupingCount);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    try {

                        // FYI: This method only takes 2 contours!
                        Contour groupedContour =
                                Contour.groupContoursByIntersection(
                                        groupingSet.get(0), groupingSet.get(1), params.getIntersection());

                        if (groupedContour != null) {
                            m_targets.add(new PotentialTarget(groupedContour, groupingSet));
                            i += (groupingCount - 1);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return m_targets;
    }

    public static class GroupContoursParams {
        private ContourGroupingMode m_group;
        private ContourIntersectionDirection m_intersection;

        public GroupContoursParams(
                ContourGroupingMode group, ContourIntersectionDirection intersectionDirection) {
            m_group = group;
            m_intersection = intersectionDirection;
        }

        public ContourGroupingMode getGroup() {
            return m_group;
        }

        public ContourIntersectionDirection getIntersection() {
            return m_intersection;
        }
    }
}
