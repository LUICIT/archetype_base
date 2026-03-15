package com.luis_r_aguilar.baseproject.converter;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GenericConverter<E, M> {

    private final Supplier<E> entidad;
    private final Supplier<M> modelo;

    public GenericConverter(Supplier<E> entidad, Supplier<M> modelo) {
        this.entidad = entidad;
        this.modelo = modelo;
    }

    public E aEntidad(M modelo) {
        if (modelo == null) {
            return null;
        }

        E entity = entidad.get();
        copiarPropiedades(modelo, entity);
        return entity;
    }

    public M aModelo(E entidad) {
        if (entidad == null) {
            return null;
        }

        M model = modelo.get();
        copiarPropiedades(entidad, model);
        return model;
    }

    protected void copiarPropiedades(Object origen, Object destino) {
        if (origen == null || destino == null) {
            return;
        }

        try {
            BeanInfo origenInfo = Introspector.getBeanInfo(origen.getClass(), Object.class);
            BeanInfo destinoInfo = Introspector.getBeanInfo(destino.getClass(), Object.class);

            Map<String, PropertyDescriptor> propiedadesOrigen = new HashMap<>();
            for (PropertyDescriptor propertyDescriptor : origenInfo.getPropertyDescriptors()) {
                propiedadesOrigen.put(propertyDescriptor.getName(), propertyDescriptor);
            }

            for (PropertyDescriptor destinoDescriptor : destinoInfo.getPropertyDescriptors()) {
                PropertyDescriptor origenDescriptor = propiedadesOrigen.get(destinoDescriptor.getName());
                if (origenDescriptor != null) {
                    Method getter = origenDescriptor.getReadMethod();
                    Method setter = destinoDescriptor.getWriteMethod();

                    if (getter != null && setter != null) {
                        Class<?> tipoOrigen = getter.getReturnType();
                        Class<?> tipoDestino = setter.getParameterTypes()[0];

                        if (tipoDestino.isAssignableFrom(tipoOrigen)) {
                            Object valor = getter.invoke(origen);
                            setter.invoke(destino, valor);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible copiar las propiedades entre "
                    + origen.getClass().getSimpleName() + " y "
                    + destino.getClass().getSimpleName(), ex);
        }
    }
}
