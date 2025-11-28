package itu.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour spécifier la méthode HTTP d'un mapping
 * Si non spécifiée, la méthode supporte à la fois GET et POST
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpMethod {
    /**
     * La méthode HTTP (GET, POST, PUT, DELETE, etc.)
     */
    String value();
}
