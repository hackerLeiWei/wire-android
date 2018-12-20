/**
 * Wire
 * Copyright (C) 2018 Wire Swiss GmbH
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.Nullable;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.waz.ServerConfig;
import com.waz.model.AccentColor;
import com.waz.service.BackendConfig;
import com.waz.service.assets.AssetServiceParams;
import com.waz.zclient.controllers.IControllerFactory;
import com.waz.zclient.ui.text.TypefaceFactory;
import com.waz.zclient.ui.text.TypefaceLoader;
import com.waz.zclient.utils.CerUtils;
import com.waz.zclient.utils.WireLoggerTree;
import com.waz.znet.ServerTrust;

import timber.log.Timber;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ZApplication extends WireApplication implements ServiceContainer {

    static {
        String domain = "accounttest";
        String circleDomain = "http://testm.pichat.im/";////朋友圈baseUrl 正式服 "https://m.isecret.im/"    测试服 "http://testm.pichat.im/"

        ServerConfig.setParams(domain, "isecret.im");
        BackendConfig.initFirebase("837746899989", "1:837746899989:android:9a79fed81c66ba94", "AIzaSyCRILafi4IYct-5uk4JYgcGqWbPXUWLJPE");
        ServerTrust.setParams(ServerTrust.TLS_V_1_2(), CerUtils.isecretCA_TrustIntArr());
        AssetServiceParams.setSaveImageDirName("Secret");//设置网络库存储图片相册名称
    }

    private static final String FONT_FOLDER = "fonts";

    private TypefaceLoader typefaceloader = new TypefaceLoader() {

        private Map<String, Typeface> typefaceMap = new HashMap<>();

        @Override
        public Typeface getTypeface(String name) {
            if (name == null || "".equals(name)) {
                return null;
            }

            if (typefaceMap.containsKey(name)) {
                return typefaceMap.get(name);
            }

            try {
                Typeface typeface;
                if (name.equals(getString(R.string.wire__glyphs)) ||
                    name.equals(getString(R.string.wire__typeface__redacted))) {
                    typeface = Typeface.createFromAsset(getAssets(), FONT_FOLDER + File.separator + name);
                } else if (name.equals(getString(R.string.wire__typeface__thin))) {
                    typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL);
                } else if (name.equals(getString(R.string.wire__typeface__light))) {
                    typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
                } else if (name.equals(getString(R.string.wire__typeface__regular))) {
                    typeface = Typeface.create("sans-serif", Typeface.NORMAL);
                } else if (name.equals(getString(R.string.wire__typeface__medium))) {
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
                } else if (name.equals(getString(R.string.wire__typeface__bold))) {
                    typeface = Typeface.create("sans-serif", Typeface.BOLD);
                } else {
                    Timber.e("Couldn't load typeface: %s", name);
                    return Typeface.DEFAULT;
                }

                typefaceMap.put(name, typeface);
                return typeface;
            } catch (Throwable t) {
                Timber.e(t, "Couldn't load typeface: %s", name);
                return null;
            }
        }
    };

    public static ZApplication from(@Nullable Activity activity) {
        return activity != null ? (ZApplication) activity.getApplication() : null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //
    //  LifeCycle
    //
    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate() {
        super.onCreate();

        setLogLevels();
        AndroidThreeTen.init(this);
        TypefaceFactory.getInstance().init(typefaceloader);

        // refresh
        AccentColor.setColors(AccentColor.loadArray(getApplicationContext(), R.array.original_accents_color));
    }

    public static void setLogLevels() {
        Timber.uprootAll();
        if (BuildConfig.DEVELOPER_FEATURES_ENABLED) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new WireLoggerTree());
        }
    }

    @Override
    public IControllerFactory getControllerFactory() {
        return controllerFactory();
    }

}
