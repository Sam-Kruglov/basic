/*
 * Base App API
 * Responsible for managing users and roles.   Following [semantic versioning](https://semver.org/).
 *
 * The version of the OpenAPI document: 1.0.0-SNAPSHOT
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.samkruglov.base.client.gen.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/** ChangePasswordDto */
@JsonPropertyOrder({
  ChangePasswordDto.JSON_PROPERTY_NEW_PASSWORD,
  ChangePasswordDto.JSON_PROPERTY_OLD_PASSWORD
})
public class ChangePasswordDto {
  public static final String JSON_PROPERTY_NEW_PASSWORD = "newPassword";
  private String newPassword;

  public static final String JSON_PROPERTY_OLD_PASSWORD = "oldPassword";
  private String oldPassword;

  public ChangePasswordDto newPassword(String newPassword) {

    this.newPassword = newPassword;
    return this;
  }

  /**
   * Get newPassword
   *
   * @return newPassword
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_NEW_PASSWORD)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public ChangePasswordDto oldPassword(String oldPassword) {

    this.oldPassword = oldPassword;
    return this;
  }

  /**
   * Get oldPassword
   *
   * @return oldPassword
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_OLD_PASSWORD)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getOldPassword() {
    return oldPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChangePasswordDto changePasswordDto = (ChangePasswordDto) o;
    return Objects.equals(this.newPassword, changePasswordDto.newPassword)
        && Objects.equals(this.oldPassword, changePasswordDto.oldPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(newPassword, oldPassword);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ChangePasswordDto {\n");
    sb.append("    newPassword: ").append(toIndentedString(newPassword)).append("\n");
    sb.append("    oldPassword: ").append(toIndentedString(oldPassword)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
