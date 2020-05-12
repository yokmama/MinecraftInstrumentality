package jp.minecraftday.minecraftinstrumentality.utils;


import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArgumentParser {
    List<ArgumentOption> options = new ArrayList<>();
    String[] args;

    public ArgumentParser(){
    }

    public ArgumentParser(String[] args){
        this.args = args;
    }

    public ArgumentParser add(ArgumentOption o){
        options.add(o);
        return this;
    }

    public ArgumentParser add(Class c){
        if(c.equals(String.class)){
            options.add(new StringArgumentOption());
        } else if(c.equals(Integer.class)) {
            options.add(new IntegerArgumentOption());
        } else if(c.equals(Double.class)) {
            options.add(new DoubleArgumentOption());
        } else if(c.equals(BlockVector.class)) {
            options.add(new BlockVectorArgumentOption());
        }

        return this;
    }

    public BlockVector getBlockVector() throws ArgumentParseException, ArgumentSizeException {
        parse(0, args);
        return (BlockVector)options.get(0).getValue();
    }

    public Object[] build() throws ArgumentParseException, ArgumentSizeException {

        parse(0, args);

        return options.stream().map(o->o.getValue()).toArray();
    }

    public Object[] parse(String[] args) throws ArgumentParseException, ArgumentSizeException {
        this.args = args;
        return build();
    }

    private void parse(int pos, String[] args) throws ArgumentParseException, ArgumentSizeException {
        if(options.size() > pos) {
            ArgumentOption option = options.get(pos);
            if (args.length >= option.getSize()) {
                String[] v = Arrays.copyOfRange(args, 0, option.getSize());
                option.setValue(v);
                parse(pos + 1, Arrays.copyOfRange(args, option.getSize(), args.length));
            } else {
                throw new ArgumentSizeException(option);
            }
        }
    }

    public interface ArgumentOption {
        void setValue(String[] args) throws ArgumentParseException;
        Object getValue();
        int getSize();

    }

    public class StringArgumentOption implements ArgumentOption {
        String value;
        public StringArgumentOption(){
        }

        @Override
        public int getSize(){ return 1;}

        @Override
        public void setValue(String[] args) {
            value = args[0];
        }

        @Override
        public Object getValue() {
            return value;
        }

    }

    public class IntegerArgumentOption implements ArgumentOption {
        int value;
        public IntegerArgumentOption(){
        }

        @Override
        public int getSize(){ return 1;}


        @Override
        public void setValue(String[] args) throws ArgumentParseException {
            try {
                value = Integer.parseInt(args[0]);
            }catch (Exception e){
                throw new ArgumentParseException(e, this);
            }
        }

        @Override
        public Object getValue() {
            return value;
        }
    }

    public class DoubleArgumentOption implements ArgumentOption {
        double value;
        public DoubleArgumentOption(){
        }

        @Override
        public int getSize(){ return 1;}


        @Override
        public void setValue(String[] args) throws ArgumentParseException {
            try {
                value = Double.parseDouble(args[0]);
            }catch (Exception e){
                throw new ArgumentParseException(e, this);
            }
        }

        @Override
        public Object getValue() {
            return value;
        }
    }

    public class BlockVectorArgumentOption implements ArgumentOption {
        BlockVector value;
        public BlockVectorArgumentOption(){
        }

        @Override
        public int getSize(){ return 3;}


        @Override
        public void setValue(String[] args) throws ArgumentParseException {
            try {
                int x = Integer.parseInt(args[0]);
                int y = Integer.parseInt(args[1]);
                int z = Integer.parseInt(args[2]);

                value = new BlockVector(x, y, z);
            }catch (Exception e){
                throw new ArgumentParseException(e, this);
            }
        }

        @Override
        public Object getValue() {
            return value;
        }
    }


    public static class ArgumentParseException extends Exception {
        ArgumentOption p;
        ArgumentParseException(Exception e, ArgumentOption p){
            super(e);
            this.p = p;
        }

        @Override
        public String getMessage() {
            return p.getClass().getName()+"????????????";
        }
    }

    public static class ArgumentSizeException extends Exception {
        ArgumentOption p;
        ArgumentSizeException(ArgumentOption p){
            this.p = p;
        }

        @Override
        public String getMessage() {
            return p.getClass().getName()+"????????????: " + p.getSize();
        }
    }
}
