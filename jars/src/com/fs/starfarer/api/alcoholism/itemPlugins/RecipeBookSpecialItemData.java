package com.fs.starfarer.api.alcoholism.itemPlugins;

import com.fs.starfarer.api.alcoholism.memory.AlcoholAPI;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.IndustrialAlcohol;
import com.fs.starfarer.api.campaign.SpecialItemData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeBookSpecialItemData extends SpecialItemData {

    private List<String> alcoholRecipeList;

    public RecipeBookSpecialItemData(String id, String... alcoholIds) {
        super(id, null);
        this.alcoholRecipeList = new ArrayList<>(Arrays.asList(alcoholIds));
    }

    public void addAlcohol(String alcoholId){
        alcoholRecipeList.add(alcoholId);
    }

    public void removeAlcohol(String alcoholId){
        alcoholRecipeList.remove(alcoholId);
    }

    public List<AlcoholAPI> getAlcoholList(){
        List<AlcoholAPI> alcoholList = new ArrayList<>();
        for (String s : alcoholRecipeList) alcoholList.add(AlcoholRepo.get(s));

        return alcoholList;
    }

    public List<String> getBaseList() {
        return alcoholRecipeList;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        final int prime = 31;
        result = prime * result + ((alcoholRecipeList == null) ? 0 : alcoholRecipeList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        boolean base = super.equals(obj);

        if (base && obj instanceof RecipeBookSpecialItemData){
            return alcoholRecipeList.equals(((RecipeBookSpecialItemData) obj).getBaseList());
        } else return false;
    }
}
