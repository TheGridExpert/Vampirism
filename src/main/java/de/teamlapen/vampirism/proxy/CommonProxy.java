package de.teamlapen.vampirism.proxy;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.registry.EntityRegistry;
import de.teamlapen.vampirism.VampireEventHandler;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.entity.*;
import de.teamlapen.vampirism.util.Logger;

public abstract class CommonProxy implements IProxy{
	
	private static final Map<String,NBTTagCompound> extendedEntityData = new HashMap<String,NBTTagCompound>();
	
	public void registerEntitys(){
		BiomeGenBase[] allBiomes = Iterators.toArray(Iterators.filter(Iterators.forArray(BiomeGenBase.getBiomeGenArray()),
				Predicates.notNull()), BiomeGenBase.class);
		
		//Registration of vampire hunter
		registerEntity(EntityVampireHunter.class,"VampireHunter");
		EntityRegistry.addSpawn(EntityVampireHunter.class, 2, 0, 1, EnumCreatureType.monster, allBiomes);	
		
		//Registration of vampire
		registerEntity(EntityVampire.class,"Vampire");
		EntityRegistry.addSpawn(EntityVampire.class, 2, 0, 1, EnumCreatureType.monster, allBiomes);
	}
	
	@SuppressWarnings("unchecked")
	private void registerEntity(Class<? extends Entity> entityClass,String name){
		int entityID = EntityRegistry.findGlobalUniqueEntityId();
		long seed = name.hashCode();
		Random rand = new Random(seed);
		int primaryColor = rand.nextInt() * 16777215;
		int secondaryColor = rand.nextInt() * 16777215;

		EntityRegistry.registerGlobalEntityID(entityClass, name, entityID);
		EntityRegistry.registerModEntity(entityClass, name, entityID, VampirismMod.instance, 64, 1, true);
		EntityList.entityEggs.put(Integer.valueOf(entityID), new EntityList.EntityEggInfo(entityID, primaryColor, secondaryColor));
	}
	
	
	public void registerSubscriptions(){
		MinecraftForge.EVENT_BUS.register(new VampireEventHandler());
	}
	
	/**
	 * Stores entity data
	 * @param name
	 * @param compound
	 */
	public static void storeEntityData(String name,NBTTagCompound compound){
		extendedEntityData.put(name, compound);
	}
	
	/**
	 * Removes the stored data from map and returns it
	 * @param name
	 * @return
	 */
	public static NBTTagCompound getEntityData(String name){
		return extendedEntityData.remove(name);
	}

}
