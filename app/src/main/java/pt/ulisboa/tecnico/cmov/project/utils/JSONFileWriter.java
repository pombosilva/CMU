package pt.ulisboa.tecnico.cmov.project.utils;

import android.content.Context;
import android.util.JsonToken;
import android.util.Log;

import com.google.gson.stream.JsonReader;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class JSONFileWriter {

    public void writeJsonToFile(Context context, JsonReader jsonReader, String fileName) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            outputStreamWriter.write(readJson(jsonReader));

            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String readJson(JsonReader jsonReader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            stringBuilder.append(readJsonObject(jsonReader));

            if (jsonReader.hasNext()) {
                stringBuilder.append(", ");
            }
        }
        jsonReader.endArray();

        return "[" + stringBuilder.toString() + "]";
    }

    private String readJsonObject(JsonReader jsonReader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            stringBuilder.append("\"").append(name).append("\": ");

            if (jsonReader.peek() == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                stringBuilder.append(readJsonObject(jsonReader));
            } else if (jsonReader.peek() == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                stringBuilder.append(readJsonArray(jsonReader));
            } else {
                stringBuilder.append(readJsonValue(jsonReader));
            }

            if (jsonReader.hasNext()) {
                stringBuilder.append(", ");
            }
        }
        jsonReader.endObject();

        return "{ " + stringBuilder.toString() + " }";
    }

    private String readJsonArray(JsonReader jsonReader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            if (jsonReader.peek() == com.google.gson.stream.JsonToken.BEGIN_OBJECT) {
                stringBuilder.append(readJsonObject(jsonReader));
            } else if (jsonReader.peek() == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                stringBuilder.append(readJsonArray(jsonReader));
            } else {
                stringBuilder.append(readJsonValue(jsonReader));
            }

            if (jsonReader.hasNext()) {
                stringBuilder.append(", ");
            }
        }
        jsonReader.endArray();

        return "[ " + stringBuilder.toString() + " ]";
    }

    private String readJsonValue(JsonReader jsonReader) throws IOException {
        com.google.gson.stream.JsonToken token = jsonReader.peek();
        if (token == com.google.gson.stream.JsonToken.BOOLEAN) {
            return String.valueOf(jsonReader.nextBoolean());
        } else if (token == com.google.gson.stream.JsonToken.NUMBER) {
            return String.valueOf(jsonReader.nextDouble());
        } else if (token == com.google.gson.stream.JsonToken.STRING) {
            return "\"" + jsonReader.nextString() + "\"";
        } else if (token == com.google.gson.stream.JsonToken.NULL) {
            jsonReader.nextNull();
            return "null";
        }

        return "";
    }
}