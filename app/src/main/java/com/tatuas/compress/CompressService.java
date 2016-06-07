package com.tatuas.compress;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CompressService extends IntentService {

    private static final String PATH_SD_CARD = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String EXTRA_LOGS = "logs";
    public static final String NAME_INTENT_FILTER = "CompressServiceFilter";

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, CompressService.class);
    }

    public CompressService() {
        super("CompressService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ArrayList<String> logs = new ArrayList<>();

        try {
            // tar
            logs.add(compress("compressed-" + System.currentTimeMillis() + ".tar", ArchiveStreamFactory.TAR));
            // zip
            logs.add(compress("compressed-" + System.currentTimeMillis() + ".zip", ArchiveStreamFactory.ZIP));
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            LocalBroadcastManager
                    .getInstance(getApplicationContext())
                    .sendBroadcast(new Intent(NAME_INTENT_FILTER).putStringArrayListExtra(EXTRA_LOGS, logs));
        }
    }

    private String compress(final String filename, final String archiveType) throws Throwable {
        final long start = System.currentTimeMillis();

        final File compressed = new File(PATH_SD_CARD, filename);
        final OutputStream output = new FileOutputStream(compressed);
        final ArchiveOutputStream archiveOutput = new ArchiveStreamFactory()
                .createArchiveOutputStream(archiveType, output);

        final List<File> files = new ArrayList<>();
        files.add(new File(PATH_SD_CARD, "1.jpg"));
        files.add(new File(PATH_SD_CARD, "2.jpg"));
        files.add(new File(PATH_SD_CARD, "3.jpg"));
        files.add(new File(PATH_SD_CARD, "4.jpg"));
        files.add(new File(PATH_SD_CARD, "5.jpg"));

        switch (archiveType) {
            case ArchiveStreamFactory.TAR:
                for (File f : files) {
                    final TarArchiveEntry tarArchive = new TarArchiveEntry(f.getName() + "-tar-" + System.currentTimeMillis() + ".jpg");
                    tarArchive.setSize(f.length());
                    archiveOutput.putArchiveEntry(tarArchive);
                    IOUtils.copy(new FileInputStream(f), archiveOutput);
                    archiveOutput.closeArchiveEntry();
                }

                break;
            case ArchiveStreamFactory.ZIP:
                for (File f : files) {
                    final ZipArchiveEntry zipArchive = new ZipArchiveEntry(f.getName() + "-zip-" + System.currentTimeMillis() + ".jpg");
                    zipArchive.setSize(f.length());
                    archiveOutput.putArchiveEntry(zipArchive);
                    IOUtils.copy(new FileInputStream(f), archiveOutput);
                    archiveOutput.closeArchiveEntry();
                }

                break;
        }

        archiveOutput.finish();
        archiveOutput.close();

        final long end = System.currentTimeMillis();

        return TextUtils.join(" | ", new String[]{
                "filename:" + filename,
                "time:" + time(start, end),
                "size:" + kb(compressed)});
    }

    private String time(final long start, final long end) {
        return String.valueOf(TimeUnit.MILLISECONDS.toMillis(end - start)) + "ms";
    }

    private String kb(final File file) {
        return String.valueOf(file.length() / 1024) + "KB";
    }
}
