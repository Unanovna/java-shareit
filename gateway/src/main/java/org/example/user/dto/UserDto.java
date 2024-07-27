package org.example.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.validationGroup.Create;
import org.example.validationGroup.Update;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(groups = {Create.class}, message = "Name must be filled!")
    private String name;
    @NotBlank(groups = {Create.class}, message = "Email must be filled!")
    @Email(groups = {Create.class, Update.class}, message = "Email must have the format  info@email.com!")
    private String email;
}
