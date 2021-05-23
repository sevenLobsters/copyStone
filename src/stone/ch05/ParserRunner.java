package stone.ch05;

import stone.*;
import stone.ast.ASTree;

public class ParserRunner {
    public static void main(String[] args) throws ParseException {
        Lexer l = new Lexer(new CodeDialog());
        BasicPaser bp = new BasicPaser();
        while (l.peek(0) != Token.EOF){
            ASTree ast = bp.parse(l);
            System.out.println("=> "+ast.toString());
        }
    }

}
