package utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.haulmont.yarg.structure.ReportBand;
import extraction.fixture.yml.YmlReportBand;

import java.io.File;
import java.io.IOException;

public class YmlDataUtil {

    private YmlDataUtil() {}

    static ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static ReportBand bandFrom(File file) throws IOException {
        return mapper.readValue(file, YmlReportBand.class);
    }
}
