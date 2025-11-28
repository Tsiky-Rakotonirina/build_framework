package itu.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour mapper une URL à une méthode de contrôleur
 * Par défaut, supporte GET et POST sauf si @HttpMethod est spécifié
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Url {
    /**
     * L'URL à mapper (ex: "/employe/{id}")
     */
    String value();
}
