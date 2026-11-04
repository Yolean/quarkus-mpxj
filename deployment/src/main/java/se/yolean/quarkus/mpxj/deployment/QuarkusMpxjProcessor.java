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
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

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

  // The readers' static initializers must run at image run time, not build time
  private static final List<String> runtimeInitialized = List.of(
      "org.mpxj.reader.UniversalProjectReader",
      "org.mpxj.conceptdraw.ConceptDrawProjectReader",
      "org.mpxj.ganttdesigner.GanttDesignerReader",
      "org.mpxj.ganttproject.GanttProjectReader",
      "org.mpxj.mspdi.MSPDIReader",
      "org.mpxj.phoenix.PhoenixReader",
      "org.mpxj.phoenix.Phoenix4Reader",
      "org.mpxj.phoenix.Phoenix5Reader",
      "org.mpxj.planner.PlannerReader",
      "org.mpxj.primavera.PrimaveraDatabaseFileReader",
      "org.mpxj.primavera.PrimaveraPMFileReader",
      "org.mpxj.primavera.PrimaveraXERFileReader",
      "org.mpxj.primavera.p3.P3DatabaseReader",
      "org.mpxj.primavera.p3.P3PRXFileReader",
      "org.mpxj.primavera.suretrak.SureTrakDatabaseReader",
      "org.mpxj.primavera.suretrak.SureTrakSTXFileReader");

  @BuildStep
  void runtimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInit) {
    runtimeInitialized.forEach(className -> runtimeInit.produce(new RuntimeInitializedClassBuildItem(className)));
  }

  @BuildStep
  void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
    logger.info("Producing index dependency build items for mpxj");
    indexDependency.produce(new IndexDependencyBuildItem("net.sf.mpxj", "mpxj"));
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
              .produce(b.methods().fields().constructors().build());
        });
  }
}
