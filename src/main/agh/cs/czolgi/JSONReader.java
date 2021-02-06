package agh.cs.czolgi;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class JSONReader {
    private final JSONParser parser = new JSONParser();
    private final String path;

    public JSONReader(String path) {
        this.path = path;
    }

    public JSONObject read() {
        try (Reader reader = new FileReader(this.path)) {
            return ((JSONObject) parser.parse(reader));
        } catch (IllegalArgumentException | IOException | ParseException ex) {
            ex.printStackTrace();
            System.out.println(ex);
            System.exit(1);
        }
        return new JSONObject();
    }
}
