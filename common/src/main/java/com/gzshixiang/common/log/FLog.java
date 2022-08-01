package com.gzshixiang.common.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FLog {
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;

    private static final String[] LEVEL_LABEL = {"", "", "V", "D", "I", "W", "E", "A"};

    private static final String TAG = "FLog";
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 10L;
    private static final int DUMP_LOG_TIMEOUT_SECONDS = 30;

    private static FLog sInstance;
    private static final int STACK_INDEX = 3;

    private Context mApplication;
    private String mDefaultTag = TAG;
    private boolean mIsDebug;
    private boolean mIsWriteToFile;
    private boolean mIsShowThread;
    private int mLogFileSaveDays = 0;
    private static String sLogFolder;
    private DateChangedReceiver mReceiver;

    private static final String FILE_SUFFIX = ".log";
    private static final SimpleDateFormat FILE_CONTENT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private ExecutorService mExecutor;
    private static OnErrorListener sOnErrorListener;

    private FLog(Builder builder) {
        if (builder != null) {
            mDefaultTag = builder.mDefaultTag;
            mIsDebug = builder.mIsDebug;
            mIsShowThread = builder.mIsShowThread;
            mIsWriteToFile = builder.mIsWriteToFile;
            mLogFileSaveDays = builder.mLogFileSaveDays;
            mApplication = builder.mApplication;
            initFileDir(mApplication);
        }
        if (mIsWriteToFile) {
            mExecutor = Executors.newSingleThreadExecutor();
        }
        delOverSaveDaysLogs();

        if (mReceiver == null) {
            mReceiver = new DateChangedReceiver();
            mReceiver.registerReceiver(mApplication);
        }
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static void init(Context context, boolean isDebug, boolean isWriteToFile, int logFileSaveDays) {
        FLog.with(context)
                .setDefaultTag(TAG)
                .setDebug(isDebug)
                .setWriteToFile(isWriteToFile)
                .setLogFileSaveDays(logFileSaveDays)
                .init();
    }

    public static void setOnErrorListener(OnErrorListener onErrorListener) {
        sOnErrorListener = onErrorListener;
    }

    private void initFileDir(Context context) {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                sLogFolder = context.getExternalFilesDir("log").getPath() + File.separator;
            } else {
                sLogFolder = context.getCacheDir() + File.separator + "log" + File.separator;
            }
            log(INFO, mDefaultTag, "[Log path] " + sLogFolder, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void log(final int level, final String tag, String msg, Throwable throwable) {
        if (msg == null && throwable == null) return;
        final String msgs;
        if (throwable == null) {
            msgs = msg;
        } else if (msg == null) {
            msgs = Log.getStackTraceString(throwable);
        } else {
            msgs = msg + '\n' + Log.getStackTraceString(throwable);
        }
        if (throwable != null && sOnErrorListener != null) {
            sOnErrorListener.onError(level, throwable);
        }
        final String information = getInformation(tag, msgs);
        if (mIsDebug || level >= WARN) {
            Log.println(level, mDefaultTag, information);
        }
        if (mIsWriteToFile) {
            enqueue(new Runnable() {
                @Override
                public void run() {
                    writeToFile(level, mDefaultTag, information);
                }
            });
        }
    }

    private static void staticLog(final int level, final String tag, String msg, Throwable throwable) {
        if (msg == null && throwable == null) return;
        final String msgs;
        if (throwable == null) {
            msgs = msg;
        } else if (msg == null) {
            msgs = Log.getStackTraceString(throwable);
        } else {
            msgs = msg + '\n' + Log.getStackTraceString(throwable);
        }
        if (throwable != null && sOnErrorListener != null) {
            sOnErrorListener.onError(level, throwable);
        }
        Log.println(level, TextUtils.isEmpty(tag) ? TAG : tag, msgs);
    }

    public static void a(String msg) {
        if (sInstance == null) {
            staticLog(ASSERT, null, msg, null);
        } else {
            sInstance.log(ASSERT, null, msg, null);
        }
    }

    public static void a(String tag, String msg) {
        if (sInstance == null) {
            staticLog(ASSERT, tag, msg, null);
        } else {
            sInstance.log(ASSERT, tag, msg, null);
        }
    }

    public static void a(String tag, String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(ASSERT, tag, msg, throwable);
        } else {
            sInstance.log(ASSERT, tag, msg, throwable);
        }
    }

    public static void a(String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(ASSERT, null, msg, throwable);
        } else {
            sInstance.log(ASSERT, null, msg, throwable);
        }
    }

    public static void a(Throwable throwable) {
        if (sInstance == null) {
            staticLog(ASSERT, null, null, throwable);
        } else {
            sInstance.log(ASSERT, null, null, throwable);
        }
    }

    public static void v(String msg) {
        if (sInstance == null) {
            staticLog(VERBOSE, null, msg, null);
        } else {
            sInstance.log(VERBOSE, null, msg, null);
        }
    }

    public static void v(String tag, String msg) {
        if (sInstance == null) {
            staticLog(VERBOSE, tag, msg, null);
        } else {
            sInstance.log(VERBOSE, tag, msg, null);
        }
    }

    public static void v(String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(VERBOSE, null, msg, throwable);
        } else {
            sInstance.log(VERBOSE, null, msg, throwable);
        }
    }

    public static void v(String tag, String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(VERBOSE, tag, msg, throwable);
        } else {
            sInstance.log(VERBOSE, tag, msg, throwable);
        }
    }

    public static void v(Throwable throwable) {
        if (sInstance == null) {
            staticLog(VERBOSE, null, null, throwable);
        } else {
            sInstance.log(VERBOSE, null, null, throwable);
        }
    }

    public static void i(String msg) {
        if (sInstance == null) {
            staticLog(INFO, null, msg, null);
        } else {
            sInstance.log(INFO, null, msg, null);
        }
    }

    public static void i(String tag, String msg) {
        if (sInstance == null) {
            staticLog(INFO, tag, msg, null);
        } else {
            sInstance.log(INFO, tag, msg, null);
        }
    }

    public static void i(Throwable throwable) {
        if (sInstance == null) {
            staticLog(INFO, null, null, throwable);
        } else {
            sInstance.log(INFO, null, null, throwable);
        }
    }

    public static void i(String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(INFO, null, msg, throwable);
        } else {
            sInstance.log(INFO, null, msg, throwable);
        }
    }

    public static void i(String tag, String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(INFO, tag, msg, throwable);
        } else {
            sInstance.log(INFO, tag, msg, throwable);
        }
    }

    public static void d(String msg) {
        if (sInstance == null) {
            staticLog(DEBUG, null, msg, null);
        } else {
            sInstance.log(DEBUG, null, msg, null);
        }
    }

    public static void d(String tag, String msg) {
        if (sInstance == null) {
            staticLog(DEBUG, tag, msg, null);
        } else {
            sInstance.log(DEBUG, tag, msg, null);
        }
    }

    public static void d(String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(DEBUG, null, msg, throwable);
        } else {
            sInstance.log(DEBUG, null, msg, throwable);
        }
    }

    public static void d(Throwable throwable) {
        if (sInstance == null) {
            staticLog(DEBUG, null, null, throwable);
        } else {
            sInstance.log(DEBUG, null, null, throwable);
        }
    }

    public static void d(String tag, String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(DEBUG, tag, msg, throwable);
        } else {
            sInstance.log(DEBUG, tag, msg, throwable);
        }
    }

    public static void w(String msg) {
        if (sInstance == null) {
            staticLog(WARN, null, msg, null);
        } else {
            sInstance.log(WARN, null, msg, null);
        }
    }

    public static void w(String tag, String msg) {
        if (sInstance == null) {
            staticLog(WARN, tag, msg, null);
        } else {
            sInstance.log(WARN, tag, msg, null);
        }
    }

    public static void w(String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(WARN, null, msg, throwable);
        } else {
            sInstance.log(WARN, null, msg, throwable);
        }
    }

    public static void w(Throwable throwable) {
        if (sInstance == null) {
            staticLog(WARN, null, null, throwable);
        } else {
            sInstance.log(WARN, null, null, throwable);
        }
    }

    public static void w(String tag, String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(WARN, tag, msg, throwable);
        } else {
            sInstance.log(WARN, tag, msg, throwable);
        }
    }

    public static void e(String msg) {
        if (sInstance == null) {
            staticLog(ERROR, null, msg, null);
        } else {
            sInstance.log(ERROR, null, msg, null);
        }
    }

    public static void e(String tag, String msg) {
        if (sInstance == null) {
            staticLog(ERROR, tag, msg, null);
        } else {
            sInstance.log(ERROR, tag, msg, null);
        }
    }

    public static void e(String tag, String msg, Throwable throwable) {
        if (sInstance == null) {
            staticLog(ERROR, tag, msg, throwable);
        } else {
            sInstance.log(ERROR, tag, msg, throwable);
        }
    }

    public static void e(String tag, Throwable throwable) {
        if (sInstance == null) {
            staticLog(ERROR, tag, null, throwable);
        } else {
            sInstance.log(ERROR, tag, null, throwable);
        }
    }

    public static void e(Throwable throwable) {
        if (sInstance == null) {
            staticLog(ERROR, null, null, throwable);
        } else {
            sInstance.log(ERROR, null, null, throwable);
        }
    }

    private static String getInformation(String tag, String msg) {
        Exception exception = new Exception();
        return "[" + ((sInstance != null && !sInstance.mIsDebug && !TextUtils.isEmpty(tag)) ?
                tag : exception.getStackTrace()[STACK_INDEX].getFileName()) + "::"
                + exception.getStackTrace()[STACK_INDEX].getLineNumber() + "] "
                + exception.getStackTrace()[STACK_INDEX].getMethodName()
                + ((sInstance != null && sInstance.mIsShowThread) ? (" @" + Thread.currentThread()) : "")
                + " >> "
                + msg;
    }

    private void delOverSaveDaysLogs() {
        if (sLogFolder == null) {
            return;
        }
        enqueue(new Runnable() {
            @Override
            public void run() {
                delOverSaveDaysLog();
            }
        });
    }

    private void enqueue(Runnable runnable) {
        if (mExecutor != null) {
            mExecutor.execute(runnable);
        }
    }

    public static void dumpProcessLog() throws IOException {
        Date today = new Date();
        String needWriteFile = FILE_NAME_FORMAT.format(today);
        int myPid = Process.myPid();
        File file = new File(sLogFolder, needWriteFile + "~pid~" + myPid + FILE_SUFFIX);
        Runtime.getRuntime().exec("logcat '*:V' -d --pid=" + myPid + " -f " + file.getPath());
        Runtime.getRuntime().exec("sync");
        for (int i = 0; i < DUMP_LOG_TIMEOUT_SECONDS; i++) {
            if (file.exists()) {
                break;
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException e) {
                FLog.e(e);
            }
        }
    }

    private void writeToFile(int level, String tag, String text) {
        try {
            if (sLogFolder == null) {
                return;
            }
            Date today = new Date();
            String tab = "    ";
            String needWriteFile = FILE_NAME_FORMAT.format(today);
            String needWriteMessage = FILE_CONTENT_FORMAT.format(today) + tab + LEVEL_LABEL[level]
                    + tab + tag + tab + text;
            File file = new File(sLogFolder, needWriteFile + "~F~Log" + FILE_SUFFIX);
            if (file.exists() && file.length() > MAX_FILE_SIZE) {
                for (int i = 1; ; i++) {
                    file = new File(sLogFolder, needWriteFile + "~F~Log." + i + FILE_SUFFIX);
                    if (file.length() < MAX_FILE_SIZE) {
                        break;
                    }
                }
            }

            FileWriter filerWriter = new FileWriter(file, true);
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (Exception e) {
            e(e);
            checkFolder(level, tag, text);
        }
    }

    private void checkFolder(int level, String tag, String text) {
        File file = new File(sLogFolder);
        if (!file.exists()) {
            boolean success = file.mkdirs();
            if (success) {
                writeToFile(level, tag, text);
            }
        }
    }

    private void delOverSaveDaysLog() {
        final Date dateBefore = getDateBefore();
        File[] files = getFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".log")) {
                    return getFileDate(filename).before(dateBefore);
                } else {
                    return true;
                }
            }
        });
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }

    private static File[] getFiles(FilenameFilter filenameFilter) {
        File destFile = new File(sLogFolder);
        if (!destFile.exists()) {
            return null;
        }
        return destFile.listFiles(filenameFilter);
    }

    private Date getDateBefore() {
        Date today = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(today);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - mLogFileSaveDays);
        return now.getTime();
    }

    private static Date getFileDate(String fileName) {
        try {
            return FILE_NAME_FORMAT.parse(fileName.split("~")[0]);
        } catch (Exception e) {
            FLog.w(e);
            return new Date();
        }
    }

    static class DateChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
                if (sInstance != null) {
                    sInstance.delOverSaveDaysLogs();
                }
            }
        }

        public void registerReceiver(Context context) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
            context.registerReceiver(this, filter);
        }
    }

    public static String getLogFolder() {
        return sLogFolder;
    }

    public static File[] getTodayFiles() {
        final Date today = new Date();
        return getFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".log")) {
                    return FILE_NAME_FORMAT.format(today).equals(FILE_NAME_FORMAT.format(getFileDate(filename)));
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * 压缩文件和文件夹
     *
     * @param srcFileString 要压缩的文件或文件夹
     * @param zipFileString 解压完成的Zip路径
     */
    private static void zipFolder(String srcFileString, String zipFileString) throws Exception {
        //创建ZIP
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFileString));
        //创建文件
        File file = new File(srcFileString);
        //压缩
        ZipFiles(file.getParent() + File.separator, file.getName(), outZip);
        //完成和关闭
        outZip.finish();
        outZip.close();
    }

    /**
     * 压缩文件
     */
    private static void ZipFiles(String folderString, String fileString, ZipOutputStream zipOutputSteam) throws Exception {
        if (zipOutputSteam == null)
            return;
        File file = new File(folderString + fileString);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }
            zipOutputSteam.closeEntry();
        } else {
            //文件夹
            String[] fileList = file.list();
            //没有子文件和压缩
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            //子文件和递归
            for (String s : fileList) {
                ZipFiles(folderString, fileString + File.separator + s, zipOutputSteam);
            }
        }
    }

    public static class Builder {
        private String mDefaultTag = "FLog";
        private boolean mIsDebug = false;
        private boolean mIsShowThread = false;
        private int mLogFileSaveDays = 7;
        private boolean mIsWriteToFile = false;
        private Context mApplication;

        public Builder(Context context) {
            mApplication = context.getApplicationContext();
        }

        public Builder setDefaultTag(String defaultTag) {
            mDefaultTag = defaultTag;
            return this;
        }

        public Builder setDebug(boolean debug) {
            mIsDebug = debug;
            return this;
        }

        public Builder setShowThread(boolean showThread) {
            mIsShowThread = showThread;
            return this;
        }

        public Builder setLogFileSaveDays(int logFileSaveDays) {
            mLogFileSaveDays = logFileSaveDays;
            return this;
        }

        public Builder setWriteToFile(boolean writeToFile) {
            mIsWriteToFile = writeToFile;
            return this;
        }

        public void init() {
            sInstance = new FLog(this);
        }
    }

    public interface OnErrorListener {
        void onError(int level, Throwable e);
    }
}
