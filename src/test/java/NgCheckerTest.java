import jp.minecraftday.minecraftinstrumentality.utils.NgMatcher;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NgCheckerTest extends TestCase {

    @Test
    public void testCheck1() {
        List<String> list = new ArrayList<>(Arrays.asList(
                "死す",
                "死な",
                //"死ぬ",
                "nob",
                "氏ね",
                "死ね",
                "死の",
                "死ん",
                "ﾀﾋ",
                "殺さ",
                "殺し",
                "殺す",
                "ころす",
                "殺せ",
                "ころせ",
                "殺そ"));

        NgMatcher matcher = new NgMatcher(list.toArray(new String[0]));

        check(matcher,"nobunobu");
        check(matcher,"死ぬなって言ってもアウト");
        check(matcher,"死ぬな");
        check(matcher,"助けて");
        check(matcher,"死ね あいうえお");
        check(matcher,"死ねあいうえお");
        check(matcher,"死 ね");
        check(matcher,"おい死す");
        check(matcher,"おい氏ねよ");
        check(matcher,"氏ね っていったら どうする？");

    }

    private boolean check(NgMatcher matcher, String s){
        if(s.contains(" ")){
            String[] ss = s.split(" ");
            for(int i=0; i<ss.length; i++){
                if(check(matcher, ss[i])){
                    return true;
                }
            }
            return false;
        }
        boolean ret = matcher.match(s);
        System.out.println(s + "="+ ret);
        return ret;
    }
}
