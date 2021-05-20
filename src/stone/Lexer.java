package stone;

import com.sun.imageio.plugins.png.PNGImageReader;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    public static String rg
            = "\\s*((//.*)|([0-9]+)|(\"(\\\\\"|\\\\\\\\|\\\\n|[^\"])*\")"
            + "|[A-Z_a-z][A-Z_a-z0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct})?";
    private Pattern pattern = Pattern.compile(rg);
    private ArrayList<Token> queue = new ArrayList<>();
    private boolean hasMore;
    private LineNumberReader reader;

    public Lexer(Reader reader){
        hasMore = true;
        this.reader = new LineNumberReader(reader);
    }

    public Token read() throws ParseException{
        if(fillQueue(0)){
            return queue.remove(0);
        } else {
            return Token.EOF;
        }
    }

    public Token peek(int i )throws ParseException{
        if(fillQueue(i)){
            return queue.get(i);
        } else {
            return Token.EOF;
        }
    }

    private boolean fillQueue(int i ) throws ParseException{
        while (i>=queue.size()){
            if(hasMore){
                readLine();
            } else {
                return false;
            }
        }
        return true;
    }

    protected void readLine() throws ParseException{
        String line = null;

        try {
            line = reader.readLine();

        } catch (IOException e) {
            throw new ParseException("ioexception");
        }
        if(line == null){
            hasMore = false;
            return;
        }

        int lineNo = reader.getLineNumber();
        Matcher matcher = pattern.matcher(line);
        matcher.useTransparentBounds(true).useAnchoringBounds(true);
        int pos = 0;
        int endPos = line.length();
        while (pos < endPos){
            matcher.region(pos,endPos);
            if(matcher.lookingAt()){
                addToken(lineNo,matcher);
                pos = matcher.end();
            } else{
                throw new ParseException("bad token at line "+lineNo);
            }
        }
        queue.add(new IdToken(lineNo,Token.EOL));
    }

    protected void addToken(int no,Matcher matcher){
        String m = matcher.group(1);
        if(m!= null){
            if(matcher.group(2)==null){
                Token token;
                if(matcher.group(3)!=null){
                    token = new NumToken(no,Integer.parseInt(m));
                } else if(matcher.group(4)!= null){
                    token = new StrToken(no,toStringLiteral(m));
                } else {
                    token = new IdToken(no,m);
                }
                queue.add(token);
            }
        }
    }

    protected String toStringLiteral(String s){
        StringBuilder sb = new StringBuilder();
        int len = s.length()-1;
        for(int i = 1;i<len;i++){
            char c = s.charAt(i);
            if(c == '\\'&& i+1< len){
                char c2 = s.charAt(i+1);
                if(c2 == '"'||c2=='\\'){
                    c=s.charAt(++i);
                } else if(c2=='n'){
                    ++i;
                    c = '\n';
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    protected static class IdToken extends Token{
        private String text;

        protected IdToken(int lineNumber,String id) {
            super(lineNumber);
            text = id;
        }

        @Override
        public boolean isIdF() {
            return true;
        }

        public String getText(){
            return text;
        }
    }

    protected static class NumToken extends Token{
        private int num;

        protected NumToken(int lineNumber,int num) {
            super(lineNumber);
            this.num = num;
        }

        @Override
        public boolean isNumber() {
            return true;
        }

        @Override
        public int getNumber() {
            return num;
        }

        @Override
        public String getText() {
            return Integer.toString(num);
        }
    }

    private static class StrToken extends Token{
        private String str;

        protected StrToken(int lineNumber,String s) {
            super(lineNumber);
            this.str = s;
        }

        @Override
        public boolean isString() {
            return true;
        }

        @Override
        public String getText() {
            return str;
        }
    }
}
