package com.jiaoyang.base.sys;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.jiaoyang.base.util.StringEx;
import com.jiaoyang.tv.util.Logger;

public class OSUtils {
    private static final Logger LOG = Logger.getLogger(OSUtils.class);

    public static final String CMD_END = "**END**";

    /**
     * * 执行一个shell命令，并返回字符串值 * * @param cmd 命令名称&参数组成的数组（例如：{"/system/bin/cat", "/proc/version"}） * @param workingDir *
     * 命令执行路径（例如："system/bin/"） * @return 执行结果组成的字符串
     */
    synchronized public static String execShell(String[] args, String workingDir) {
        String result = null;

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            // 设置一个路径（绝对路径了就不一定需要）
            if (!StringEx.isNullOrEmpty(workingDir)) {
                processBuilder.directory(new File(workingDir));
            }
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            baos = new ByteArrayOutputStream();
            is = process.getInputStream();
            int read = -1;
            while (-1 != (read = is.read())) {
                baos.write(read);
            }
            result = new String(baos.toByteArray());
        } catch (Exception e) {
            result = null;
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    synchronized public static void runShell(String cmd, OnShellCmdExecCallback callback) {
        String line = null;
        InputStream is = null;
        // StringBuffer sb = new StringBuffer();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(cmd);

            proc.waitFor();

            is = proc.getInputStream();

            // 换成BufferedReader
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            while ((line = buf.readLine()) != null) {
                callback.onResult(cmd, line);
            }
            callback.onResult(cmd, CMD_END);
            if (is != null) {
                buf.close();
                is.close();
            }
        } catch (IOException e) {
            LOG.warn("run shell failed. err={}", e.getMessage());
        } catch (InterruptedException e) {
            LOG.warn("run shell failed. err={}", e.getMessage());
        } catch (Exception e) {
            LOG.warn("run shell failed. err={}", e.getMessage());
        }
    }

    public interface OnShellCmdExecCallback {
        public void onResult(String cmd, String result);
    }
}
