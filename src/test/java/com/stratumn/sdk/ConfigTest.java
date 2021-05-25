package com.stratumn.sdk;

public class ConfigTest {

    // Env dependant
    // FIXME : set these in semaphore ?
    public static final String WORKFLOW_ID = "812";
    public static final String MY_GROUP = "4822";
    public static final String TRACE_ID = "f1c1d4fa-961e-4b2c-9519-ccbda4ecc2d4";
    public static final String OTHER_GROUP = "4823";

    public static final String ACCOUNT_STAGING_URL = "https://account-api.staging.stratumn.com";
    public static final String TRACE_STAGING_URL = "https://trace-api.staging.stratumn.com";
    public static final String MEDIA_STAGING_URL = "https://media-api.staging.stratumn.com";

    // Non env dependant
    public static final String COMMENT_ACTION_KEY = "comment";
    public static final String INIT_ACTION_KEY = "init";
    public static final String UPLOAD_DOCUMENTS_ACTION_KEY = "uploadDocuments";
    public static final String IMPORT_TA_ACTION_KEY = "importTa";
    // Bot 1
    public static final String PEM_PRIVATEKEY = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRAP7BEfm6Smg9h3mmOM3zayeAyPk4/VvT927NN5Y8e\nsgqwoZr++UHatd9r9cg2NZvCleMojySIsLKQpZYEwr21uw==\n-----END ED25519 PRIVATE KEY-----\n";
    // Bot 2
    public static final String PEM_PRIVATEKEY_2 = "-----BEGIN ED25519 PRIVATE KEY-----\nMFACAQAwBwYDK2VwBQAEQgRAtMoOToj7bv+A+7dOrM5UyG2buHgsSu0OriTJfv7/\nEqKUzdjgxvAvTtOA7RCIY1/FoDWjHZ/wG5hPcA3Bj3BRkQ==\n-----END ED25519 PRIVATE KEY-----\n";
    public static final String MY_GROUP_LABEL = "group1";
    public static final String OTHER_GROUP_LABEL = "group2";
    public static final String OTHER_GROUP_NAME = "SDKs Group 2";
}
