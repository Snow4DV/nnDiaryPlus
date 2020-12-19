package com.example.govDiary;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.sender.HttpSender;

import java.security.AlgorithmParameterGenerator;

import static org.acra.ReportField.*;

@AcraCore(buildConfigClass = BuildConfig.class, reportContent = { ANDROID_VERSION,
        APP_VERSION_CODE,
        APP_VERSION_NAME,
        PACKAGE_NAME,
        REPORT_ID,
        STACK_TRACE,
        USER_APP_START_DATE,
        USER_CRASH_DATE })

@AcraHttpSender(uri = "https://collector.tracepot.com/a98beefc",
        httpMethod = HttpSender.Method.POST)
public class DiaryApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
}
