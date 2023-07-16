package com.fs.starfarer.api.alcoholism.listeners;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;

public interface RefitTabListener {
    void reportRefitOpened(CampaignFleetAPI fleet);
    void reportRefitClosed(CampaignFleetAPI fleet);
}
