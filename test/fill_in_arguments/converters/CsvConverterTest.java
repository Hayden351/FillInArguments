package fill_in_arguments.converters;

import fill_in_arguments.FillInArguments;
import fill_in_arguments.NamedArgument;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CsvConverterTest {

    public static class Arguments
    {
        @NamedArgument(shortName="c", converter=CsvConverter.class,description = "Some test csv values.")
        List<String> csvValues = new ArrayList<>();

        @NamedArgument(converter=CsvConverter.class, shortName = "o")
        List<String> otherCsvValues = new ArrayList<>();
    }

    @Test
    public void testConvert()
    {
        Arguments arguments = FillInArguments.createAndSet(Arguments.class, new String[]{"--csvValues", "a,b,c", "-o", "1,2,3"}).parsedArguments;
        System.out.println(arguments.csvValues);
        System.out.println(arguments.otherCsvValues);
    }
}