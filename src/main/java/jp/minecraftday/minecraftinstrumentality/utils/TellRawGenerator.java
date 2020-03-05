package jp.minecraftday.minecraftinstrumentality.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TellRawGenerator {
    List<TellRawGenerator.Message> texts = new ArrayList<>();

    public enum EventType {
        COMMAND,
        OEPN_URL
    }

    public static class ClickEvent {
        final EventType type;
        String text;
        public ClickEvent(String text, EventType type){
            this.text = text;
            this.type = type;
        }
    }

    class Message {
        String text;
        String hoverText;
        ClickEvent clickEvent;
        boolean italic = false;

        public Message(String text, boolean italic, String hoverText){
            this.text = text;
            this.hoverText = hoverText;
            this.clickEvent = null;
            this.italic = italic;
        }
        public Message(String text, boolean italic, ClickEvent clickEvent){
            this.text = text;
            this.hoverText = null;
            this.clickEvent = clickEvent;
            this.italic = italic;
        }
    }

    public TellRawGenerator append(String text, boolean italic, ClickEvent clickEvent){
        texts.add(new Message(text, italic, clickEvent));
        return this;
    }
    public TellRawGenerator append(String text, boolean italic, String hoverText){
        texts.add(new Message(text, italic, hoverText));
        return this;
    }
    public TellRawGenerator append(String text, boolean italic){
        return append(text, italic, (String)null);
    }
    public TellRawGenerator append(String text){
        return append(text, false, (String)null);
    }


    //["",{"text":"Gm ","italic":true},{"text":"yokmama","italic":true,"clickEvent":{"action":"open_url","value":"hello"}},{"text":" »","italic":true},{"text":" "}]
    // ["",{"text":"Gm ","italic":true},{"text":"yokmama","italic":true,"clickEvent":{"action":"run_command","value":"time set day"}},{"text":" »","italic":true},{"text":" "}]
    // ["",{"text":"Gm ","italic":true},{"text":"yokmama","italic":true,"hoverEvent":{"action":"show_text","value":"aiueo"}},{"text":" »","italic":true},{"text":" "}]
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[\"\"");
        texts.forEach(message -> {
            builder.append(",{");

                builder.append("\"text\":\"");
                builder.append(ChatColor.translateAlternateColorCodes('&', message.text));
                builder.append("\"");

                if(message.hoverText!=null){
                    builder.append(",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"");
                    builder.append(message.hoverText);
                    builder.append("\"}");
                }
                if(message.clickEvent!=null){
                    if(message.clickEvent.type == EventType.COMMAND)
                        builder.append(",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"");
                    else if(message.clickEvent.type == EventType.OEPN_URL)
                        builder.append(",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"");
                    builder.append(message.clickEvent.text);
                    builder.append("\"}");
                }
                builder.append(",\"italic\":").append(message.italic?"true":"false");

            builder.append("}");
        });
        builder.append("]");

        return builder.toString();
    }
}
