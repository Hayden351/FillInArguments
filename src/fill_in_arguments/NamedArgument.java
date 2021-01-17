package fill_in_arguments;

import fill_in_arguments.converters.DefaultConverter;
import fill_in_arguments.converters.Converter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Hayden Fields
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedArgument
{
    // TODO: pretty sure I can remove either this or the converter method
    public int neededParameters() default 1;

    // TODO: remove or support having different flag names
    public String shortName() default "";
    
    public String description() default "";
    
    public Class<? extends Converter> converter() default DefaultConverter.class;
}