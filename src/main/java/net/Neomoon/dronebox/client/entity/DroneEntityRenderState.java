
package net.Neomoon.dronebox.client.entity;

import net.Neomoon.dronebox.Drone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;

@Environment(EnvType.CLIENT)
public class DroneEntityRenderState extends LivingEntityRenderState {
	public Drone entity;

	public double roll;
	public double Pitch;
	public double yaw;
	public int size;

	public DroneEntityRenderState() {
	}
}
