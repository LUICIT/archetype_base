package com.luis_r_aguilar.baseproject.domain.repository;

import com.luis_r_aguilar.baseproject.domain.entity.AuditableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface DatabaseRepository<E extends AuditableEntity<I>, I extends Serializable>
        extends JpaRepository<E, I>, JpaSpecificationExecutor<E> {

    @Transactional
    default void softDelete(E entity) {
        if (entity == null) {
            return;
        }
        entity.setDeletedAt(Instant.now());
        save(entity);
    }

    @Transactional
    default void softDeleteById(I id) {
        findById(id).ifPresent(entity -> {
            entity.setDeletedAt(Instant.now());
            save(entity);
        });
    }

    @Transactional
    default void softDeleteAll(Iterable<? extends E> entities) {
        if (entities == null) {
            return;
        }
        Instant now = Instant.now();
        for (E entity : entities) {
            if (entity == null) {
                continue;
            }
            entity.setDeletedAt(now);
            save(entity);
        }
    }

    Optional<E> findByIdAndDeletedAtIsNull(I id);

    List<E> findAllByDeletedAtIsNull();
}
