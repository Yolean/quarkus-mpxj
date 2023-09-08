package se.yolean.quarkus.mpxj.deployment;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.transformer.action.ActionContext;
import org.eclipse.transformer.action.ByteData;
import org.eclipse.transformer.action.impl.ActionContextImpl;
import org.eclipse.transformer.action.impl.ByteDataImpl;
import org.eclipse.transformer.action.impl.ClassActionImpl;
import org.eclipse.transformer.action.impl.SelectionRuleImpl;
import org.eclipse.transformer.action.impl.SignatureRuleImpl;
import org.eclipse.transformer.util.FileUtils;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.objectweb.asm.ClassReader;
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
      "net.sf.mpxj.common",
      "net.sf.mpxj.mspdi.schema",
      "net.sf.mpxj.planner.schema",
      "net.sf.mpxj.ganttdesigner.schema",
      "net.sf.mpxj.ganttproject.schema",
      "net.sf.mpxj.phoenix.schema.phoenix4",
      "net.sf.mpxj.conceptdraw.schema",
      "net.sf.mpxj.primavera",
      "net.sf.mpxj.primavera.schema");

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
    indexDependency.produce(new IndexDependencyBuildItem("net.sf.mpxj", "mpxj"));
  }

  @BuildStep
  void transformToJakarta(CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<BytecodeTransformerBuildItem> producer) {
    JakartaTransformer transformer = new JakartaTransformer();
    logger.info("Transforming javax.xml package names to jakarta.xml");

    IndexView index = combinedIndexBuildItem.getIndex();

    List<DotName> allPackages = Stream.of(DotName.createSimple("net.sf.mpxj"))
        .flatMap(s -> streamSubPackages(s, index))
        .toList();

    allPackages.stream()
        .flatMap(pack -> index.getClassesInPackage(pack).stream())
        .map(classInfo -> classInfo.name().toString())
        .map(s -> new BytecodeTransformerBuildItem.Builder().setCacheable(true).setContinueOnFailure(false)
            .setEager(true)
            .setClassToTransform(s).setClassReaderOptions(ClassReader.SKIP_DEBUG)
            .setInputTransformer(transformer::transform).build())
        .forEach(producer::produce);
  }

  private Stream<DotName> streamSubPackages(DotName pack, IndexView index) {
    return Stream.concat(Stream.of(pack),
        index.getSubpackages(pack).stream().flatMap(s -> streamSubPackages(s, index)));
  }

  @BuildStep
  void registerClassesForReflection(CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveClassBuildItem> reflectiveClass, List<BytecodeTransformerBuildItem> transfomed) {
    logger.info("Registering classes for reflection");

    IndexView index = combinedIndexBuildItem.getIndex();

    packagesToReflect.stream()
        .flatMap(pack -> index.getClassesInPackage(pack).stream())
        .forEach(classInfo -> {
          reflectiveClass
              .produce(ReflectiveClassBuildItem.builder(classInfo.name().toString()).methods().fields().build());
        });
  }

  /**
   * REVIEW workaround until mpxj is updated to use the 4.x version of jakarta.xml.bind-api
   * https://github.com/joniles/mpxj/issues/548
   */
  private static class JakartaTransformer {

    private final Logger logger;
    private final ActionContext ctx;
    private static final Map<String, String> renames = Map.of("javax.xml.bind.*", "jakarta.xml.bind");

    JakartaTransformer() {
      logger = LoggerFactory.getLogger("JakartaTransformer");

      ctx = new ActionContextImpl(logger, new SelectionRuleImpl(logger, Collections.emptyMap(), Collections.emptyMap()),
          new SignatureRuleImpl(logger, renames, null, null, null, null, null, Collections.emptyMap()));
    }

    byte[] transform(final String name, final byte[] bytes) {
      logger.debug("Jakarta EE compatibility enhancer for Quarkus: transforming " + name);
      final ClassActionImpl classTransformer = new ClassActionImpl(ctx);
      final ByteBuffer input = ByteBuffer.wrap(bytes);
      final ByteData inputData = new ByteDataImpl(name, input, FileUtils.DEFAULT_CHARSET);
      final ByteData outputData = classTransformer.apply(inputData);
      return outputData.buffer().array();
    }
  }
}
