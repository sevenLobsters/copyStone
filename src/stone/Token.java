package stone;

public abstract class Token {
    public static final Token EOF = new Token(-1){};
    public static final String EOL = "\n";
    private int lineNumber;
    protected Token(int lineNumber){
        this.lineNumber = lineNumber;
    }

    public int getLineNumber(){return lineNumber;}

    public boolean isIdF(){return false;}

    public boolean isNumber(){return false;}

    public boolean isString(){return false;}

    public int getNumber(){throw new CopyStoneException("not number token");}

    public String getText(){return "";}
}
