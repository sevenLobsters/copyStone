package stone.ch03;

import stone.CodeDialog;
import stone.Lexer;
import stone.ParseException;
import stone.Token;

public class LexerRunner {

    public static void main(String[] args){
        Lexer lexer = new Lexer(new CodeDialog());
        try {
            for(Token token;(token = lexer.read()) !=Token.EOF;){
                System.out.println(" => "+token.getText());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
