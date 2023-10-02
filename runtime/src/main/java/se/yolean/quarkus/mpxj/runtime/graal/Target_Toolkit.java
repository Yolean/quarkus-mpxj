package se.yolean.quarkus.mpxj.runtime.graal;

import java.awt.Toolkit;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(Toolkit.class)
public final class Target_Toolkit {

  @Substitute
  static void loadLibraries() {}

  @Substitute
  private static void initStatic() {}

}
