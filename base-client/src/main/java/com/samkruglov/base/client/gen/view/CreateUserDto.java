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

/** CreateUserDto */
@JsonPropertyOrder({
  CreateUserDto.JSON_PROPERTY_EMAIL,
  CreateUserDto.JSON_PROPERTY_FIRST_NAME,
  CreateUserDto.JSON_PROPERTY_LAST_NAME,
  CreateUserDto.JSON_PROPERTY_PASSWORD
})
public class CreateUserDto {
  public static final String JSON_PROPERTY_EMAIL = "email";
  private String email;

  public static final String JSON_PROPERTY_FIRST_NAME = "firstName";
  private String firstName;

  public static final String JSON_PROPERTY_LAST_NAME = "lastName";
  private String lastName;

  public static final String JSON_PROPERTY_PASSWORD = "password";
  private String password;

  public CreateUserDto email(String email) {

    this.email = email;
    return this;
  }

  /**
   * Get email
   *
   * @return email
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_EMAIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public CreateUserDto firstName(String firstName) {

    this.firstName = firstName;
    return this;
  }

  /**
   * Get firstName
   *
   * @return firstName
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_FIRST_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public CreateUserDto lastName(String lastName) {

    this.lastName = lastName;
    return this;
  }

  /**
   * Get lastName
   *
   * @return lastName
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_LAST_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public CreateUserDto password(String password) {

    this.password = password;
    return this;
  }

  /**
   * Get password
   *
   * @return password
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_PASSWORD)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateUserDto createUserDto = (CreateUserDto) o;
    return Objects.equals(this.email, createUserDto.email)
        && Objects.equals(this.firstName, createUserDto.firstName)
        && Objects.equals(this.lastName, createUserDto.lastName)
        && Objects.equals(this.password, createUserDto.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, firstName, lastName, password);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateUserDto {\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
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
