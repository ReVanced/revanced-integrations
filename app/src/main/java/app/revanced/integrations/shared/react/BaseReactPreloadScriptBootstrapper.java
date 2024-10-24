package app.revanced.integrations.shared.react;

import android.app.Activity;
import android.content.Context;
import com.facebook.react.bridge.CatalystInstanceImpl;

import java.io.*;

public abstract class BaseReactPreloadScriptBootstrapper {
    private Thread initializeThread;
    private File workingDirectory;

    protected abstract void initialize(Context context);

    public final void hookOnCreate(Activity mainActivity) {
        workingDirectory = mainActivity.getFilesDir();
        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            throw new RuntimeException("Failed to create working directory");
        }

        initializeThread = new Thread(() -> initialize(mainActivity));
        initializeThread.start();
    }

    public final void hookLoadScriptFromFile(CatalystInstanceImpl instance) {
        waitUntilInitialized();
        loadPreloadScripts(instance);
    }

    protected void loadPreloadScripts(CatalystInstanceImpl instance) {
        final var preloadScripts = workingDirectory.listFiles(pathname ->
                pathname.isFile() && pathname.getName().endsWith(".bundle"));
        assert preloadScripts != null;

        for (final var preloadScript : preloadScripts) {
            final var path = preloadScript.getAbsolutePath();
            instance.loadPreloadScriptFromFile(path, path, false);
        }
    }

    protected void waitUntilInitialized() {
        try {
            initializeThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected final File getWorkingDirectoryFile(String name) {
        return new File(workingDirectory, name);
    }

    protected final void write(InputStream inputStream, File file, int bufferSize) {
        try (final var fileOutputStream = new FileOutputStream(file)) {
            final var buffer = new byte[bufferSize];

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected final String read(File file, int bufferSize) {
        try (final var fileInputStream = new FileInputStream(file)) {
            final var buffer = new byte[bufferSize];
            final var stringBuilder = new StringBuilder();

            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                stringBuilder.append(new String(buffer, 0, bytesRead));
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
