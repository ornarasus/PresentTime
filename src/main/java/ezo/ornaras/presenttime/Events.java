package ezo.ornaras.presenttime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZoneId;

public class Events implements Listener {
    public static Events singleton;

    public Events(){
        singleton = this;
    }

    public int getOffset(String ip){
        try {
            URL url = new URL(String.format("http://ip-api.com/line/%s?fields=offset", ip));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream responseStream = connection.getInputStream();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8));
            return Integer.parseInt(streamReader.readLine());
        }
        catch (Exception e){
            return 0;
        }
    }

    public String newUTC(Player pl){
        int offset = getOffset(pl.getAddress().getAddress().getHostAddress());
        int hour = (int)Math.floor((float)Math.abs(offset)/60/60);
        int minute = Math.abs(offset/60)-hour*60;
        String utc = String.format("%s%02d:%02d", offset<0?"-":"+", hour, minute);
        PresentTime.singleton.db.AddPlayer(pl.getUniqueId(), utc);
        return utc;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String utc = PresentTime.singleton.db.GetUTC(e.getPlayer());
        BukkitRunnable temp = new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime totalTime = LocalTime.now(ZoneId.of("GMT"+ utc));
                double time = (totalTime.getHour()+(totalTime.getMinute()+
                        totalTime.getSecond()/60D)/60D)*1000D-6000D;
                if(time > 24000) time -= 24000;
                if(time < 0) time += 24000;
                e.getPlayer().setPlayerTime(Math.round(time), false);
            }
        };
        PresentTime.singleton.clients.put(e.getPlayer().getUniqueId(),
                temp.runTaskTimerAsynchronously(PresentTime.singleton,0L,600));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if (PresentTime.singleton.clients.containsKey(e.getPlayer().getUniqueId())){
            PresentTime.singleton.clients.get(e.getPlayer().getUniqueId()).cancel();
            PresentTime.singleton.clients.remove(e.getPlayer().getUniqueId());
        }
    }
}
