/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fill_in_arguments;

import fill_in_arguments.converters.Converter;
import fill_in_arguments.converters.DefaultConverter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import static fill_in_arguments.FillInArguments.fill;

/**
 *
 * @author Hayden Fields
 */
public class FillInArgumentsTest
{
    @NamedArgument(shortName = "x") public static int x;
    
    @PositionalArgument public static int y;
    
    @NamedArgument(shortName = "z") public static int z;

    public static class Arguments
    {
        @NamedArgument(shortName = "f") public boolean flag;
        @NamedArgument(shortName = "x") public int x;
        @NamedArgument(shortName = "y") public float y;
        @NamedArgument(shortName ="z", converter=ComplexType.ComplexTypeConverter.class) public ComplexType z;
        @PositionalArgument String value;
        @PositionalArgument float otherValue;
        @PositionalArgument int theOtherValue;

//        @NamedArgument(shortName="a") public Function aTest;
    }
    
    public static void main (String[] args)
    {
         args = new String[]{"--x", "32", "y", "123.45", "--z", "asdf", "234"};
        
        Arguments parsedArguments = FillInArguments.createAndSet(Arguments.class, args).parsedArguments;
        System.out.println(FillInArguments.createAndSet(Arguments.class, args).wasSet);

        System.out.println(FillInArguments.usage(Arguments.class));
        
        System.out.println(toString(parsedArguments));
    }
    
    
    public static class ComplexType
    {
        public static class ComplexTypeConverter implements Converter
        {

            @Override
            public ComplexType convert (List<String> args)
            {
                DefaultConverter converter = new DefaultConverter();
                ComplexType value = new ComplexType();
                value.value = (String)converter.convert(Arrays.asList(args.get(0)));
                value.sailor = (int)converter.convert(Arrays.asList(args.get(1)));
                
                return value;
            }

            @Override
            public int numberOfArguments()
            {
                return 2;
            }
        }
        
        String value;
        int sailor;
        
        public String toString() { return String.format("(%s %s)", value, sailor);}
    }


  
    
    @Test
    public void testGenerate ()
    {
        
        
        
        String[] args = new String[]{"--x", "32", "y", "123.45", "--z", "asdf", "3"};
        
        FillInArguments.ArgumentsResult<Arguments> parsedArgumentsResult = FillInArguments.createAndSet(Arguments.class, args);
        Arguments parsedArguments = parsedArgumentsResult.parsedArguments;
        
        System.out.println(parsedArguments.x);
        System.out.println(parsedArguments.y);
        System.out.println(parsedArguments.z);
    }

    public static String toString(Object obj)
    {
        StringBuilder result = new StringBuilder();
        for (Field argumentField : obj.getClass().getDeclaredFields())
            try
            {
                argumentField.setAccessible(true);
                result.append(String.format("%s=%s ", argumentField.getName(), argumentField.get(obj)));
            } catch (Exception e ) {}
        return result.toString();
    }

    @Test
    public void testFill() {
        class Arguments {
            @PositionalArgument String asdf;
            @NamedArgument(shortName = "x") int x;
            @PositionalArgument int sdfg;
        }

        Arguments arguments = new Arguments();
        System.out.println(fill(Arguments.class, arguments, new String[]{"--x", "3", "asdf", "123"}));

        System.out.println(toString(arguments));


        arguments = new Arguments();
        System.out.println(fill(Arguments.class, arguments, new String[]{"asdfasdf", "--x", "5"}));

        System.out.println(toString(arguments));
    }
    
}
