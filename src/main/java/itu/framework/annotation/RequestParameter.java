package itu.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour mapper un paramètre de méthode à un paramètre HTTP avec un nom personnalisé
 * Exemple: public String method(@RequestParameter(key="user_id") String userId)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParameter {
    /**
     * Le nom du paramètre HTTP à récupérer via request.getParameter()
     */
    String key();
}
