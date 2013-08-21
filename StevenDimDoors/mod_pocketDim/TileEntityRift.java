package StevenDimDoors.mod_pocketDim;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import StevenDimDoors.mod_pocketDim.blocks.BlockRift;
import StevenDimDoors.mod_pocketDim.helpers.dimHelper;
import StevenDimDoors.mod_pocketDim.helpers.yCoordHelper;
import StevenDimDoors.mod_pocketDim.ticking.MobMonolith;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

public class TileEntityRift extends TileEntity

{
	public int xOffset=0;
	public int yOffset=0;
	public int zOffset=0;
	public int distance=0;
	public boolean hasGrownRifts=false;
	public boolean shouldClose=false;
	//public boolean isClosing=false;
	public boolean isNearRift=false;
	private int count=200;
	private int count2 = 0;
	
	public HashMap<Integer, double[]> renderingCenters = new HashMap<Integer, double[]>();
	public LinkData nearestRiftData;
	Random rand  = new Random();
	
	
	public float age = 0;
	
	
	
	 public boolean canUpdate()
	 {
		 return true;
	 }
	 public void clearBlocksOnRift()
	 {
		 for(double[] coord: this.renderingCenters.values())
		 {
			 int x = MathHelper.floor_double(coord[0]+.5);
			 int y = MathHelper.floor_double(coord[1]+.5);
			 int z = MathHelper.floor_double(coord[2]+.5);
			 
			 if(!BlockRift.isBlockImmune(worldObj,this.xCoord+x, this.yCoord+y, this.zCoord+z))
			 {
				 this.worldObj.setBlockToAir(this.xCoord+x, this.yCoord+y, this.zCoord+z);
			 }
			
			 if(!BlockRift.isBlockImmune(worldObj,this.xCoord-x, this.yCoord-y, this.zCoord-z))
			 {
			 this.worldObj.setBlockToAir(this.xCoord-x, this.yCoord-y, this.zCoord-z);
			 }

		 }
	 }
	 public void spawnEndermen()
	 {
		 if(count>200&&dimHelper.instance.getDimData(this.worldObj.provider.dimensionId)!=null)
		 {
			
			
			 nearestRiftData = dimHelper.instance.getDimData(this.worldObj.provider.dimensionId).findNearestRift(worldObj, 5, xCoord, yCoord, zCoord);
			 if(nearestRiftData!=null)
			 {
				 if(rand.nextInt(30)==0&&!this.worldObj.isRemote)
				 {
					
					List list =  worldObj.getEntitiesWithinAABB(EntityEnderman.class, AxisAlignedBB.getBoundingBox( this.xCoord-9, this.yCoord-3, this.zCoord-9, this.xCoord+9, this.yCoord+3, this.zCoord+9));
					
					if(list.size()<1)
					{
					 
					 
					  EntityEnderman creeper = new EntityEnderman(worldObj);
	                  creeper.setLocationAndAngles(this.xCoord+.5, this.yCoord-1, this.zCoord+.5, 5, 6);
	                  worldObj.spawnEntityInWorld(creeper);
					}				
				 }
			 }
			 else
			 {
				 this.isNearRift=false;
			 }
			 count=0;
		 }
	 }
	 public void closeRift()
	 {
		 if(count2>20&&count2<22)
		 {			 
			 nearestRiftData = dimHelper.instance.getDimData(this.worldObj.provider.dimensionId).findNearestRift(worldObj, 10, xCoord, yCoord, zCoord);
			 if(this.nearestRiftData!=null)
			 {
			 TileEntityRift rift = (TileEntityRift) this.worldObj.getBlockTileEntity(nearestRiftData.locXCoord, nearestRiftData.locYCoord, nearestRiftData.locZCoord);
			 if(rift!=null)
			 {
				 rift.shouldClose=true;
			 }
			 }
		 }
		 if(count2>40)
		 {
			 this.invalidate();
			 this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord,0);
			if(dimHelper.instance.getLinkDataFromCoords(this.xCoord, this.yCoord, this.zCoord, this.worldObj.provider.dimensionId)!=null)
			{
			 dimHelper.instance.removeLink(this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord);
			 this.worldObj.playSound(xCoord, yCoord, zCoord, "mods.DimDoors.sfx.riftClose", (float) .7, 1,true);

			}
			
		 }
		 count2++; 
	 }
	 public void updateEntity() 
	 {
		 
		 if(rand.nextInt(10)==0)
		 {
			 age = age + 1;
			 this.calculateNextRenderQuad(age, rand);
			 
		 }
		this.clearBlocksOnRift();
		this.spawnEndermen();
		
		
		 count++;
		 
		 if(this.shouldClose)
		 {
			 closeRift();
		 }
		
		 if(dimHelper.instance.getLinkDataFromCoords(xCoord, yCoord, zCoord, this.worldObj.provider.dimensionId)==null)
		 {
			 this.invalidate();
			 this.worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		 }
	 }
	 public void grow(int distance)
	 {
		 
	 }
	 public void calculateNextRenderQuad(float age, Random rand)
	 {
		 int iteration =  MathHelper.floor_double((Math.log(Math.pow(age+1,1.5))));
		 double fl =Math.log(iteration+1)/(iteration);
		 double[] coords= new double[4];
		 double noise = ((rand.nextGaussian())/(2+iteration/3+1));
		 
		 if(!this.renderingCenters.containsKey(iteration-1))
		 {
			 if(rand.nextBoolean())
			 {
				coords[0] = fl*1.5;
				coords[1] = rand.nextGaussian()/5;
				coords[2] = 0;
				coords[3] = 1;
			 }
			 else
			 {
				coords[0] = 0;
				coords[1] = rand.nextGaussian()/5;
				coords[2] = fl*1.5;
				coords[3] = 0;

			 }
			 this.renderingCenters.put(iteration-1,coords);
			

		 }
		 else if(!this.renderingCenters.containsKey(iteration))
		 {
			if(this.renderingCenters.get(iteration-1)[3]==0)
			{
				coords[0]=noise/2+this.renderingCenters.get(iteration-1)[0];
				coords[1]=noise/2+this.renderingCenters.get(iteration-1)[1];
				coords[2]= this.renderingCenters.get(iteration-1)[2]+fl;
				coords[3] = 0;

			}
			else
			{
				coords[0]=this.renderingCenters.get(iteration-1)[0]+fl;
				coords[1]=noise/2+this.renderingCenters.get(iteration-1)[1];
				coords[2]=noise/2+this.renderingCenters.get(iteration-1)[2];
				coords[3] = 1;

			} 
			
			 
			this.renderingCenters.put(iteration,coords);
			
		 }

	 }
	 
	 @Override
	 public boolean shouldRenderInPass(int pass)
	 {
	        return pass == 1;
	 }
	 @Override
	    public void readFromNBT(NBTTagCompound nbt)
	    {
	     
	        int i = nbt.getInteger(("Size"));

	        try
	        { 
	            this.count=nbt.getInteger("count");
	            this.count2=nbt.getInteger("count2");
	            this.shouldClose=nbt.getBoolean("shouldClose");
	            this.age=nbt.getFloat("age");
	            for(int key=0; key<=nbt.getInteger("hashMapSize");key++)
	            {
	            	double[] coords = new double[4];
	            	
	            	coords[0]= nbt.getDouble(key+"+0");
	            	coords[1]= nbt.getDouble(key+"+1");
	            	coords[2]= nbt.getDouble(key+"+2");
	            	coords[3]= nbt.getDouble(key+"+3");

	            	this.renderingCenters.put(key, coords);
	            }

	         
	           

	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	        super.readFromNBT(nbt);
	    }

	    @Override
	    public void writeToNBT(NBTTagCompound nbt)
	    {
	        int i = 0;
	       
	      
	        for(Integer key:this.renderingCenters.keySet())
	        {
	        	nbt.setDouble(key+"+0", this.renderingCenters.get(key)[0]);
	        	nbt.setDouble(key+"+1", this.renderingCenters.get(key)[1]);
	        	nbt.setDouble(key+"+2", this.renderingCenters.get(key)[2]);
	        	nbt.setDouble(key+"+3", this.renderingCenters.get(key)[3]);
	        }
	        nbt.setInteger("hashMapSize", this.renderingCenters.size());
	        nbt.setInteger("count", this.count);
	        nbt.setInteger("count2", this.count2);
	        nbt.setFloat("age", this.age);

	        nbt.setBoolean("shouldClose", this.shouldClose);
	        super.writeToNBT(nbt);
	    }
}
