package top.dsbbs2.mp.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import top.dsbbs2.mp.MidiPlayer;
import top.dsbbs2.mp.util.MidiUtil;

public class PlayMidiCommand implements CommandExecutor,TabCompleter {
	@Override
	public List<String> onTabComplete(CommandSender paramCommandSender, Command paramCommand, String paramString,
			String[] paramArrayOfString) {
		Lists.newArrayList(MidiPlayer.getInstance().getDataFolder().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&f.getName().contains(" "))).parallelStream().forEach(i->i.renameTo(new File(i.getParent(),i.getName().replace(" ", ""))));
		StringBuilder arg=new StringBuilder();
		for(String i : paramArrayOfString)
			arg.append(i+" ");
		String args=arg.toString().trim();
		return Lists.newArrayList(MidiPlayer.getInstance().getDataFolder().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&!f.getName().contains(" "))).parallelStream().map(File::getName).filter(i->i.toLowerCase(Locale.getDefault()).startsWith(args.toLowerCase(Locale.getDefault()))).collect(Collectors.toList());
	}

	@Override
	public boolean onCommand(CommandSender paramCommandSender, Command paramCommand, String paramString,
			String[] paramArrayOfString) {
		StringBuilder arg=new StringBuilder();
		for(String i : paramArrayOfString)
			arg.append(i+" ");
		String args=arg.toString().trim();
		if(args.isEmpty())
		{
			paramCommandSender.sendMessage("Argument midi_file is required");
			return false;
		}
		if(!new File(MidiPlayer.getInstance().getDataFolder(),args).isFile())
		{
			paramCommandSender.sendMessage("midi_file does not exist");
			return true;
		}
		if(paramCommandSender instanceof Player)
		{
			Player p=(Player)paramCommandSender;
			try {
				MidiUtil.stop(p.getUniqueId());
				MidiUtil.playMidi(new FileInputStream(new File(MidiPlayer.getInstance().getDataFolder(),args)),1.0f,p.getUniqueId(),MidiUtil.PlaySoundAble.newPlaySoundAble(p));
			} catch (Throwable e) {
				paramCommandSender.sendMessage("Error occurred\n"+throwableToString(e));
			}
		}
		return true;
	}
	public String throwableToString(Throwable e) {
		try(ByteArrayOutputStream bao=new ByteArrayOutputStream())
		{
			try(PrintStream ps=new PrintStream(bao,true,StandardCharsets.UTF_8))
			{
				e.printStackTrace(ps);
			}
			return new String(bao.toByteArray(),StandardCharsets.UTF_8);
		}catch(Throwable t) {if(t instanceof Error)throw (Error)t;else if(t instanceof RuntimeException)throw (RuntimeException)t;else throw new RuntimeException(t);}
	}
}
