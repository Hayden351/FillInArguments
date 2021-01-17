package fill_in_arguments.converters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author Hayden Fields
 * 
 * default matcher will try to match arg against a boolean, integer, float
 * else will consider it to be a string
 */
public class DefaultConverter implements Converter
{
    public static Pattern boolPattern = scannerPattern("boolPattern");
    public static Pattern integerPattern = scannerPattern("integerPattern");
    public static Pattern floatPattern = scannerPattern("floatPattern");
    
     // This is so dumb
    public static Pattern scannerPattern(String pattern)
    {
        Pattern p = null;
        try
        {
            Method m = Scanner.class.getDeclaredMethod(pattern);
            m.setAccessible(true);
            p = (Pattern)m.invoke(new Scanner(""));
        }
        catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        { ; }
        return p;
    }
     
     @Override
    public Object convert (List<String> args)
    {
        if (args.isEmpty())
            return true;
        String arg = args.get(0);

        if (boolPattern.matcher(arg).matches())
            return Boolean.parseBoolean(arg);
        else if (integerPattern.matcher(arg).matches())
            return Integer.parseInt(arg);
        else if (floatPattern.matcher(arg).matches())
            return Float.parseFloat(arg);

        return arg;
    }

    @Override public int numberOfArguments () { return 1; }
}