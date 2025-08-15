package net.Neomoon.dronebox.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record CameraFramePayload(String droneUuid, byte[] imageData) implements CustomPayload {
	public static final Identifier ID_RAW = Identifier.of("dronebox", "camera_frame");
	public static final CustomPayload.Id<CameraFramePayload> ID = new CustomPayload.Id<>(ID_RAW);

	public static final PacketCodec<RegistryByteBuf, CameraFramePayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING, CameraFramePayload::droneUuid,
			PacketCodecs.BYTE_ARRAY, CameraFramePayload::imageData,
			CameraFramePayload::new
		);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
