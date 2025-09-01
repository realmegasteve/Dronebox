package net.Neomoon.dronebox.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record ViewUpdateS2CPayload(String target) implements CustomPayload {
	public static final Identifier ID_RAW = Identifier.of("dronebox", "viewupdatetoggle");
	public static final Id<ViewUpdateS2CPayload> ID = new Id<>(ID_RAW);

	public static final PacketCodec<net.minecraft.network.RegistryByteBuf, ViewUpdateS2CPayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING, ViewUpdateS2CPayload::target,
			ViewUpdateS2CPayload::new
		);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
