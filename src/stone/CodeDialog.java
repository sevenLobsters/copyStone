package stone;

import javax.swing.*;
import java.io.*;

public class CodeDialog extends Reader {
    private String buf = null;
    private int pos = 0;

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if(buf == null){
            String in = showDialog();
            if(in == null) return -1;
            else {
                print(in);
                buf = in+"\n";
                pos = 0;
            }
        }
        int size = 0;
        int length = buf.length();
        while (pos < length && size<len){
            cbuf[off+size++] = buf.charAt(pos++);
        }

        if(pos == length) buf = null;
        return size;
    }

    public void print(String s){
        System.out.println(s);
    }

    @Override
    public void close() throws IOException {

    }

    public String showDialog(){
        JTextArea ta = new JTextArea(20,40);
        JScrollPane sp = new JScrollPane(ta);
        int reslult = JOptionPane.showOptionDialog(null,sp,"输入",JOptionPane.OK_CANCEL_OPTION
                ,JOptionPane.PLAIN_MESSAGE,null,null,null);
        if(reslult == JOptionPane.OK_OPTION){
            return ta.getText();
        } else {
            return null;
        }
    }

    public static Reader file() throws FileNotFoundException {
        JFileChooser chooser = new JFileChooser();
        if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            return new BufferedReader(new FileReader(chooser.getSelectedFile()));
        }else {
            throw  new FileNotFoundException("没有文件");
        }
    }
}
