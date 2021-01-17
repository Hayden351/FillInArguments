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
public @interface PositionalArgument
{
    public String description() default "Used for something!";
    
    // allows you to define an ordering on positional arguments without changing their location in source code
    public int order() default 0;
    
    // converts the input that comes in as a String to the variable
    public Class<? extends Converter> converter() default DefaultConverter.class;
}
