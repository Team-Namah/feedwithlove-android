package com.namah.feedwithlove;

import android.content.Context;
import android.text.TextUtils;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

public class CognitoManager {

    private static CognitoUserPool userPool;

    public static synchronized CognitoUserPool getUserPool(Context context) {

        if (userPool != null) {
            return userPool;
        }

        if (context == null) {
            throw new IllegalStateException("Context is null while initializing CognitoUserPool");
        }

        if (TextUtils.isEmpty(CognitoConfig.USER_POOL_ID)
                || TextUtils.isEmpty(CognitoConfig.CLIENT_ID)) {
            throw new IllegalStateException("CognitoConfig USER_POOL_ID or CLIENT_ID is missing");
        }

        userPool = new CognitoUserPool(
                context.getApplicationContext(),   // 🔥 IMPORTANT
                CognitoConfig.USER_POOL_ID,
                CognitoConfig.CLIENT_ID,
                null,
                Regions.AP_SOUTH_1
        );

        return userPool;
    }
}
