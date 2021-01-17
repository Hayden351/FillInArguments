package fill_in_arguments;

import fill_in_arguments.converters.Converter;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Hayden Fields
 */
public class FillInArguments
{

    /*
    What kind of behavior do we want?
    positional arguments alos known as free arguments
    and named arguments
    
    
    So positional arguments always exist
    they are the unaccounted for arguments
    
    interpret
        them as an error
        return as a list
        specify a list to assign the positional arguments
        specify a variables via an annotation to be positional arguments


        command asdf -a a -b -f 1.23 sdfg
        loop through the arguments
    
    */

    public static class ArgumentsResult<ArgumentObject>
    {
        public ArgumentObject parsedArguments;
        public Map<String, Boolean> wasArgumentFilledIn = new HashMap<>();
    }

    public static <T> T createAndFill(Class<T> cls, String[] args)
    {
        T obj = null;
        try 
        { obj = cls.getConstructor().newInstance(); }
        catch (Exception e)
        { throw new IllegalArgumentException(e); }
        
        fill(cls, obj, args, System.out);
        
        return obj;
    }
    
    public static void fill(Class<?> aClass, String[] args)
    {
        fill(aClass, null, args, System.out);
    }
    
    public static void fill(Class<?> argumentsObjectClass, Object argumentsObject, String[] arguments, PrintStream loggingStream) throws IllegalArgumentException
    {
        try
        {
            for (int i = 0; i < arguments.length; i++)
            {
                String arg = arguments[i];
                // I don't think we want position/free arguments
                // if we decide we do we can handle and continue
                if (!arg.startsWith("-")) continue;
                
                for (Field f : argumentsObjectClass.getDeclaredFields())
                {
                    // access the configuration annotation
                    NamedArgument var = f.getDeclaredAnnotation(NamedArgument.class);

                    // check to see if field is an argument field
                    if (var == null) continue;

                    // Either it matches the name of the field or it matches the short name of the field.
                    if (!((arg.startsWith("--") && arg.equals("--" + f.getName()))
                          || (arg.startsWith("-") && arg.equals("-" + var.shortName())))) continue;

                    // Get the converter from the annotation.
                    Converter converter = var.converter().newInstance();

                    // Forward parse based on the number of arguments required by the converter.
                    List<String> parameterArgs = new ArrayList<>();
                    for (int argCount = 0; argCount < converter.numberOfArguments(); argCount++)
                    {
                        // advance to the next arg
                        String parameterArg = arguments[++i];

                        // check to see if it is actually a valid argument
                        // If we need two arguments for our flag and we see --flag flagValue --otherArgument
                        // Then we should fail at the --otherArgument because we need two arguments for the flag.
                        Pattern p = Pattern.compile("^--?");
                        if (p.matcher(parameterArg).matches())
                            throw new IllegalArgumentException("Expected argument to a flag, but instead received a flag.");

                        parameterArgs.add(parameterArg);
                    }

                    f.setAccessible(true);
                    f.set(argumentsObject, converter.convert(parameterArgs));
                }
            }
        }
        catch (IllegalAccessException | InstantiationException ex)
        { // TODO: throw exception or return code?
            throw new IllegalArgumentException(ex);
        }
    }
}
