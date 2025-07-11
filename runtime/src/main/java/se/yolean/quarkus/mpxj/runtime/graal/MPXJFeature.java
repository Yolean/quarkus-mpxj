package se.yolean.quarkus.mpxj.runtime.graal;

import java.util.List;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

public class MPXJFeature implements Feature {

  private static List<String> classes = List.of(
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


  @Override
  public void afterRegistration(AfterRegistrationAccess access) {
    final RuntimeClassInitializationSupport runtimeInit = ImageSingletons
        .lookup(RuntimeClassInitializationSupport.class);
    final String reason = "Quarkus run time init for MPXJ";

    classes.forEach(className -> {
      runtimeInit.initializeAtRunTime(className, reason);
    });
  }

  @Override
  public String getDescription() {
    return "Quarkus runtime initialization for MPXJ";
  }

}
