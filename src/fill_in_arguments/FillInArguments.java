package fill_in_arguments;

import fill_in_arguments.converters.Converter;
import fill_in_arguments.converters.DefaultConverter;

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
    positional arguments/free arguments
    and named arguments
    
    Either unaccounted for arguments are errors or they can be plopped into variables.
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

    /**
     * Holds an filled in instance of type <ArgumentObject> and a map containing the name of the fields in <ArgumentObject>
     * and true if the variable was set/filled in and false otherwise.
     *
     * @param <ArgumentObject> A class containing some instance variables annotated with one of the following annotations:
     * @See fill_in_arguments.NamedArgument
     * @See fill_in_arguments.PositionalArgument
     */
    public static class ArgumentsResult<ArgumentObject>
    {
        public ArgumentObject parsedArguments;
        public Map<String, Boolean> wasSet = new HashMap<>();

        public ArgumentsResult(ArgumentObject parsedArguments, Map<String, Boolean> wasSet) {
            this.parsedArguments = parsedArguments;
            this.wasSet = wasSet;
        }
    }

    /**
     * Create an instance of the arguments object, fill it in and return a map
     *
     * @param argumentsClass The class object to create a new instance.
     * @param args The command line arguments as a string array.
     * @param <ArgumentObject> The arguments object that defines the command line options via annotations.
     * @return The filled in object and a map between the object instance variable field names and whether they were filled in or not.
     */
    public static <ArgumentObject> ArgumentsResult<ArgumentObject> createAndSet(Class<ArgumentObject> argumentsClass, String[] args)
    {
        ArgumentObject argumentsObject = null;
        try 
        { 
            argumentsClass.getConstructor().setAccessible(true);
            argumentsObject = argumentsClass.getConstructor().newInstance(); }
        catch (Exception e)
        { throw new IllegalArgumentException(e); }

        Map<String, Boolean> wasSet = fill(argumentsClass, argumentsObject, args);
        return new ArgumentsResult<>(argumentsObject, wasSet);
    }

    public static <ArgumentObject> String usage(Class<ArgumentObject> argumentsObjectClass)
    {
        StringBuilder usage = new StringBuilder();

        for (Field argumentField : argumentsObjectClass.getDeclaredFields())
        {
            NamedArgument namedArgumentAnnotation = argumentField.getDeclaredAnnotation(NamedArgument.class);

            if (namedArgumentAnnotation != null)
            {
                // Put in reference for long and short name of the named argument variable.
                try
                {
                    if (isBooleanFlag(argumentField, namedArgumentAnnotation.converter()))
                        usage.append(String.format("-%s --%s: A boolean flag.\n    Required: %s\n    Description: %s\n",
                                namedArgumentAnnotation.shortName(), argumentField.getName(),
                                namedArgumentAnnotation.isRequired(),
                                namedArgumentAnnotation.description()));
                    else
                        usage.append(String.format("-%s --%s: Requires %s parameter for converter %s.\n    Required: %s\n    Description: %s\n",
                                namedArgumentAnnotation.shortName(), argumentField.getName(), namedArgumentAnnotation.converter().newInstance().numberOfArguments(), namedArgumentAnnotation.converter().getName(),
                                namedArgumentAnnotation.isRequired(),
                                namedArgumentAnnotation.description()));
                } catch(Exception e) { throw new IllegalArgumentException(e);}
            }
        }

        List<Field> positionalArguments = new ArrayList<>();
        for (Field argumentField : argumentsObjectClass.getDeclaredFields())
            if (argumentField.getDeclaredAnnotation(PositionalArgument.class) != null)
                positionalArguments.add(argumentField);

        Collections.sort(positionalArguments,
                Comparator.comparingInt((Field field) -> field.getDeclaredAnnotation(PositionalArgument.class).order()));

        for (Field positionalArgument : positionalArguments)
        {
            PositionalArgument positionalArgumentAnnotation = positionalArgument.getDeclaredAnnotation(PositionalArgument.class);
            try
            {
                usage.append(String.format("%s: Is positional argument using converter %s.\n    Description: %s\n",
                        positionalArgument.getName(), positionalArgumentAnnotation.converter().getName(),
                        positionalArgumentAnnotation.description()));
            } catch(Exception e) { throw new IllegalArgumentException(e);}
        }



        return usage.toString();
    }

    /**
     * Fill in static variables of the given class.
     * @param aClass A class with static variables annotated with one of the following annotations:
     *        @See fill_in_arguments.NamedArgument
     *        @See fill_in_arguments.PositionalArgument
     * @param args
     * @return
     */
    public static Map<String, Boolean> fill(Class<?> aClass, String[] args)
    {
        return fill(aClass, null, args);
    }

    /**
     *
     * @param argumentsObjectClass
     * @param argumentsObject
     * @param arguments
     * @throws IllegalArgumentException
     */
    public static Map<String, Boolean> fill(Class<?> argumentsObjectClass, Object argumentsObject, String[] arguments) throws IllegalArgumentException
    {
        Map<String, Field> fieldForName = new HashMap<>();
        List<Field> positionalArguments = new ArrayList<>();
        // LinkedHashMap so that insertion order is maintained.
        Map<String, Boolean> wasSet = new LinkedHashMap<>();

        for (Field argumentField : argumentsObjectClass.getDeclaredFields())
        {
            NamedArgument namedArgumentAnnotation = argumentField.getDeclaredAnnotation(NamedArgument.class);
            PositionalArgument positionalArgumentAnnotation = argumentField.getDeclaredAnnotation(PositionalArgument.class);
            if (namedArgumentAnnotation != null)
            {
                // Put in reference for long and short name of the named argument variable.
                if (namedArgumentAnnotation.shortName() != null
                        && !namedArgumentAnnotation.shortName().isEmpty())
                    fieldForName.put("-" + namedArgumentAnnotation.shortName(), argumentField);
                fieldForName.put("--" + argumentField.getName(), argumentField);
                wasSet.put(argumentField.getName(), false);
            }
            else if (positionalArgumentAnnotation != null)
            {
                positionalArguments.add(argumentField);
                wasSet.put(argumentField.getName(), false);
            }
        }
        // Sort by order, if the orders are all equal then should be declaration order.
        // The jvm doesn't guarantee that reflection will return fields in declaration order, but in this case
        // the jvm does.
        // Should be in declaration order because Collections.sort uses a stable merge sort variant.
        // This is dependent on the having a java standard library where Collections.sort has a stable sort.
        Collections.sort(positionalArguments,
                Comparator.comparingInt((Field field) -> field.getDeclaredAnnotation(PositionalArgument.class).order()));

        int positionalArgumentIndex = 0;
        try
        {
            for (int i = 0; i < arguments.length; i++)
            {
                String arg = arguments[i];
                // I don't think we want position/free arguments
                // if we decide we do we can handle and continue

                Field annotatedArgument = null;
                Converter converter = null;
                List<String> parameterArgs = new ArrayList<>();
                if (!arg.startsWith("-"))
                {
                    if (positionalArgumentIndex >= positionalArguments.size()) throw new IllegalArgumentException(String.format("Found %s free arguments, when at most %s were expected.", positionalArgumentIndex + 1, positionalArguments.size()));
                    annotatedArgument = positionalArguments.get(positionalArgumentIndex);
                    PositionalArgument positionalArgumentAnnotation = annotatedArgument.getDeclaredAnnotation(PositionalArgument.class);
                    converter = positionalArgumentAnnotation.converter().newInstance();
                    parameterArgs = Arrays.asList(arg);
                    positionalArgumentIndex++;
                }
                else
                {
                    annotatedArgument = fieldForName.get(arg);
                    NamedArgument namedArgumentAnnotation = annotatedArgument.getDeclaredAnnotation(NamedArgument.class);
                    converter = namedArgumentAnnotation.converter().newInstance();

                    // Special case for booleans, if type of field is boolean and we are using default converter then
                    // treat the presence of the argument
                    if (isBooleanFlag(annotatedArgument, namedArgumentAnnotation.converter()))
                    {
                        // In some cases we would want to toggle the boolean flag instead of always setting it to true.
                        // Would be specifically in teh case where the default value is set to true.
                        if (annotatedArgument.get(argumentsObject) != null)
                            annotatedArgument.set(argumentsObject, !(boolean)annotatedArgument.get(argumentsObject));
                        else annotatedArgument.set(argumentsObject, true);
                        wasSet.put(annotatedArgument.getName(), true);
                        continue;
                    }

                    // Forward parse based on the number of arguments required by the converter.
                    for (int argCount = 0; argCount < converter.numberOfArguments(); argCount++)
                    {
                        String parameterArg = arguments[++i];
                        // If we need two arguments for our flag and we see --flag flagValue --otherArgument
                        // Then we should fail at the --otherArgument because we need two arguments for the flag.
                        if (Pattern.compile("^--?").matcher(parameterArg).matches()) throw new IllegalArgumentException("Expected argument to a flag, but instead received a flag (A string that begins with a dash).");
                        parameterArgs.add(parameterArg);
                    }
                }
                annotatedArgument.set(argumentsObject, converter.convert(parameterArgs));
                wasSet.put(annotatedArgument.getName(), true);
            }
        }
        catch (IllegalAccessException | InstantiationException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        return wasSet;
    }

    public static boolean isBooleanFlag(Field annotatedArgument, Class<? extends Converter> converterClass) {
        return (boolean.class.equals(annotatedArgument.getType()) || Boolean.class.equals(annotatedArgument.getType()))
                && DefaultConverter.class.equals(converterClass);
    }
}
