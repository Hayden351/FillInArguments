/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fill_in_arguments.converters;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hayden Fields
 */
public class DefaultConverterTest
{
    DefaultConverter defaultConverter = new DefaultConverter();
    @Test
    public void testConvert_boolean ()
    {
        boolean booleanValue = (Boolean) defaultConverter.convert(Arrays.asList("true"));
        
        assertTrue(booleanValue);
    }
    
    @Test
    public void testConvert_integer ()
    {
        int integerValue = (int) defaultConverter.convert(Arrays.asList("123"));
        
        assertEquals(integerValue, 123);
    }
    
    @Test
    public void testConvert_float ()
    {
        float floatValue = (float) defaultConverter.convert(Arrays.asList("123.45"));
        
        assertEquals(floatValue, 123.45f, 1.1754E-38f);
    }
    
    @Test
    public void testConvert_String ()
    {
        String stringValue = (String) defaultConverter.convert(Arrays.asList("asdf"));
        
        assertEquals(stringValue, "asdf");
    }
}
