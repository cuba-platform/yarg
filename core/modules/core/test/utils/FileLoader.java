package utils;

import java.io.File;
import java.net.URISyntaxException;

public class FileLoader {

    private FileLoader() {
    }

    public static File load(String resource) throws URISyntaxException {
        return new File(FixtureUtils.class.getClassLoader().getResource(resource).toURI());
    }
}
