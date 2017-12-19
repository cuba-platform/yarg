package extraction.fixture.yml;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.haulmont.yarg.structure.BandOrientation;

import java.io.IOException;

public class BandOrientationDeserializer extends JsonDeserializer<BandOrientation> {
    @Override
    public BandOrientation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return BandOrientation.defaultIfNull(BandOrientation.fromId(p.getValueAsString()));
    }
}
