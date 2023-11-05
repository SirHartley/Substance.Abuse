package com.fs.starfarer.api.alcoholism_re.listeners;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;

public interface RefitTabListener {
    void reportRefitOpened(CampaignFleetAPI fleet);
    void reportRefitClosed(CampaignFleetAPI fleet);
}
