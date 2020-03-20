import jdk.nashorn.internal.runtime.regexp.RegExpFactory;
import jdk.nashorn.internal.runtime.regexp.joni.MatcherFactory;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import jp.minecraftday.minecraftinstrumentality.utils.KanaConverter;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KanaConverterTest {

    @Test
    public void testConveConv1(){
        String text = "aiueo";
        String kana = new KanaConverter().convert(text);

        assert(kana.equals("あいうえお"));
    }

    @Test
    public void testConveConv2(){
        String text = "\'aiueo\'";
        String kana = new KanaConverter().convert(text);

        //assert(kana.equals("aiueo"));
    }

    @Test
    public void testConveConv3(){
        String text = "ettmitai";
        String kana = new KanaConverter().convert(text);

        //assert(kana.equals("えっみたい"));
    }

    @Test
    public void testRegx(){
        String str = "e-to '/warp lobby' de moto no 'machi' ni kaware masu. atoha 'tphere'  mo iidesu";
        Pattern pattern = Pattern.compile("'(.*?)'");

        Matcher m = pattern.matcher(str);

        System.out.println(m.find());
        do {
            System.out.println("一致した部分は : " + m.group());
        }while((m.find()));
    }
}
