package se.yolean.quarkus.mpxj.deployment;

import java.util.Collection;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.BytecodeTransformerBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.NativeImageFeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import se.yolean.quarkus.mpxj.runtime.graal.MPXJFeature;

class QuarkusMpxjProcessor {

  private static final String FEATURE = "quarkus-mpxj";
  private static final Logger logger = LoggerFactory.getLogger(QuarkusMpxjProcessor.class);

  private static final List<String> packagesToReflect = List.of(
      "org.mpxj.common",
      "org.mpxj.mspdi.schema",
      "org.mpxj.planner.schema",
      "org.mpxj.ganttdesigner.schema",
      "org.mpxj.ganttproject.schema",
      "org.mpxj.phoenix.schema.phoenix4",
      "org.mpxj.conceptdraw.schema",
      "org.mpxj.primavera",
      "org.mpxj.primavera.schema");

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  NativeImageFeatureBuildItem nativeImageFeature() {
    return new NativeImageFeatureBuildItem(MPXJFeature.class);
  }

  @BuildStep
  void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
    logger.info("Producing index dependency build items for mpxj");
    indexDependency.produce(new IndexDependencyBuildItem("org.mpxj", "mpxj"));
  }

  @BuildStep
  void registerClassesForReflection(CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveClassBuildItem> reflectiveClass, List<BytecodeTransformerBuildItem> transfomed) {
    logger.info("Registering classes for reflection");

    IndexView index = combinedIndexBuildItem.getIndex();

    packagesToReflect.stream()
        .flatMap(pack -> {
          Collection<ClassInfo> classes = index.getClassesInPackage(pack);
          if (classes.isEmpty()) throw new IllegalArgumentException("No classes found in package " + pack);
          return classes.stream();
        })
        .forEach(classInfo -> {
          ReflectiveClassBuildItem.Builder b = ReflectiveClassBuildItem.builder(classInfo.getClass());
          reflectiveClass
              .produce(b.methods().fields().build());
          reflectiveClass
              .produce(b.constructors().fields().build());
        });
  }
}
