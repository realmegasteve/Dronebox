package net.Neomoon.dronebox.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record ViewUpdatePayload(String target) implements CustomPayload {
	public static final Identifier ID_RAW = Identifier.of("dronebox", "viewupdatetoggle");
	public static final Id<ViewUpdatePayload> ID = new Id<>(ID_RAW);

	public static final PacketCodec<net.minecraft.network.RegistryByteBuf, ViewUpdatePayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING, ViewUpdatePayload::target,
			ViewUpdatePayload::new
		);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
