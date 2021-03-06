package com.rnfingerprint;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private boolean selfCancelled;
    private Context mAppContext;

    private final FingerprintManager mFingerprintManager;
    private final Callback mCallback;

    public FingerprintHandler(Context context, FingerprintManager fingerprintManager, Callback callback) {
        mAppContext = context;
        mFingerprintManager = fingerprintManager;
        mCallback = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        return (android.os.Build.VERSION.SDK_INT >= 23)
                && mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startAuth(FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();
        selfCancelled = false;
        mFingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    public void endAuth() {
        cancelAuthenticationSignal();
    }

    public void dialogWasDetached() {
        selfCancelled = true;
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        if (!selfCancelled) {
            mCallback.onError(errString.toString()); 
        }
    }

    @Override
    public void onAuthenticationFailed() {
        mCallback.onError("failed"); 
        selfCancelled = true;
        cancelAuthenticationSignal(); 
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        
        mCallback.onAuthenticated();
        cancelAuthenticationSignal();
    }

    private void cancelAuthenticationSignal() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    public interface Callback {
        void onAuthenticated();
        void onError(String errorString);
        void onCancelled();
    }
}
