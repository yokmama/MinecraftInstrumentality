package jp.minecraftday.minecraftinstrumentality.utils;

import java.util.HashMap;
import java.util.Map;

public class NgMatcher {

    private String[] words;
    private String word;
    private int state;

    public NgMatcher(String[] words){
        this.words = words;
    }

    public String getWord() {
        return word;
    }

    public String getMaskString(String msg) {
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<word.length(); i++) builder.append("#");

        return  msg.replaceAll(word, builder.toString());
    }

    public int getState() {
        return state;
    }

    public boolean match(String target){
        state = 0;
        word = null;
        for(int i=0; i<words.length; i++){
            if(target.equals(words[i])){
                state = 1;
                word = words[i];
                return true;
            }
            else if(target.contains(words[i])){
                state = 2;
                word = words[i];
                return true;
            }
        }
        return false;
    }
}
