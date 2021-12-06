package sh.reece.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Main implements Listener, CommandExecutor {

	private static Main plugin;
	private FileConfiguration trainbackup, testing;
	private String FILENAME;
	private static Boolean playerOnAreaSoRunCommand, isTestingBreak;
	private static int movingRunnable, doorsRunnable;
	private int speed;

	// Locations of blocks from train
	private static ArrayList<Location> train = new ArrayList<Location>();

	// Queue to get all blocks from select command
	private ArrayList<Location> saveNewTrain = new ArrayList<Location>();

	// testing, saves blocks to config which I break so I can do stuff with them.
	//  mainly making a bunch of train doors
	private List<String> testingBlocks = new ArrayList<String>();
	
	// config file plugijn.XXXX found in EVNTMinigames.jar

	public Main(Main instance) {
		plugin = instance;

		FILENAME = "newTrainSchem.yml";
		plugin.createFile(FILENAME);
		trainbackup = plugin.getConfigFile(FILENAME);	
		trainbackup.addDefault("BACKUP", new ArrayList<String>());

		plugin.getConfig().addDefault("trainspeed", 2);
		speed = plugin.getConfig().getInt("trainspeed");

		plugin.getCommand("train").setExecutor(this);
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		playerOnAreaSoRunCommand = false;
		isTestingBreak = false;
		
		plugin.createFile("test.yml");
		testing = plugin.getConfigFile("test.yml");
		

	}


	public void selectionLocations(Player p) {		
		// used to save an object as a schem thing in YML.

		if(saveNewTrain.size() == 0) {
			saveNewTrain.add(p.getLocation());
			p.sendMessage("Run this command again to get the 2nd location");
		} else {
			saveNewTrain.add(p.getLocation());
			List<String> s = getTrainBlocks(saveNewTrain.get(0), saveNewTrain.get(1));

			trainbackup.set("NEW_TRAIN", s);
			plugin.saveConfig(trainbackup, "newTrainSchem.yml");
			p.sendMessage("New Train saved to newTrainSchem.yml! Copy to config.yml");
			saveNewTrain.clear();

		}

	}

	public void initalSpawn() {
		for(String item : plugin.getConfig().getStringList("TRAIN")) {				
			Location l = locationFromString(item);
			Material mat = getMaterialFromConfig(item);
			train.add(l);			
			l.getBlock().setType(mat);			
		}
	}


	public void startTrain() {
		// runs the train and moves it forward.

		stopTrain();		
		initalSpawn(); // loads in blocks from the config file, adds to train List

		//Util.coloredBroadcast("&a[!] TRAIN CHEW CHEW!");
		movingRunnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			int TimesRun = 0;

			public void run() {  
				//Util.coloredBroadcast("Run: " + TimesRun + "&c!");

				for(int i=0;i<train.size();i++) {

					Location initLoc = train.get(i);

					// if it has moved X blocks, stop moving it
					if(TimesRun >= 67) {		
						//Util.coloredBroadcast("IT SHOULD STOP AFTER THIS!!!");
						Bukkit.getScheduler().cancelTask(movingRunnable);
						
						
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								openDoors(initLoc.getWorld()); 
							}}, 30L);
						return;
					}					


					Material mat = initLoc.getBlock().getType();
					initLoc.getBlock().setType(Material.AIR);
					Location newLoc = initLoc.add(0,0,-1);
					newLoc.getBlock().setType(mat);
					train.set(i, newLoc);
				} 
				TimesRun+=1;
			}
		}, 2*20L, speed);
	}


	public void openDoors(World w) {
		Location doorL = new Location(w, 106, 19, -1256);
		Location doorR = new Location(w, 106, 19, -1257);

		// future update, add runnables to add delays to all of these? or is that too much
		
		// clearing doorway
		for(int z=0;z<49;z+=6) {
			for(int y=-1;y<4;y++) {
				doorL.getBlock().getLocation().clone().subtract(0, y, z).getBlock().setType(Material.AIR);	
				doorR.getBlock().getLocation().clone().subtract(0, y, z).getBlock().setType(Material.AIR);
			}
		}
	}

	// MANUAL STUFF HERE FOR THE DOORS
