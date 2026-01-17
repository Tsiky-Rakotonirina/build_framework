package itu.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour indiquer qu'une méthode nécessite un rôle spécifique
 * Peut prendre plusieurs rôles séparés par des virgules
 * Exemple: @Role("admin, dg")
 * Fonctionne uniquement si roleAttribute est défini dans web.xml
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Role {
    String value();
}
