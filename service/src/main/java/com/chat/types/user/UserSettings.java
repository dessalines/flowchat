package com.chat.types.user;

import com.chat.db.Tables;
import com.chat.types.JSONWriter;

/**
 * Created by tyler on 9/23/16.
 */
public class UserSettings implements JSONWriter {
  private String defaultViewTypeRadioValue, defaultSortTypeRadioValue, defaultCommentSortTypeRadioValue;
  private Boolean readOnboardAlert;
  private Theme theme;

  public UserSettings(String defaultViewTypeRadioValue, String defaultSortTypeRadioValue,
      String defaultCommentSortTypeRadioValue, Boolean readOnboardAlert, Theme theme) {
    this.defaultViewTypeRadioValue = defaultViewTypeRadioValue;
    this.defaultSortTypeRadioValue = defaultSortTypeRadioValue;
    this.defaultCommentSortTypeRadioValue = defaultCommentSortTypeRadioValue;
    this.readOnboardAlert = readOnboardAlert;
    this.theme = theme;
  }

  public UserSettings() {
  }

  public static UserSettings create(Tables.UserSetting uv) {

    return new UserSettings(ViewType.values()[uv.getInteger("default_view_type_id") - 1].getRadioValue(),
        SortType.values()[uv.getInteger("default_sort_type_id") - 1].getRadioValue(),
        CommentSortType.values()[uv.getInteger("default_comment_sort_type_id") - 1].getRadioValue(),
        uv.getBoolean("read_onboard_alert"),
        Theme.values()[uv.getInteger("theme")]
        );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    UserSettings that = (UserSettings) o;

    if (!defaultViewTypeRadioValue.equals(that.defaultViewTypeRadioValue))
      return false;
    if (!defaultSortTypeRadioValue.equals(that.defaultSortTypeRadioValue))
      return false;
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

  public String getDefaultCommentSortTypeRadioValue() {
    return defaultCommentSortTypeRadioValue;
  }

  public Boolean getReadOnboardAlert() {
    return readOnboardAlert;
  }

  public Integer getTheme() {
    return theme.ordinal();
  }
}
