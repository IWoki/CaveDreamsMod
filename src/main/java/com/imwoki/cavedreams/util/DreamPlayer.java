package com.imwoki.cavedreams.util;

public interface DreamPlayer {
    void cavedreams_startDream(long wakeTick, boolean untamed, boolean stabilized);
    boolean cavedreams_isDreaming();
    boolean cavedreams_isAllowWake();
    void cavedreams_startLullabiteDream(long wakeTick);
}