//		Location doorsOpening = new Location(w, 106, 20, -1255);
//		int x=106;
//		// walls when the doors open
//		for(int mZ=0;mZ<=48;mZ+=6) {
//			for(int z=0;z<=3;z+=3) {
//				for(int y=0;y<=5;y++) {
//
//					if(y==0 || y==5) {
//						x = 0;
//					} else {x=-1;}
//					doorsOpening.getBlock().getLocation().clone().subtract(x, y, z+mZ).getBlock().setType(Material.RED_CONCRETE);	
//				}
//			}
//		}
//		
//		// glass through each door thing
//		Location glassLvl = new Location(w, 107, 18, -1255);
//		for(int z=0;z<=52;z+=3) {
//			glassLvl.getBlock().getLocation().clone().subtract(x, 0, z).getBlock().setType(Material.BLACK_STAINED_GLASS);
//		}
//		
//		
//		Location pathIn = new Location(w, 106, 15, -1256);
//		for(int mZ=0;mZ<=48;mZ+=6) { // repeating over
//			for(int x2=0;x2>=-3;x2-=1) {
//				for(int z=0;z<=1;z+=1) {
//					pathIn.getBlock().getLocation().clone().subtract(x+x2, 0, z+mZ).getBlock().setType(Material.LIGHT_GRAY_CONCRETE);
//				}
//			}
//		}
//		
//		Location stepUp = new Location(w, 110, 16, -1256);
//		for(int mZ=0;mZ<=48;mZ+=6) { // repeating over
//			for(int z=0;z<=1;z+=1) {
//				stepUp.getBlock().getLocation().clone().subtract(0, 0, z+mZ).getBlock().setType(Material.LIGHT_GRAY_CONCRETE);
//			}
//		}
//		
//		// change this location to inside the train
//		// doors are open, allow when player steps through to run command
//		playerOnAreaSoRunCommand = true;
//
//	}
//	
//	@EventHandler
//	public void teleportPlayer(PlayerMoveEvent e) {		
//		if(playerOnAreaSoRunCommand == true) {
//			Player p = e.getPlayer();
//			Location ploc = p.getLocation();
//			
//			if(ploc.clone().subtract(0, 1, 0).getBlock().getType() == Material.WHITE_CONCRETE) {
//				Location loc2 = new Location(ploc.getWorld(), 104, 16, -1256); // be larger than other
//				Location loc1 = new Location(ploc.getWorld(), 101, 14, -1309);
//				
//				int px = p.getLocation().getBlockX();
//		        int pz = p.getLocation().getBlockZ();
//		        if((px >= loc1.getBlockX() && px <= loc2.getBlockX()) && (pz >= loc1.getBlockZ() && pz <= loc2.getBlockZ())) {  
//		        	// puts them inside the train as they await TP.		        			
//		        	//p.teleport(Zones.jail);
//					Util.coloredMessage(p, "&4(!) Teleporting to jail...");
//		        	Util.console("warpjail " + p.getName());
//		        	
//		        	return;
//		        } 
//			}
//		}
//	}
	
	@EventHandler
	public void testing(BlockBreakEvent e) {		
		
		if(isTestingBreak == true) {
			Block b = e.getBlock();
			//Material mat = b.getType();
			Location l = b.getLocation();
			
			String location = locationToString(l);
			Util.consoleMSG(location);
			
			testingBlocks.add(location);
			
		} 
		
	}
	
	

	public static void removeDoors(World w) {
		// path way to station
		for(int x=106;x<=110;x++) {
			new Location(w, x, 15, -1280).getBlock().setType(Material.AIR);
			new Location(w, x, 15, -1281).getBlock().setType(Material.AIR);	
		}

		// top thing overhead
		for(int z=-1279;z>=-1282;z--) {
			new Location(w, 106, 20, z).getBlock().setType(Material.AIR);
		}
	}


	public static void stopTrain() {
		Bukkit.getScheduler().cancelTask(movingRunnable);
		Bukkit.getScheduler().cancelTask(doorsRunnable);
		playerOnAreaSoRunCommand = false;
		if(train.size() > 0) {
			World w = train.get(0).getWorld();
			removeDoors(w);
			
			// clear all door path bvlock stuff
			List<String> doorsExtra = getTrainBlocks(new Location(w, 110, 14, -1255), new Location(w, 106, 20, -1307));
			
			for(String l : doorsExtra) {
				locationFromString(l).getBlock().setType(Material.AIR);	
			} 
			
		}
		for(Location l : train) {
			l.getBlock().setType(Material.AIR);	
		} 

		train.clear();
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {		
		if(!sender.hasPermission("train.use")) {
			Util.coloredMessage((Player) sender, "&cNo permissions to ride the trainnn");
			return true;
		}
		if (args.length == 0) {
			Util.coloredMessage((Player) sender, "&b/train &f<start, stop>");
			Util.coloredMessage((Player) sender, "&b/train &f<select> &7- Used for creating a new train.");
			return true;
		}

		switch(args[0]){
		case "start":
		case "go":
			startTrain();
			return true;
		case "spawn":
			initalSpawn();
			return true;
		case "stop":
		case "clear":
			stopTrain();
			return true;
			
		
		case "reload":
			Util.console("plugman reload EVNTMinigames");
			return true;
		}

		Player p = (Player) sender;

		switch(args[0]){
		case "select":
			selectionLocations(p);
			return true;
		case "testing":
			isTestingBreak = !isTestingBreak;
			Util.coloredMessage((Player) sender, "Test breaking writes to config: " + isTestingBreak);
			return true;
			
		case "savetesting":
			Util.consoleMSG(testingBlocks.toString());
			testing.set("TEST_BLOCKS_BROKEN", testingBlocks);
			plugin.saveConfig(testing, "test.yml");
			return true;	
			
		default:
			return true;		
		}

	}

	public static List<String> getTrainBlocks(Location loc1, Location loc2){
		List<String> blocks = new ArrayList<String>();
		int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
		int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX()); 
		int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
		int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY()); 
		int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
		int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ()); 
		for(int x = bottomBlockX; x <= topBlockX; x++){
			for(int z = bottomBlockZ; z <= topBlockZ; z++){
				for(int y = bottomBlockY; y <= topBlockY; y++){

					Block b = loc1.getWorld().getBlockAt(x, y, z);
					// if block is not air or nothing, save to config
					if(!b.isEmpty()) { // if not air, cave air, etc
						String l = locationToString(b.getLocation());                       
						blocks.add(l);
					}                	                    
				}
			}
		}       
		return blocks;
	}


	public static String locationToString(Location l) {
		return l.getBlock().getType()+";"+l.getWorld().getName()
				+";"+l.getBlockX()+";"+l.getBlockY()+";"+l.getBlockZ();		
	}
	public static Location locationFromString(String str) {
		String[] block = str.split(";");
		//Material mat = Material.getMaterial(block[0].toUpperCase());		
		World w = Bukkit.getWorld(block[1]);
		int x = Integer.valueOf(block[2]);
		int y = Integer.valueOf(block[3]);
		int z = Integer.valueOf(block[4]);					
		return new Location(w, x, y, z);
	}
	public Material getMaterialFromConfig(String str) {
		String[] block = str.split(";");
		return Material.getMaterial(block[0].toUpperCase());
	}

}
