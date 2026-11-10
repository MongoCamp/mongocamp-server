package dev.mongocamp.graal;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeResourceAccess;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import static java.lang.System.out;


public class MongoCampFeature implements Feature {

    /**
     * Packages of the classes that are written to and read from MongoDB (users, roles, tokens, jobs, files, ...).
     * The driver converts them reflectively, so every class of these packages is registered for reflection while the image is built,
     * a hand-written reflect-config.json gets stale with every refactoring or dependency update.
     */
    private static final String[] REFLECTIVE_PACKAGES = {"dev.mongocamp.server.model"};

    private static boolean isReflective(String className) {
        for (String reflectivePackage : REFLECTIVE_PACKAGES) {
            if (className.startsWith(reflectivePackage + ".")) {
                return true;
            }
        }
        return false;
    }

    private void registerModelClassesForReflection(BeforeAnalysisAccess access, ScanResult scanResult) {
        int registered = 0;
        for (ClassInfo classInfo : scanResult.getAllClasses()) {
            if (isReflective(classInfo.getName())) {
                Class<?> clazz = access.findClassByName(classInfo.getName());
                if (clazz != null) {
                    RuntimeReflection.register(clazz);
                    RuntimeReflection.registerAllDeclaredConstructors(clazz);
                    RuntimeReflection.registerAllDeclaredFields(clazz);
                    RuntimeReflection.registerAllDeclaredMethods(clazz);
                    registered++;
                }
            }
        }
        out.println("MongoCamp-Classes registered for reflection: " + registered);
    }

    @Override
    public String getDescription() {
        return "Makes MongoCamp working like expected";
    }

    private Path writablePathFromClassPath(FeatureAccess access) {
        return access.getApplicationClassPath().stream().filter(p -> !p.toString().toLowerCase().endsWith("jar")).findFirst().orElseThrow(() -> new RuntimeException("No writable path found "));
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        out.println("Start checking for MongoCamp-Classes");
        URLClassLoader classLoader = URLClassLoader.newInstance(access.getApplicationClassPath().stream().map(p -> {
            try {
                return p.toUri().toURL();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new));
        ClassGraph classGraph = new ClassGraph().enableClassInfo().addClassLoader(classLoader);
        ScanResult scanResult = classGraph.scan();
        ScanResult.fromJSON(scanResult.toJSON());
        registerModelClassesForReflection(access, scanResult);
        Path path = Paths.get(writablePathFromClassPath(access) + "/mongocamp-classes.json");
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
            Files.writeString(path, scanResult.toJSON());
            // GraalVM 25 resolves the resource patterns before this feature writes the file, so the content is registered directly
            RuntimeResourceAccess.addResource(MongoCampFeature.class.getModule(), "mongocamp-classes.json", scanResult.toJSON().getBytes(StandardCharsets.UTF_8));
            out.println("MongoCamp-Classes written to " + path);
        } catch (IOException e) {
            out.println("MongoCamp-Classes error on write to " + path);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}