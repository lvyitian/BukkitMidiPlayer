package top.dsbbs2.mp;

import java.io.File;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import top.dsbbs2.mp.command.PlayMidiCommand;
import top.dsbbs2.mp.util.MidiUtil;

@Plugin(id="midiplayer",name="MidiPlayer",version="0.1",authors="lvyitian")
public class MidiPlayer {
	@Inject
	private Game game;
	@Inject
	private Logger logger;
	@Inject
	@ConfigDir(sharedRoot=false)
	private Path configDir;
	@Inject
	private PluginContainer plugin;
	private static MidiPlayer instance;
	{
		instance=this;
	}
	@Listener
	public void init(GameInitializationEvent e)
	{
		if (!configDir.toFile().isDirectory()) {
			configDir.toFile().mkdirs();
		}
		Lists.newArrayList(MidiPlayer.getInstance().getConfigDir().toFile().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&f.getName().contains(" "))).parallelStream().forEach(i->i.renameTo(new File(i.getParent(),i.getName().replace(" ", ""))));
		game.getServiceManager().getRegistration(PermissionService.class).map(i->i.getProvider().newDescriptionBuilder(plugin)).ifPresent(builder->{
			builder.id("midiplayer.command.play")
		       .description(Text.of("Allows the user to execute the playmidi command."))
		       .assign(PermissionDescription.ROLE_USER, true)
		       .register();
			builder.id("midiplayer.command.stop")
		       .description(Text.of("Allows the user to execute the stopmidi command."))
		       .assign(PermissionDescription.ROLE_USER, true)
		       .register();
		});
		Sponge.getCommandManager().register(plugin, new PlayMidiCommand(), "playmidi");
		Sponge.getCommandManager().register(plugin, CommandSpec.builder().description(Text.of("Stop playing midi")).permission("midiplayer.command.stop").executor((source,arg)->{
			if(source instanceof Player)
			{
				Player p=(Player)source;
				MidiUtil.stop(p.getUniqueId());
			}else {
				throw new CommandException(Text.of("This command can only be executed by a player"));
			}
			return CommandResult.success();
		}).build(), "stopmidi");
		logger.info("MidiPlayer loaded!");
	}
	public PluginContainer getPlugin()
	{
		return this.plugin;
	}
	public Game getGame()
	{
		return this.game;
	}
	public Logger getLogger() 
	{
		return this.logger;
	}
	public Path getConfigDir()
	{
		return this.configDir;
	}
	public static MidiPlayer getInstance()
	{
		return instance;
	}
}
