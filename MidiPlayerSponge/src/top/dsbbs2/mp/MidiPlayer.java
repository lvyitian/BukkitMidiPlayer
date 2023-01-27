package top.dsbbs2.mp;

import java.io.File;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.common.collect.Lists;

import top.dsbbs2.mp.command.PlayMidiCommand;
import top.dsbbs2.mp.util.MidiUtil;

public class MidiPlayer extends JavaPlugin {
	private static MidiPlayer instance;
	{
		instance=this;
	}
	@Override
	public void onLoad()
	{
		if (!this.getDataFolder().isDirectory()) {
			this.getDataFolder().mkdirs();
		}
		Lists.newArrayList(MidiPlayer.getInstance().getDataFolder().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&f.getName().contains(" "))).parallelStream().forEach(i->i.renameTo(new File(i.getParent(),i.getName().replace(" ", ""))));
		this.getLogger().info("MidiPlayer loaded!");
	}
	@Override
	public void onEnable()
	{
		PluginCommand c=this.getCommand("playmidi");
		PlayMidiCommand cmd_i=new PlayMidiCommand();
		c.setExecutor(cmd_i);
		c.setTabCompleter(cmd_i);
		this.getCommand("stopmidi").setExecutor((sender,cmd,alias,args)->{
			if(sender instanceof Player)
			{
				Player p=(Player)sender;
				MidiUtil.stop(p.getUniqueId());
			}else sender.sendMessage("This command can only be executed by a player");
			return true;
		});
	}
	public static MidiPlayer getInstance()
	{
		return instance;
	}
}
