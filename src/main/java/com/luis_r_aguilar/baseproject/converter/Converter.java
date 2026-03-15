package com.luis_r_aguilar.baseproject.converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Conversor base entre entidades y modelos.
 *
 * <p>Se implementa con funciones para evitar obligar a las clases hijas
 * a sobreescribir métodos cuando no se desea. Si en algún caso se requiere
 * lógica adicional, la clase hija puede seguir sobreescribiendo los métodos.</p>
 */
public class Converter<E, M> {

    private final Function<M, E> toEntityFunction;
    private final Function<E, M> toModelFunction;
    private final BiFunction<E, M, E> updateEntityFunction;

    public Converter(
            Function<M, E> toEntity,
            Function<E, M> toModel
    ) {
        this(toEntity, toModel, (entity, model) -> toEntity.apply(model));
    }

    public Converter(
            Function<M, E> toEntityFunction,
            Function<E, M> toModelFunction,
            BiFunction<E, M, E> updateEntityFunction
    ) {
        this.toEntityFunction = Objects.requireNonNull(toEntityFunction, "toEntityFunction is required");
        this.toModelFunction = Objects.requireNonNull(toModelFunction, "toModelFunction is required");
        this.updateEntityFunction = Objects.requireNonNull(updateEntityFunction, "updateEntityFunction is required");
    }

    public E toEntity(M model) {
        return toEntityFunction.apply(model);
    }

    public M toModel(E entity) {
        return toModelFunction.apply(entity);
    }

//    public E updateEntity(E entity, M model) {
//        return updateEntityFunction.apply(entity, model);
//    }

    public List<E> toEntityList(List<M> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }

        return models.stream()
                .filter(Objects::nonNull)
                .map(this::toEntity)
                .toList();
    }

    public List<M> toModelList(List<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toModel)
                .toList();
    }

}
