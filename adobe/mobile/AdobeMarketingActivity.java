package com.adobe.mobile;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import com.appdynamics.eumagent.runtime.BuildInfo;
import com.appdynamics.eumagent.runtime.InstrumentationCallbacks;

public abstract class AdobeMarketingActivity extends Activity {
    static {
        try {
            if (!BuildInfo.f86appdynamicsGeneratedBuildId_ec052cfb07624749a4976b491e549053) {
                BuildInfo.f86appdynamicsGeneratedBuildId_ec052cfb07624749a4976b491e549053 = true;
            }
        } catch (Throwable unused) {
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        InstrumentationCallbacks.dispatchTouchEventCalled(this, motionEvent);
        return super.dispatchTouchEvent(motionEvent);
    }

    public void onConfigurationChanged(Configuration configuration) {
        InstrumentationCallbacks.onConfigurationChangedCalled(this, configuration);
        super.onConfigurationChanged(configuration);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        InstrumentationCallbacks.onDestroyCalled(this);
        super.onDestroy();
    }

    /* access modifiers changed from: protected */
    public void onRestart() {
        InstrumentationCallbacks.onRestartCalled(this);
        super.onRestart();
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        InstrumentationCallbacks.onStartCalled(this);
        super.onStart();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        InstrumentationCallbacks.onStopCalled(this);
        super.onStop();
    }

    public void onCreate(Bundle bundle) {
        InstrumentationCallbacks.onCreateCalled(this, bundle);
        super.onCreate(bundle);
        Config.setContext(getApplicationContext());
    }

    public void onPause() {
        InstrumentationCallbacks.onPauseCalled(this);
        super.onPause();
        Config.pauseCollectingLifecycleData();
    }

    public void onResume() {
        InstrumentationCallbacks.onResumeCalled(this);
        super.onResume();
        Config.collectLifecycleData(this);
    }
}
