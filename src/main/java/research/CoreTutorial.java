package research;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Example usage of the JaCoCo core API. In this tutorial a single target class
 * will be instrumented and executed. Finally the coverage information will be
 * dumped.
 */
public final class CoreTutorial {
    private final PrintStream out;

    /**
     * Creates a new example instance printing to the given stream.
     *
     * @param out stream for outputs
     */
    public CoreTutorial(final PrintStream out) {
        this.out = out;
    }

    /**
     * Entry point to run this examples as a Java application.
     *
     * @param args list of program arguments
     * @throws Exception in case of errors
     */
    public static void main(final String[] args) throws Exception {
        new CoreTutorial(System.out).execute(NoBranchesClass.class);
        new CoreTutorial(System.out).execute(WithBranchesClass.class);
    }

    /**
     * Run this example.
     *
     * @throws Exception in case of errors
     */
    public void execute(Class<?> clazz) throws Exception {
        final ExecutionDataStore executionData =
                run(clazz.getName());

        final CoverageBuilder coverageBuilder =
                analyze(clazz.getName(), executionData);

        printResults(coverageBuilder);
    }

    private ExecutionDataStore run(String targetName) throws Exception {
        IRuntime runtime = new LoggerRuntime();
        RuntimeData data = new RuntimeData();
        ClassLoader memoryClassLoader = createClassLoader(targetName + "Test", runtime, data);

        org.junit.runner.JUnitCore.runClasses(memoryClassLoader.loadClass(targetName));

        return getExecutionDataStore(runtime, data);
    }

    private ExecutionDataStore getExecutionDataStore(IRuntime runtime, RuntimeData data) {
        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();
        return executionData;
    }

    private ClassLoader createClassLoader(String targetName, IRuntime runtime, RuntimeData data) throws Exception {
        Instrumenter instr = new Instrumenter(runtime);
        byte[] instrumented = instr.instrument(getTargetClass(targetName), targetName);

        runtime.startup(data);
        MemoryClassLoader memoryClassLoader = new MemoryClassLoader();
        memoryClassLoader.addDefinition(targetName, instrumented);
        return memoryClassLoader;
    }

    private CoverageBuilder analyze(String targetName, ExecutionDataStore executionData) throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        analyzer.analyzeClass(getTargetClass(targetName), targetName);
        return coverageBuilder;
    }

    private void printResults(CoverageBuilder coverageBuilder) {
        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            out.printf("Coverage of class %s%n", cc.getName());

            System.out.println("instructions: " + cc.getInstructionCounter().getCoveredRatio());
            System.out.println("branches: " + cc.getBranchCounter().getCoveredRatio());
            System.out.println("lines: " + cc.getLineCounter().getCoveredRatio());
            System.out.println("methods: " + cc.getMethodCounter().getCoveredRatio());
            System.out.println("complexity: " + cc.getComplexityCounter().getCoveredRatio());
        }
    }

    private InputStream getTargetClass(final String name) {
        final String resource = '/' + name.replace('.', '/') + ".class";
        return getClass().getResourceAsStream(resource);
    }

    /**
     * A class loader that loads classes from in-memory data.
     */
    public static class MemoryClassLoader extends ClassLoader {

        private final Map<String, byte[]> definitions = new HashMap<String, byte[]>();

        /**
         * Add a in-memory representation of a class.
         *
         * @param name  name of the class
         * @param bytes class definition
         */
        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        protected Class<?> loadClass(final String name, final boolean resolve)
                throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.loadClass(name, resolve);
        }

    }

}

