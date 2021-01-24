package fill_in_arguments;

/**
 * @author Hayden Fields
 */
public class Testing
{
    public static void main (String[] args)
    {
        System.out.println(convertPrimitive("float"));
    }
    public static String convertPrimitive(String type)
    {
        switch (type)
        {
            case "float": return Float.class.getName();
            case "int": return Integer.class.getName();
            case "boolean": return Boolean.class.getName();
        }
        return type;
    }
}
