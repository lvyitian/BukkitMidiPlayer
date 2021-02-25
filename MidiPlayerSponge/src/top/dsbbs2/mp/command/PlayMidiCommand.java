package top.dsbbs2.mp.command;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.collect.Lists;

import top.dsbbs2.mp.MidiPlayer;
import top.dsbbs2.mp.util.MidiUtil;

public class PlayMidiCommand implements CommandCallable {

	@Override
	public Optional<Text> getHelp(CommandSource var1) {
		return Optional.of(Text.of("/playmidi <midi_file>"));
	}

	@Override
	public Optional<Text> getShortDescription(CommandSource var1) {
		return Optional.of(Text.of("Play Midi"));
	}

	@Override
	public List<String> getSuggestions(CommandSource var1, String var2, Location<World> var3) throws CommandException {
		Lists.newArrayList(MidiPlayer.getInstance().getConfigDir().toFile().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&f.getName().contains(" "))).parallelStream().forEach(i->i.renameTo(new File(i.getParent(),i.getName().replace(" ", ""))));
		return Lists.newArrayList(MidiPlayer.getInstance().getConfigDir().toFile().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&!f.getName().contains(" "))).parallelStream().map(File::getName).filter(i->i.toLowerCase(Locale.getDefault()).startsWith(var2.toLowerCase(Locale.getDefault()))).collect(Collectors.toList());
	}

	@Override
	public Text getUsage(CommandSource var1) {
		return Text.of("/playmidi <midi_file>");
	}

	@Override
	public CommandResult process(CommandSource var1, String var2) throws CommandException {
		if (var2.isEmpty()) {
			throw new CommandException(Text.of("Argument midi_file is required"));
		}
		if(!new File(MidiPlayer.getInstance().getConfigDir().toFile(),var2).isFile())
			throw new CommandException(Text.of("midi_file does not exist"));
		if(var1 instanceof Player)
		{
			Player p=(Player)var1;
			try {
				MidiUtil.stop(p.getUniqueId());
				MidiUtil.playMidi(new FileInputStream(new File(MidiPlayer.getInstance().getConfigDir().toFile(),var2)),1.0f,p.getUniqueId(),MidiUtil.PlaySoundAble.newPlaySoundAble(p));
			} catch (Throwable e) {
				throw new CommandException(Text.of("Error occurred"), e);
			}
		}else {
			throw new CommandException(Text.of("This command can only be executed by a player"));
		}
		return CommandResult.success();
	}
	@Override
	public boolean testPermission(CommandSource var1) {
		return var1.hasPermission("midiplayer.command.play");
	}
}
