package stone;


import stone.ast.*;


import java.util.HashSet;

import static stone.Parser.rule;

public class BasicPaser {
    HashSet<String> reserved = new HashSet<>();
    Parser.Operators operators = new Parser.Operators();
    Parser expr0 = rule();
    Parser primary = rule(PrimaryExpr.class)
            .or(rule().sep("(").ast(expr0).sep(")"),
                    rule().number(NumberLiteral.class),
                    rule().idF(Name.class, reserved),
                    rule().string(StringLiteral.class));
    Parser factor = rule().or(rule(NegativeExpr.class).sep("-").ast(primary),
            primary);
    Parser expr = expr0.expression(BinaryExpr.class, factor, operators);

    Parser statement0 = rule();
    Parser block = rule(BlockStmnt.class)
            .sep("{").option(statement0)
            .repeat(rule().sep(";", Token.EOL).option(statement0))
            .sep("}");
    Parser simple = rule(PrimaryExpr.class).ast(expr);
    Parser statement = statement0.or(
            rule(IfStmnt.class).sep("if").ast(expr).ast(block)
                    .option(rule().sep("else").ast(block)),
            rule(WhileStmnt.class).sep("while").ast(expr).ast(block),
            simple);

    Parser program = rule().or(statement, rule(NullStmnt.class))
            .sep(";", Token.EOL);

     public BasicPaser(){
         reserved.add(";");
         reserved.add("}");
         reserved.add(Token.EOL);

         operators.add("=", 1, Parser.Operators.RIGHT);
         operators.add("==", 2, Parser.Operators.LEFT);
         operators.add(">", 2, Parser.Operators.LEFT);
         operators.add("<", 2, Parser.Operators.LEFT);
         operators.add("+", 3, Parser.Operators.LEFT);
         operators.add("-", 3, Parser.Operators.LEFT);
         operators.add("*", 4, Parser.Operators.LEFT);
         operators.add("/", 4, Parser.Operators.LEFT);
         operators.add("%", 4, Parser.Operators.LEFT);
     }
    public ASTree parse(Lexer lexer) throws ParseException {
        return program.parse(lexer);
    }
}
