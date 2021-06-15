package com.stratumn.sdk;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigTest {

    public String WORKFLOW_ID;
    public String MY_GROUP;

    public String TRACE_ID;
    public String OTHER_GROUP;
    public String ACCOUNT_API_URL;
    public String TRACE_API_URL;
    public String MEDIA_API_URL;

    // Non env dependant
    public String COMMENT_ACTION_KEY = "comment";
    public String INIT_ACTION_KEY = "init";
    public String UPLOAD_DOCUMENTS_ACTION_KEY = "uploadDocuments";
    public String IMPORT_TA_ACTION_KEY = "importTa";
    public String MY_GROUP_LABEL = "group1";
    public String OTHER_GROUP_LABEL = "group2";
    public String OTHER_GROUP_NAME = "SDKs Group 2";

    public String PEM_PRIVATEKEY_2;
    public String PEM_PRIVATEKEY;

    public ConfigTest() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();

        WORKFLOW_ID = dotenv.get("WORKFLOW_ID");
        TRACE_ID = dotenv.get("TRACE_ID");
        MY_GROUP = dotenv.get("MY_GROUP");
        OTHER_GROUP = dotenv.get("OTHER_GROUP");
        ACCOUNT_API_URL = dotenv.get("ACCOUNT_API_URL");
        TRACE_API_URL = dotenv.get("TRACE_API_URL");
        MEDIA_API_URL = dotenv.get("MEDIA_API_URL");

        // Bot 1
        PEM_PRIVATEKEY = dotenv.get("PEM_PRIVATEKEY");
        ;
        // // Bot 2
        PEM_PRIVATEKEY_2 = dotenv.get("PEM_PRIVATEKEY_2").replaceAll("\\\\n", "\n");
    }
}
