import org.ini4j.Config;
import org.ini4j.Ini;
import com.google.common.io.Resources;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class IniRead {
    public HashMap<String,String> getConfig(){
        HashMap<String,String> map = new HashMap<String, String>();
        Config config = new Config();
        Ini ini = new Ini();
        ini.setConfig(config);
        try {
            ini.load(new File("config.ini"));
            Profile.Section section = ini.get("config");
            System.out.println("host:" + section.get("host"));
            map.put("host",section.get("host"));
            map.put("user",section.get("user"));
            map.put("password",section.get("password"));
            map.put("shPath",section.get("shPath"));
            map.put("output",section.get("output"));
            map.put("to",section.get("to"));
            map.put("shell",section.get("shell"));
            map.put("rm",section.get("rm"));
            map.put("preUrl",section.get("preUrl"));
            map.put("del",section.get("del"));
            map.put("html",section.get("html"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
