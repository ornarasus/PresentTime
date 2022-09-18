package ezo.ornaras.presenttime.presenttime;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalTime;
import java.time.ZoneId;

public class Events implements Listener {

    PresentTime main;

    public Events(PresentTime main){
        this.main = main;
    }

    public JSONObject getRequest(String ip, String key) throws IOException {
        URL url = new URL(String.format("https://api.ipdata.co/%s/time_zone?api-key=%s", ip, key));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("accept", "application/json");
        InputStream responseStream = connection.getInputStream();
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);
        JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
        return jsonObject;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String utc = main.getConfig().getString(e.getPlayer().getUniqueId().toString(), "");
        if (utc == "") {
            try {
                String token = main.getConfig().getString("key","6ebadc82026d6ded06b32a8d7afa545aa430fbc81770146473c45197");
                JSONObject model = getRequest(e.getPlayer().getAddress().getAddress().getHostAddress(),token);
                String[] tempUTC = model.getString("current_time").split(":");
                utc = tempUTC[2].substring(2)+":"+tempUTC[3];
                main.getConfig().set(e.getPlayer().getUniqueId().toString(), utc);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        String finalUtc = utc;
        BukkitRunnable temp = new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime totalTime = LocalTime.now(ZoneId.of("GMT"+ finalUtc));
                double time = (totalTime.getHour()+(totalTime.getMinute()+
                        totalTime.getSecond()/60D)/60D)*1000D-6000D;
                if(time > 24000) time -= 24000;
                if(time < 0) time += 24000;
                long r = Math.round(time);
                e.getPlayer().setPlayerTime(r, false);
            }
        };
        main.clients.put(e.getPlayer().getUniqueId(), temp.runTaskTimerAsynchronously(main,0L,10L));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if (main.clients.containsKey(e.getPlayer().getUniqueId())){
            main.clients.get(e.getPlayer().getUniqueId()).cancel();
            main.clients.remove(e.getPlayer().getUniqueId());
        }
    }
}
