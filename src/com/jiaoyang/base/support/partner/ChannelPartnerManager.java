package com.jiaoyang.base.support.partner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.jiaoyang.base.util.StringEx;
import com.jiaoyang.tv.util.Logger;

public class ChannelPartnerManager {
    private static final Logger LOG = Logger.getLogger(ChannelPartnerManager.class);

    public static final int COMPONENT_ID_APP_RECOMMEND = 0;
    public static final int COMPONENT_ID_AUTO_UPDATE = 1;

    public static int ChannelPartenerId = PartnerID.PARTNER_ID_SELF;
    private static String ComponentExclusion;

    public static boolean isComponentAvailable4CurrentChannelPartner(Context ctx, int cmpId) {
        boolean enable = true;
        try {
            if (StringEx.isNullOrEmpty(ComponentExclusion)) {
                ApplicationInfo appInfo = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                        PackageManager.GET_META_DATA);
                Bundle metaData = appInfo.metaData;
                ComponentExclusion = metaData.getString("exclude_comp_by_channel_partnerid");
            }
            if (!StringEx.isNullOrEmpty(ComponentExclusion)) {
                String[] items = ComponentExclusion.split(";");
                for (String exclusionItem : items) {
                    exclusionItem = exclusionItem.toUpperCase(Locale.US);
                    if (exclusionItem.startsWith("{" + cmpId + ":")
                            && exclusionItem.contains(Integer.toHexString(ChannelPartenerId))) {
                        enable = false;
                        break;
                    }
                }
            }
        } catch (NameNotFoundException e) {
            LOG.warn(e);
        }

        return enable;
    }

    public static void fetchPartnerId(Context ctx) {
        int parterId = 0;
        String fileName = "appconfig/PartnerInfo.txt";
        InputStream in = null;
        BufferedReader br = null;

        try {
            in = ctx.getAssets().open(fileName);
            br = new BufferedReader(new InputStreamReader(in));
            String firstLine = br.readLine();
            String[] items = firstLine.split(",");
            if (items != null && items.length == 2) {
                String partnerName = items[0];
                if (!StringEx.isNullOrEmpty(items[1])) {
                    String hexString = items[1].toUpperCase(Locale.US);
                    hexString = hexString.substring(hexString.indexOf("0X") + 2);
                    ChannelPartenerId = Integer.parseInt(hexString, 16);
                    LOG.info("get partnerId from {}. name={} id={}", fileName, partnerName, parterId);
                }
            }
        } catch (IOException e) {
            LOG.warn("get partnerId failed. err={}", e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.warn(e);
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.warn(e);
                }
            }
        }
    }
}
