#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.domain.repository;

import ${package}.domain.entity.UserEntity;
import io.github.luicit.luisprojectscore.domain.repository.BaseUserRepository;

public interface UserRepository extends BaseUserRepository<UserEntity> {
}
