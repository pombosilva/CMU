package pt.ulisboa.tecnico.cmov.project.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class InternalStorage {

    private static final String LINE_SEP = System.getProperty("line.separator");

    public static void write(String filename, String contentToWrite, ContextWrapper ctxWrp) {
        FileOutputStream fos = null;
        try {
            // note that there are many modes you can use
            Log.d("InternalStorage", "Vou escrever no file.");
            fos = ctxWrp.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(contentToWrite.getBytes());
        } catch (FileNotFoundException e) {
            Log.e("InternalStorage", "File not found", e);
        } catch (IOException e) {
            Log.e("InternalStorage", "IO problem", e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.d("InternalStorage", "Close error.");
            }
        }
    }

    public static String read(String filename, ContextWrapper ctxWrp) {
        FileInputStream fis = null;
        Scanner scanner = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = ctxWrp.openFileInput(filename);
            // scanner does mean one more object, but it's easier to work with
            scanner = new Scanner(fis);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine() + LINE_SEP);
            }
        } catch (FileNotFoundException e) {
            Log.e("InternalStorage", "File not found", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.d("FileExplorer", "Close error.");
                }
            }
            if (scanner != null) {
                scanner.close();
            }
        }
        return sb.toString();
    }

}
