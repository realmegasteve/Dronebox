package net.Neomoon.dronebox.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record DroneBatchC2SPayload(
    List<DroneStateC2SPayload> droneStates
) implements CustomPayload {
    
    public static final Id<DroneBatchC2SPayload> ID = new Id<>(Identifier.of("dronebox", "batch_drone_state_c2s"));
    
    public static final PacketCodec<RegistryByteBuf, DroneBatchC2SPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, DroneStateC2SPayload.CODEC),
            DroneBatchC2SPayload::droneStates,
            DroneBatchC2SPayload::new
        );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
