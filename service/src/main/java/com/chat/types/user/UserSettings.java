package com.chat.types.user;

import com.chat.db.Tables;
import com.chat.types.JSONWriter;

/**
 * Created by tyler on 9/23/16.
 */
public class UserSettings implements JSONWriter {
    private String defaultViewTypeRadioValue,
        defaultSortTypeRadioValue;
    private Boolean readOnboardAlert;

    public UserSettings(String defaultViewTypeRadioValue, String defaultSortTypeRadioValue, Boolean readOnboardAlert) {
        this.defaultViewTypeRadioValue = defaultViewTypeRadioValue;
        this.defaultSortTypeRadioValue = defaultSortTypeRadioValue;
        this.readOnboardAlert = readOnboardAlert;
    }

    public UserSettings() {}

    public static UserSettings create(Tables.UserView uv) {
        return new UserSettings(uv.getString("default_view_type_radio_value"),
                uv.getString("default_sort_type_radio_value"),
                uv.getBoolean("read_onboard_alert"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserSettings that = (UserSettings) o;

        if (!defaultViewTypeRadioValue.equals(that.defaultViewTypeRadioValue)) return false;
        if (!defaultSortTypeRadioValue.equals(that.defaultSortTypeRadioValue)) return false;
        return readOnboardAlert.equals(that.readOnboardAlert);

    }

    @Override
    public int hashCode() {
        int result = defaultViewTypeRadioValue.hashCode();
        result = 31 * result + defaultSortTypeRadioValue.hashCode();
        result = 31 * result + readOnboardAlert.hashCode();
        return result;
    }

    public String getDefaultViewTypeRadioValue() {
        return defaultViewTypeRadioValue;
    }

    public String getDefaultSortTypeRadioValue() {
        return defaultSortTypeRadioValue;
    }

    public Boolean getReadOnboardAlert() {
        return readOnboardAlert;
    }
}
