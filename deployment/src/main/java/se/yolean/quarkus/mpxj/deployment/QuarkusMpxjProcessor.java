package se.yolean.quarkus.mpxj.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class QuarkusMpxjProcessor {

    private static final String FEATURE = "quarkus-mpxj";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
