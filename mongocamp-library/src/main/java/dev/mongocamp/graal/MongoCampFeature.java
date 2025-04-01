package dev.mongocamp.graal;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.graalvm.nativeimage.hosted.Feature;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.out;


public class MongoCampFeature implements Feature {

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
        Path path = Paths.get(writablePathFromClassPath(access) + "/mongocamp-classes.json");
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
            Files.writeString(path, scanResult.toJSON());
            out.println("MongoCamp-Classes written to " + path);
        } catch (IOException e) {
            out.println("MongoCamp-Classes error on write to " + path);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}