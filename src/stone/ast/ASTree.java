package stone.ast;

public abstract class ASTree implements Iterable<ASTree> {

    public abstract ASTree child(int i);

    public abstract int numChildren();

    public abstract Iterable<ASTree> children();

    public abstract String location();

    public Iterable<ASTree> iterable() {return children();}

}
