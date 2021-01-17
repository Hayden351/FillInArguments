package fill_in_arguments.converters;

import java.util.ArrayList;
import java.util.List;

public class CsvConverter implements Converter
{
    @Override
    public List<String> convert(List<String> args) {
        String csvArgument = args.get(0);
        List<String> result = new ArrayList<>();
        for (String csvValue : csvArgument.split("\\s+,\\s+"))
            result.add(csvValue);
        return result;
    }

    @Override
    public int numberOfArguments() {
        return 1;
    }
}
