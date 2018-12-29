package net.towpod;

import android.provider.BaseColumns;

/**
 * Created by WebMaster on 1/30/17.
 */

public class UserContract {
    public static abstract class UserEntry implements BaseColumns {
        public static final String TABLE_NAME ="user.db";

        public static final String usermail = "email";
        public static final String password = "password";
    }
}
