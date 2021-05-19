package stone;

import stone.ast.ASTree;

public class CopyStoneException extends RuntimeException {

    public CopyStoneException(String s){super(s);}
    public CopyStoneException(ASTree tree,String s){super(tree.location()+"  "+s);}

}
