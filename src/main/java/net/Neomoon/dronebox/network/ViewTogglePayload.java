package net.Neomoon.dronebox.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record ViewTogglePayload(
		String target
	) implements CustomPayload {
	public static final Identifier ID_RAW = Identifier.of("dronebox", "viewtoggle_payload");
	public static final Id<ViewTogglePayload> ID = new Id<>(ID_RAW);

	public static final PacketCodec<net.minecraft.network.RegistryByteBuf, ViewTogglePayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING, ViewTogglePayload::target,
			ViewTogglePayload::new
		);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
