package com.cheeseum.lazyzombies;

import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = LazyZombies.MODID, version = LazyZombies.VERSION)
public class LazyZombies
{
    public static final String MODID = "lazyzombies";
    public static final String VERSION = "1.0";
   
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void entityJoin(EntityJoinWorldEvent event)
    {
    	if (event.entity instanceof EntityZombie) {
    		EntityZombie zombie = (EntityZombie)event.entity;
    		
    		// never break doors
    		PathNavigate nav = zombie.getNavigator();
    		if (nav != null)
    			nav.setBreakDoors(false);
    		
    		// nearby villagers only
    		if (zombie.targetTasks.taskEntries.size() > 2) {
	    		zombie.targetTasks.taskEntries.remove(2);
		        zombie.targetTasks.addTask(2, new EntityAINearestAttackableTarget(zombie, EntityVillager.class, 0, true));
    		}
    	}
    }
    
}
