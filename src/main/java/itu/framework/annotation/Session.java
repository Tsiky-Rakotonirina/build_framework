package itu.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour injecter la session HTTP dans un paramètre de type Map&lt;String, Object&gt;
 * Un seul paramètre annoté @Session est autorisé par méthode.
 * 
 * <p>Le paramètre injecté est un {@link itu.framework.web.SessionMap} qui maintient
 * une synchronisation bidirectionnelle avec HttpSession :</p>
 * <ul>
 *   <li>Toute modification (put, remove, clear) est immédiatement répercutée dans HttpSession</li>
 *   <li>Toute lecture (get, containsKey) lit directement depuis HttpSession</li>
 * </ul>
 * 
 * <p>Exemple d'utilisation :</p>
 * <pre>
 * {@literal @}Url("/login")
 * {@literal @}HttpMethod("POST")
 * public ModelView login(
 *     {@literal @}RequestParameter(key = "username") String username,
 *     {@literal @}Session Map&lt;String, Object&gt; session
 * ) {
 *     // Stocke directement dans HttpSession
 *     session.put("user", username);
 *     session.put("role", "user");
 *     // ...
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Session {
}
