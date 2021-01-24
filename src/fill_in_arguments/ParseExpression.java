package fill_in_arguments;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ParseExpression {

    public static enum NodeType
    {
        AND,
        OR,
        XOR,
        EQUAL,
        LEFT_PARENTHESIS,
        RIGHT_PARENTHESIS,
        IDENTIFIER,
        INDEX;
    }

    public static class Node
    {
        public NodeType nodeType;
        public String content;
        public int columnStart = -1;
        public int columnEnd = -1;
        public List<Node> childNodes = new ArrayList<>();

        @Override
        public String toString() {
            return "Node{" +
                    "nodeType=" + nodeType +
                    ", content='" + content + '\'' +
                    ", columnStart=" + columnStart +
                    ", columnEnd=" + columnEnd +
                    ", childNodes=" + childNodes +
                    '}';
        }

        public Node(NodeType nodeType, String content, int columnStart, int columnEnd) {
            this.nodeType = nodeType;
            this.content = content;
            this.columnStart = columnStart;
            this.columnEnd = columnEnd;
        }
    }

    public static class IdentifierNode extends Node
    {
        public String identifier;
        public IdentifierNode(NodeType nodeType, String content, int columnStart, int columnEnd, String identifier) {
            super(nodeType, content, columnStart, columnEnd);
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return "IdentifierNode{" +
                    "identifier='" + identifier + '\'' +
                    "} " + super.toString();
        }
    }
    public static class IndexNode extends Node
    {
        public int index;

        public IndexNode(NodeType nodeType, String content, int columnStart, int columnEnd, int index) {
            super(nodeType, content, columnStart, columnEnd);
            this.index = index;
        }

        @Override
        public String toString() {
            return "IndexNode{" +
                    "index=" + index +
                    "} " + super.toString();
        }
    }

    public static class BinaryExpression extends Node
    {
        public BinaryExpression(NodeType nodeType, String content, int columnStart, int columnEnd) {
            super(nodeType, content, columnStart, columnEnd);
        }
    }

    public static void main(String[] args) {
        String expression = "(--asdf)&-x&-y|0&1|--asdf";

        System.out.println(expressionToTokens(expression));
        System.out.println();
        System.out.println(expression);
        expressionToTokens(expression).forEach(System.out::println);

        Stack<Node> nodesStack = new Stack<>();
        for (Node node : expressionToTokens(expression)) {
            if (node.nodeType == NodeType.LEFT_PARENTHESIS)
                nodesStack.push(node);
            else if (node.nodeType == NodeType.RIGHT_PARENTHESIS)
            {
//                nodesStack
            }
        }

    }

    public static List<Node> expressionToTokens(String expression)
    {
        List<Node> result = new ArrayList<>();

        for (int i = 0; i < expression.length(); i++)
        {
            if (expression.charAt(i) == '&')
                result.add(new BinaryExpression(NodeType.AND, "&", i, i+1));
            else if (expression.charAt(i) == '|')
                result.add(new BinaryExpression(NodeType.OR, "|", i, i+1));
            else if (expression.charAt(i) == '^')
                result.add(new BinaryExpression(NodeType.XOR, "^", i, i+1));
            else if (expression.charAt(i) == '=')
                result.add(new BinaryExpression(NodeType.EQUAL, "=", i, i+1));
            else if (expression.charAt(i) == '(')
                result.add(new Node(NodeType.LEFT_PARENTHESIS, "(", i, i+1));
            else if (expression.charAt(i) == ')')
                result.add(new Node(NodeType.RIGHT_PARENTHESIS, ")", i, i+1));
            else if (expression.charAt(i) == '-')
            {
                int start = i;
                // We see a dash
                // Need to advance past the dash, if there is another dash ned to advance past that dash
                i++;
                if ("-".equals(peek(i, 1, expression)))
                    i++;
                StringBuilder identifier = new StringBuilder();

                int j = 0;
                for (; i + j < expression.length()
                                && Character.isJavaIdentifierPart(expression.charAt(i + j)); j++)
                    identifier.append(expression.charAt(i + j));
                if (identifier.length() == 0 || !Character.isJavaIdentifierStart(identifier.charAt(0))) throw new IllegalArgumentException();
                // Have to account for the last j++ at the end with an extra -1.
                i += j - 1;
                result.add(new IdentifierNode(NodeType.IDENTIFIER, expression.substring(start, i + 1), start, i + 1, identifier.toString()));

            }
            else if (Character.isDigit(expression.charAt(i)))
            {
                int start = i;
                StringBuilder index = new StringBuilder();
                index.append(expression.charAt(i));
                int j = 0;
                for (; i + j < expression.length()
                                && Character.isDigit(expression.charAt(i + j)); j++)
                    index.append(expression.charAt(i + j));
                i += j - 1;
                int indexValue = Integer.parseInt(index.toString());
                result.add(new IndexNode(NodeType.INDEX, expression.substring(start, i + 1), start, i + 1, indexValue));
            }

        }

        return result;
    }

    public static String peek(int currentIndex, int charactersForward, String string)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < charactersForward && currentIndex + i < string.length(); i++)
            result.append(string.charAt(currentIndex + i));
        return result.toString();
    }
}
