package stone.ch06;

import stone.*;
import stone.ast.ASTree;
import stone.ast.NullStmnt;

public class BasicInterpreter {
    public static void main(String[] args) throws ParseException{
        run(new BasicPaser(),new BasicEnv());
    }

    public static void run(BasicPaser bp,Environment env)throws ParseException{
        Lexer lexer = new Lexer(new CodeDialog());
        while (lexer.peek(0) != Token.EOF){
            ASTree t = bp.parse(lexer);
            if(!(t instanceof NullStmnt)){
//                Object r =
            }
        }
    }
}
