package com.luis_r_aguilar.baseproject.domain.repository;

import com.luis_r_aguilar.baseproject.domain.entity.BaseUserEntity;

import java.util.Optional;

public interface BaseUserRepository extends DatabaseRepository<BaseUserEntity, Long> {

    Optional<BaseUserEntity> findByEmailAndDeletedAtIsNull(String email);
    Optional<BaseUserEntity> findByUsernameAndDeletedAtIsNull(String username);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByUsernameAndDeletedAtIsNull(String username);

}
