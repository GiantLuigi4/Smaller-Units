package tfc.smallerunits.data.access;

import tfc.smallerunits.networking.hackery.NetworkingHacks;

public interface LevelDescripted {
    NetworkingHacks.LevelDescriptor getDescriptor();
    void setDescriptor(NetworkingHacks.LevelDescriptor descriptor);
}
