package stone.ast;

import stone.Token;

import javax.rmi.PortableRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;

public class ASTLeaf extends ASTree{
    private static ArrayList<ASTree> empty = new ArrayList<>();
    protected Token token;
    public ASTLeaf(Token t){
        token = t;
    }
    @Override
    public ASTree child(int i) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int numChildren() {
        return 0;
    }

    @Override
    public Iterator<ASTree> children() {
        return empty.iterator();
    }

    @Override
    public String location() {
        return "at line "+token.getLineNumber();
    }

    public Token token(){
        return token;
    }

    public String toString(){
        return token().getText();
    }

    @Override
    public Iterator<ASTree> iterator() {
        return children();
    }
}
