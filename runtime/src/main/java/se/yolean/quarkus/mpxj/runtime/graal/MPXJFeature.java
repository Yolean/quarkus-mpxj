package se.yolean.quarkus.mpxj.runtime.graal;

import java.util.List;

import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

public class MPXJFeature implements Feature {

  private static List<String> classes = List.of(
      "net.sf.mpxj.reader.UniversalProjectReader",
      "net.sf.mpxj.conceptdraw.ConceptDrawProjectReader",
      "net.sf.mpxj.ganttdesigner.GanttDesignerReader",
      "net.sf.mpxj.ganttproject.GanttProjectReader",
      "net.sf.mpxj.mspdi.MSPDIReader",
      "net.sf.mpxj.phoenix.PhoenixReader",
      "net.sf.mpxj.phoenix.Phoenix4Reader",
      "net.sf.mpxj.phoenix.Phoenix5Reader",
      "net.sf.mpxj.planner.PlannerReader",
      "net.sf.mpxj.primavera.PrimaveraDatabaseFileReader",
      "net.sf.mpxj.primavera.PrimaveraPMFileReader",
      "net.sf.mpxj.primavera.PrimaveraXERFileReader",
      "net.sf.mpxj.primavera.p3.P3DatabaseReader",
      "net.sf.mpxj.primavera.p3.P3PRXFileReader",
      "net.sf.mpxj.primavera.suretrak.SureTrakDatabaseReader",
      "net.sf.mpxj.primavera.suretrak.SureTrakSTXFileReader");


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
