package com.jiaoyang.tv.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.codec2.digest.DigestUtils;

import android.content.Context;

import com.jiaoyang.base.util.FileUtility;
import com.jiaoyang.tv.data.Properties;
import com.jiaoyang.tv.util.Util;
import com.jiaoyang.tv.util.Logger;

public class ModuleManager {
    private static final Logger LOG = Logger.getLogger(ModuleManager.class);

    public final static String LIB_NAME = "libdownloadengine.so";
    public final static String LIB_PROPERTIES_NAME = "libdownloadengine.so.properties";

    private static String mLoadingLibDir;
    private static String mUpdateLibDir;
    private static Properties mCurrentProperties;

    public static void init(Context context) {
        mLoadingLibDir = context.getFilesDir().getPath() + "/libs_loading/";
        mUpdateLibDir = context.getFilesDir().getPath() + "/libs_update/";
        FileUtility.createDirectoryIfNotExist(mLoadingLibDir);
        FileUtility.createDirectoryIfNotExist(mUpdateLibDir);

        LOG.debug("loading lib dir is {}.", mLoadingLibDir);
        LOG.debug("update lib dir is {}.", mUpdateLibDir);

        File loadingLibFile = new File(mLoadingLibDir, LIB_NAME);
        File loadingPropertiesFile = new File(mLoadingLibDir, LIB_PROPERTIES_NAME);
        File updateLibFile = new File(mUpdateLibDir + LIB_NAME);
        File updatePropertiesFile = new File(mUpdateLibDir + LIB_PROPERTIES_NAME);

        boolean firstLaunch = Util.isFirstLaunch(context);
        LOG.debug("it {} the first launch.", firstLaunch ? "is" : "isn't");
        // 覆盖安装时删除原来的so
        if (firstLaunch) {
            loadingLibFile.delete();
            loadingPropertiesFile.delete();
            updateLibFile.delete();
            updatePropertiesFile.delete();
        }

        if (updateLibFile.exists() && updatePropertiesFile.exists()) {
            LOG.debug("update lib and properties exists.");

            Properties updateProperties = getProperties(updatePropertiesFile);
            if (!loadingLibFile.exists() || !loadingPropertiesFile.exists()) {
                LOG.debug("loading lib or properties not exists.");

                copyUpdatedLibs(loadingLibFile, loadingPropertiesFile, updateLibFile, updatePropertiesFile,
                        updateProperties);
                mCurrentProperties = updateProperties;
            } else {
                LOG.debug("loading lib and properties exists.");

                mCurrentProperties = getProperties(loadingPropertiesFile);
                if (!compareMd5(mCurrentProperties, updateProperties)) {
                    copyUpdatedLibs(loadingLibFile, loadingPropertiesFile, updateLibFile, updatePropertiesFile,
                            updateProperties);
                    mCurrentProperties = updateProperties;
                }
            }
        } else {
            LOG.debug("update lib or properties not exists.");

            if (loadingLibFile.exists() && loadingPropertiesFile.exists()) {
                LOG.debug("loading lib and properties exists.");

                mCurrentProperties = getProperties(loadingPropertiesFile);
                if (!verifyLibMd5(loadingLibFile, mCurrentProperties)) {
                    copyAssetsLibs(context);
                    mCurrentProperties = getProperties(loadingPropertiesFile);
                }
            } else {
                LOG.debug("loading lib or properties not exists.");

                copyAssetsLibs(context);
                mCurrentProperties = getProperties(loadingPropertiesFile);
            }

            // 避免用户手动删除其中一个文件导致运行不正常
            updateLibFile.delete();
            updatePropertiesFile.delete();
        }

        LOG.debug("the current version of downloadengine is {}", mCurrentProperties.version);
    }

    public static String getLoadingLibPath() {
        return mLoadingLibDir + LIB_NAME;
    }

    public static String getUpdateLibPath() {
        return mUpdateLibDir + LIB_NAME;
    }

    public static String getUpdatePropertiesPath() {
        return mUpdateLibDir + LIB_PROPERTIES_NAME;
    }

    public static Properties getProperties() {
        return mCurrentProperties;
    }

    private static boolean copyAssetsLibs(Context context) {
        LOG.debug("copy assets libs.");

        boolean success = false;
        try {
            Util.copy(context.getAssets().open("libs/" + LIB_NAME), new FileOutputStream(mLoadingLibDir + LIB_NAME));
            Util.copy(context.getAssets().open("libs/" + LIB_PROPERTIES_NAME), new FileOutputStream(mLoadingLibDir +
                    LIB_PROPERTIES_NAME));

            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    private static boolean copyUpdatedLibs(File loadingLib, File loadingProperties,
            File updateLib, File updateProperties, Properties properties) {
        boolean success = false;

        if (verifyLibMd5(updateLib, properties)) {
            try {
                Util.copy(new FileInputStream(updateLib), new FileOutputStream(loadingLib));
                Util.copy(new FileInputStream(updateProperties), new FileOutputStream(loadingProperties));

                success = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            updateLib.delete();
            updateProperties.delete();
        }

        LOG.debug("copy update libs {}.", success ? "success" : "failed");
        return success;
    }

    private static boolean compareMd5(Properties loadingProperties, Properties updateProperties) {
        boolean result = false;

        if (loadingProperties != null && updateProperties != null
                && loadingProperties.md5.equalsIgnoreCase(updateProperties.md5)) {
            result = true;
        }

        LOG.debug("compare md5. result={}.", result);
        return result;
    }

    private static boolean verifyLibMd5(File lib, Properties properties) {
        boolean result = false;
        if (properties != null) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(lib);
                String libMd5 = DigestUtils.md5Hex(fis);

                LOG.debug("verify lib md5. libPath={}, libMd5={}", lib.getPath(), libMd5);
                fis.close();

                if (properties.md5.equalsIgnoreCase(libMd5)) {
                    result = true;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        LOG.debug("verify lib md5. result={}", result);
        return result;
    }

    private static Properties getProperties(File file) {
        Properties properties = null;
        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);

            String[] verItems = br.readLine().split(":");
            String[] md5Items = br.readLine().split(":");
            LOG.debug("get properties. version={}, md5={}", verItems[1], md5Items[1]);

            if (verItems != null && verItems.length == 2
                    && md5Items != null && md5Items.length == 2) {
                properties = new Properties();
                properties.version = verItems[1].trim();
                properties.md5 = md5Items[1].trim();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return properties;
    }
}
