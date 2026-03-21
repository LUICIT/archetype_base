#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.web.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserModel {

    private Long id;

    @NotBlank
    @Size(min = 3, max = 90)
    private String name;

    @Size(min = 10, max = 60)
    private String username;

    @Email
    @Size(min = 10, max = 120)
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

}
