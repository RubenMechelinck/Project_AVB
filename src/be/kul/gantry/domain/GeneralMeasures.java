package be.kul.gantry.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class GeneralMeasures {

    public static Slot zoekLeegSlot(Set<Slot> toCheck){
        //in een rij een slot zoeken we beginnen zo laag mogelijk
        Set<Slot> parents = new HashSet<>();
        for(Slot s: toCheck){
            if (s.getItem() == null){
                return s;
            }
            else if(s.getParent() != null) {
                parents.add(s.getParent());
            }
        }
        return zoekLeegSlot(parents);
    }

    //Bereken welke hoogte elke rij heeft
    //Recursief alle Parents checken van slots waar iets inzit, omgekeerd is niet nodig want er kan niet iets op niets staan
    public static int hoogteBezetting(Set<Slot> toCheck){
        Set<Slot> parents = new HashSet<>();

        for(Slot s: toCheck){
            if(s != null){
                parents.add(s.getParent());
            }
            else{
                //return nul wanneer niet vol
                return 0;
            }
        }
        //Opnieuw met parents
        return hoogteBezetting(parents);
    }

    public static List<ItemMovement> createMoves(double pickupPlaceDuration, Gantry gantry, Slot start , Slot destination){

        List<ItemMovement> movements = new ArrayList<>();
        //De tijd wordt in ItemMovement zelf berekend

        //De kraan bewegen naar het slot waar het item inzit;
        movements.add(new ItemMovement(0, start.getCenterX(), start.getCenterY(), null,gantry));
        //Item heffen;
        movements.add(new ItemMovement(pickupPlaceDuration, gantry.getX(), gantry.getY(), start.getItem().getId(), gantry));
        //Item vervoeren naar destination;
        movements.add(new ItemMovement(0, destination.getCenterX(), destination.getCenterY(), start.getItem().getId(), gantry));
        //Item plaatsen op destination;
        movements.add(new ItemMovement(pickupPlaceDuration, gantry.getX(), gantry.getY(), null, gantry));

        return movements;
    }


}
