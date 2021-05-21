package stone;

import stone.ast.ASTree;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Parser {
    protected static abstract class Element{
        protected abstract void parse(Lexer lexer, List<ASTree> res) throws ParseException;
        protected abstract boolean match(Lexer lexer) throws ParseException;

    }

    protected static class Tree extends Element{
        protected Parser  parser;
        protected Tree(Parser parser){
            this.parser = parser;
        }
        @Override
        protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
            res.add(parser.parse(lexer));
        }

        @Override
        protected boolean match(Lexer lexer) throws ParseException {
            return parser.match(lexer);
        }
    }

    protected static class OrTree extends Element{

        protected Parser[] parsers;
        protected OrTree(Parser[] p){
            parsers = p;
        }

        @Override
        protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
            Parser p = choose(lexer);
            if(p == null){
                throw new ParseException(lexer.peek(0));
            } else {
                res.add(p.parse(lexer));
            }
        }

        @Override
        protected boolean match(Lexer lexer) throws ParseException {
            return choose(lexer)!= null;
        }

        protected Parser choose(Lexer lexer)throws ParseException{
            for(Parser p:parsers){
                if(p.match(lexer)){
                    return p;
                }
            }
            return null;
        }

        protected void insert( Parser p){
            Parser[] newPasers = new Parser[parsers.length+1];
            newPasers[0] = p;
            System.arraycopy(parsers,0,newPasers,1,parsers.length);
        }
    }

    protected static class Repeat extends Element{
        protected Parser parser;
        protected boolean onlyOnce;

        protected Repeat(Parser p,boolean onlyOnce){
            this.parser = p;
            this.onlyOnce = onlyOnce;
        }

        @Override
        protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
            while (parser.match(lexer)){
                ASTree t = parser.parse(lexer);
                if(t.getClass() != ASTree.class || t.numChildren()>0){
                    res.add(t);
                }
                if(onlyOnce) break;
            }
        }

        @Override
        protected boolean match(Lexer lexer) throws ParseException {
            return parser.match(lexer);
        }
    }

    protected static abstract class AToken extends Element{
        protected Factory factory;
        protected AToken(Class<? extends ASTLeaf> type){
            if(type == null)
                type = ASTLeaf.class;
            factory = Factory.get(type,Token.class);
        }

        protected void parse(Lexer lexer,List<ASTree> res) throws ParseException{
            Token t = lexer.read();
            if(test(t)){
                ASTree leaf = factory.make(t);
                res.add(leaf);
            } else {
                throw new ParseException(t);
            }
        }

        protected boolean match(Lexer lexer)throws ParseException{
            return test(lexer.peek(0));
        }

        protected abstract boolean test(Token token);
    }


    protected static class IdToken extends AToken{
        HashSet<String> reserved;
        protected IdToken(Class<? extends ASTLeaf> type,HashSet<String> r) {
            super(type);
            reserved = r!=null?r:new HashSet<String>();
        }

        @Override
        protected boolean test(Token token) {
            return token.isIdF() && !reserved.contains(token.getText());
        }
    }

    protected static class NumToken extends AToken{

        protected NumToken(Class<? extends ASTLeaf> type) {
            super(type);
        }

        @Override
        protected boolean test(Token token) {
            return token.isNumber();
        }
    }

    protected static class StrToken extends AToken{

        protected StrToken(Class<? extends ASTLeaf> type) {
            super(type);
        }

        @Override
        protected boolean test(Token token) {
            return token.isString();
        }
    }

    protected static class Leaf extends Element{
        protected String[] tokens;
        protected Leaf(String[] pat){
            tokens = pat;
        }

        @Override
        protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
            Token token = lexer.read();
            if(token.isIdF()){
                for(String st :tokens){
                    if(st.equals(token.getText())){
                        find(res,token);
                        return;
                    }
                }
            }

            if(tokens.length >0) throw new ParseException(tokens[0] +"expected ",token);
            else throw new ParseException(token);
        }

        protected void find(List<ASTree> res,Token t){
            res.add(new ASTLeaf(t));
        }

        @Override
        protected boolean match(Lexer lexer) throws ParseException {
            Token t = lexer.peek(0);
            if(t.isIdF()){
                for(String s:tokens){
                    if(s.equals(t.getText())){
                        return true;
                    }
                }
            }
            return false;
        }
    }

    protected static class Skip extends Leaf{

        protected Skip(String[] pat) {
            super(pat);
        }

        protected  void find(List<ASTree> res,Token token){}
    }

    public static class Precedence{
        int value;
        boolean leftAssoc;
        public Precedence(int v,boolean a){
            value = v;
            leftAssoc = a;
        }
    }

    public static class Operators extends HashMap<String,Precedence> {
        public static boolean LEFT = true;
        public static boolean RIGHT = false;
        public void add(String name,int prec,boolean leftAssoc){
            put(name,new Precedence(prec,leftAssoc));
        }
    }

    protected static class Expr extends Element{
        protected Factory factory;
        protected Operators ops;
        protected Parser factor;

        protected Expr(Class<? extends ASTree> clazz, Operators map, Parser exp) {
            this.factory = factory;
            this.ops = map;
            this.factor = exp;
        }


        @Override
        protected void parse(Lexer lexer, List<ASTree> res) throws ParseException {
            ASTree right = factor.parse(lexer);
            Precedence prec;
            while ((prec = nexOperator(lexer))!=null){
                right = doShift(lexer,right,prec.value);
            }
            res.add(right);
        }

        private ASTree doShift(Lexer lexer,ASTree left,int prec)throws ParseException{
            ArrayList<ASTree> list = new ArrayList<>();
            list.add(left);
            list.add(new ASTLeaf(lexer.read()));
            ASTree right = factor.parse(lexer);
            Precedence next;
            while (next == nextOperator(lexer)!=null && rightIsExpr(prec,next)){
                right = doShift(lexer,right,next.value);
            }
            list.add(right);
            return factory.make(list);
        }
        private Precedence nexOperator(Lexer lexer) throws ParseException{
            Token t = lexer.peek(0);
            if(t.isIdF()){
                return ops.get(t.getText());
            }else {
                return null;
            }
        }

        private static boolean rightIsExpr(int prec,Precedence precedence){
            if(precedence.leftAssoc){
                return prec < precedence.value;
            } else {
                return prec <= precedence.value;
            }
        }

        @Override
        protected boolean match(Lexer lexer) throws ParseException {
            return factor.match(lexer);
        }
    }

    public static final String factoryName = "create";

    protected static abstract class Factory{
        protected abstract ASTree make0(Object arg) throws Exception;

        protected ASTree make(Object arg){
            try {
                return make0(arg);
            } catch (IllegalArgumentException e1) {
                throw e1;
            } catch (Exception e2) {
                throw new RuntimeException(e2); // this compiler is broken.
            }
        }

        protected static Factory getForASTList(Class<? extends ASTree> clazz){
            Factory f = get(clazz,List.class);
            if(f == null){
                f = new Factory(){
                    protected ASTree make0(Object arg) throws Exception{
                        List<ASTree> result = (List<ASTree>)arg;
                        if(result.size()== 1){
                            return result.get(0);
                        }else {
                            return new ASTList(result);
                        }
                    }
                };
            }
            return f;
        }

        protected static Factory get(Class<? extends ASTree> clazz,Class<?> argType){
            if(clazz == null) return null;

            try {
                final Method m = clazz.getMethod(factoryName,new Class<?>[]{argType});
                return new Factory() {
                    @Override
                    protected ASTree make0(Object arg) throws Exception {
                        return (ASTree) m.invoke(null,arg);
                    }
                };
            } catch (NoSuchMethodException e) {
                try {
                    final Constructor<? extends ASTree> c = clazz.getConstructor(argType);
                    return new Factory() {
                        @Override
                        protected ASTree make0(Object arg) throws Exception {
                            return c.newInstance(arg);
                        }
                    };
                } catch (NoSuchMethodException e1) {
                       throw new RuntimeException();
                }
            }
        }

    }

    protected List<Element> elements;
    protected Factory factory;

    public Parser(Class<? extends ASTree> clazz){
        reset(clazz);
    }

    protected Parser(Parser p){
        elements = p.elements;
        factory = p.factory;
    }

    public boolean match(Lexer lexer) throws ParseException{
        if(elements.size() == 0) return true;
        else{
            Element e = elements.get(0);
            return e.match(lexer);
        }
    }

    public static Parser rule(){
        return rule(null);
    }

    public static Parser rule(Class<? extends ASTree> clazz){
        return new Parser(clazz);
    }

    public Parser reset(){
        elements = new ArrayList<Element>();
        return this;
    }

    public Parser reset(Class<? extends ASTree> clazz){
        elements = new ArrayList<>();
        factory = Factory.getForASTList(clazz);
        return this;
    }

    public Parser number(){return number(null);}

    public Parser number(Class<? extends ASTLeaf> clazz){
        elements.add(new NumToken(clazz));
        return this;
    }

    public Parser idF(HashSet<String> reserved){return idF(null,reserved);}

    public Parser idF(Class<? extends ASTLeaf> clazz,HashSet<String> reserved){
        elements.add(new IdToken(clazz,reserved));
        return this;
    }

    public Parser string(){return string(null);}

    public Parser string(Class<? extends ASTLeaf> clazz){
        elements.add(new StrToken(clazz));
        return this;
    }

    public Parser Token(String... pat){
        elements.add(new Leaf(pat));
        return this;
    }

    public Parser sep(String... pat){
        elements.add(new Skip(pat));
        return this;
    }

    public Parser ast(Parser p){
        elements.add(new Tree(p));
        return this;
    }

    public Parser Maybe(Parser p){
        Parser p2 = new Parser(p);
        p2.reset();
        elements.add(new OrTree(new Parser[]{p,p2}));
        return this;
    }

    public Parser option(Parser parser){
        elements.add(new Repeat(parser,true));
        return this;
    }

    public Parser repeat(Parser parser){
        elements.add(new Repeat(parser,false));
        return this;
    }
    public Parser experessino(Parser subexp,Operators operators){
        elements.add(new Expr(null,subexp,operators));
        return this;
    }

    public Parser expression(Class<? extends  ASTree> clazz, Parser p, Operators op){
        elements.add(new Expr(clazz,p,op));
        return this;
    }

    public Parser insertChoice(Parser p){
        Element e = elements.get(0);
        if(e instanceof OrTree){
            ((OrTree) e).insert(p);
        } else{
            Parser otherwise = new Parser(this);
            reset(null);
            or(p,otherwise);
        }
        return this;
    }
}
