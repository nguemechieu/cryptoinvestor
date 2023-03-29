package cryptoinvestor.cryptoinvestor.JsonToCsv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import static java.lang.System.out;

public class JsonToCsv {

    public JsonToCsv() {
    }

    private static final Logger logger = LoggerFactory.getLogger(JsonToCsv.class);

    public void convertJsonToCsv(String jsonfileName, Object data) throws IOException {

        try {

            if (data == null || jsonfileName == null) {
                out.println("No data to convert");
                return;
            }
            FileWriter file = new FileWriter(jsonfileName);

            file.write(String.valueOf(data));
            out.println(jsonfileName + " converted to csv");
            file.flush();

            file.close();
        } catch (IOException io) {
            logger.error(io.getMessage());
        }


        JsonNode jsonTree = new ObjectMapper().readTree(new File(jsonfileName));
        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
        JsonNode firstObject = jsonTree.elements().next();
        firstObject.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
        CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.writerFor(JsonNode.class)
                .with(csvSchema)
                .writeValue(new File(jsonfileName + ".csv"), jsonTree);
        out.println(jsonTree);

    }


}
