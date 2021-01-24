package fill_in_arguments.compiler_processor;

import fill_in_arguments.NamedArgument;
import fill_in_arguments.converters.DefaultConverter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 */
@SupportedAnnotationTypes("fill_in_arguments.NamedArgument")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class NamedArgumentProcessor extends AbstractProcessor
{
    @Override
    public boolean process (Set<? extends TypeElement> annotations, RoundEnvironment env)
    {
        Types types = processingEnv.getTypeUtils();
        Messager messager = processingEnv.getMessager();

         for (TypeElement typeElement : annotations) {
            for (Element element : env.getElementsAnnotatedWith(typeElement)) {
                NamedArgument annotation = element.getAnnotation(NamedArgument.class);
//                element.getSimpleName();
                try 
                {
                    getMyValue(annotation).accept(new SimpleTypeVisitor8<Void, Void>()
                    {
                        @Override
                        public Void visitDeclared (DeclaredType annotationsConverter, Void p)
                        {
                            // special case for default converter for convenience
                            if (DefaultConverter.class.getName().equals(annotationsConverter.toString()))
                            {
                                if (!new HashSet<>(Arrays.asList("java.lang.Float", "java.lang.Integer", "java.lang.Boolean", "java.lang.String")).contains(convertPrimitive(element.asType().toString())))
                                        messager.printMessage(Diagnostic.Kind.ERROR,
                                            String.format("Variables of type %s are not supported by the DefaultConverter."
                                                        + " Type should be int, boolean, float, or String."
                                                        + " Otherwise a custom converter can be made by implementing the %s interface", element.asType().toString(), DefaultConverter.class.getName()), 
                                            element);
                                
//                                convertPrimitive(element.asType().toString()), 
//                                                            ((ExecutableElement )types.asElement(annotationsConverter).getEnclosedElements().get(1)).getReturnType().toString()
                            }
                            else
                                // get the MirrorType of the Converter that is stored in the Converter
                                for (Element converterElements : types.asElement(annotationsConverter).getEnclosedElements())
                                    // TODO: to be really equal name, ckass, and arguments must match, I think java already checks to see if the interface is a Converter in the annotation
                                    if ("convert".equals(((ExecutableElement )converterElements).getSimpleName().toString()))
                                        if (!convertPrimitive(element.asType().toString()).
                                                equals( ((ExecutableElement )types.asElement(annotationsConverter).getEnclosedElements().get(1)).getReturnType().toString()) )
                                            messager.printMessage(Diagnostic.Kind.ERROR,
                                                String.format("Converter outputs a value of type %s which does not match the variable of type %s.", 
                                                    ((ExecutableElement )types.asElement(annotationsConverter).getEnclosedElements().get(1)).getReturnType().toString(), 
                                                    convertPrimitive(element.asType().toString())), 
                                                element);
                            return null;
                        }
                    }, null);
                }
                catch (Exception e)
                {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            e.getMessage(), element);
                }
            }
         }
        return true;
    }
    // this stuff was so confusing that I want to save anything that could be potentially useful
    // TODO: maybe I will look back on these snippets and just delete them
    //                    getMyValue(annotation).accept(new SimpleTypeVisitor8<Boolean, Void>(){
//                        @Override
//                        public Boolean visitDeclared(DeclaredType t, Void p) {
//                            messager.printMessage(Diagnostic.Kind.ERROR,
//                            String.format("%s", t), element);
//                            return !t.getTypeArguments().isEmpty();
//                        }
//                    },null);
    //                    we are compiling a compulation unit. So this will print out all 2nd level nodes of the compilation unit via a simplified ast interface
//                    Set<? extends Element> rootE=env.getRootElements();
//                    for(Element e: rootE)
//                    {
//                        for(Element subElement : e.getEnclosedElements())
//                        {
//                            messager.printMessage(Diagnostic.Kind.ERROR,
//                            "", subElement);
//                        }
//                    }    
    
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
    // https://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation
    // this is dumb
    private static TypeMirror getMyValue (NamedArgument annotation)
    {
        try // this should throw
        { annotation.converter();  }
        catch( MirroredTypeException mte )
        {
            return mte.getTypeMirror();
        }
        return null;
    }
    private TypeElement asTypeElement (TypeMirror typeMirror)
    {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement)TypeUtils.asElement(typeMirror);
    }
    
    @Override
    public SourceVersion getSupportedSourceVersion () {
        return SourceVersion.latestSupported();
    }
    
//    @Override
//    public Set<String> getSupportedAnnotationTypes () {
//        return new HashSet<>(Arrays.asList(NamedArgument.class.getName()));
//    }
}
