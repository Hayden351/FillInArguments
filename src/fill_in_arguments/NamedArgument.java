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
    // Short name is required because no default name would make sense.
    public String shortName();
    
    public String description() default "";

    public boolean isRequired() default false;

    public String[] mutuallyExclusive() default {};
    
    public Class<? extends Converter> converter() default DefaultConverter.class;
}