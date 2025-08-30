package net.Neomoon.dronebox.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record RequestCameraPayload(String droneUuid) implements CustomPayload {
	public static final Identifier ID_RAW = Identifier.of("dronebox", "request_camera");
	public static final CustomPayload.Id<RequestCameraPayload> ID = new CustomPayload.Id<>(ID_RAW);

	public static final PacketCodec<net.minecraft.network.RegistryByteBuf, RequestCameraPayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING, RequestCameraPayload::droneUuid,
			RequestCameraPayload::new
		);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
