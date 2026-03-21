#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.domain.entity;

import io.github.luicit.luisprojectscore.domain.entity.BaseUserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "users_username_un", columnNames = "username"),
        @UniqueConstraint(name = "users_email_un", columnNames = "email")
})
public class UserEntity extends BaseUserEntity {
}
