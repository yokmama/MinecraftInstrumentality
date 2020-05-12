import jp.minecraftday.minecraftinstrumentality.utils.ArgumentParser;
import org.bukkit.util.BlockVector;
import org.junit.Test;

import java.util.Arrays;

public class ArgumentParserTest {

    @Test
    public void testString(){
        String[] args = new String[]{"aiueo", "kakikukeko", "sasisuseso"};

        try {
            Object[] params = new ArgumentParser(args)
                    .add(String.class)
                    .add(String.class)
                    .add(String.class)
                    .build();
            System.out.println(params);
            Arrays.stream(params).forEach(System.out::println);
        } catch (ArgumentParser.ArgumentParseException e) {
            e.printStackTrace();
        } catch (ArgumentParser.ArgumentSizeException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testInt(){
        String[] args = new String[]{"aiueo", "1", "4"};

        try {
            Object[] params = new ArgumentParser(args)
                    .add(String.class)
                    .add(Integer.class)
                    .add(Integer.class)
                    .build();
            System.out.println(params);
            Arrays.stream(params).forEach(System.out::println);
        } catch (ArgumentParser.ArgumentParseException e) {
            e.printStackTrace();
        } catch (ArgumentParser.ArgumentSizeException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testVector(){
        String[] args = new String[]{"aiueo", "1", "4", "5"};

        try {
            Object[] params = new ArgumentParser(args)
                    .add(String.class)
                    .add(BlockVector.class)
                    .build();
            System.out.println(params);
            Arrays.stream(params).forEach(System.out::println);
        } catch (ArgumentParser.ArgumentParseException e) {
            e.printStackTrace();
        } catch (ArgumentParser.ArgumentSizeException e) {
            e.printStackTrace();
        }

    }

}
