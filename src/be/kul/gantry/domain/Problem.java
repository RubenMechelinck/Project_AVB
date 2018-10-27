package be.kul.gantry.domain;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by Wim on 27/04/2015.
 */
public class Problem {

    private final int minX, maxX, minY, maxY;
    private final int maxLevels;
    private final List<Item> items;
    private final List<Job> inputJobSequence;
    private final List<Job> outputJobSequence;

    private final List<Gantry> gantries;
    private final List<Slot> slots;
    private final int safetyDistance;
    private final int pickupPlaceDuration;


    private List<Integer> heightList;
    //We maken een 2D Array aan voor alle bodem slots (z=0)
    HashMap<Integer, HashMap<Integer, Slot>> grondSlots = new HashMap<>();
    //We vullen de array met nieuwe arrays
    HashMap<Integer, Slot> Map = new HashMap<>();

    public enum Richting {
        NaarVoor,
        NaarAchter
    }

    public enum Operatie {
        VerplaatsIntern,
        VerplaatsNaarOutput,
        VerplaatsNaarBinnen
    }


    public Problem(int minX, int maxX, int minY, int maxY, int maxLevels, List<Item> items, List<Job> inputJobSequence, List<Job> outputJobSequence, List<Gantry> gantries, List<Slot> slots, int safetyDistance, int pickupPlaceDuration) {
        this.minX = -10;
        this.maxX = 1010;
        this.minY = minY;
        this.maxY = maxY;
        this.maxLevels = maxLevels;
        this.items = items;
        this.inputJobSequence = inputJobSequence;
        this.outputJobSequence = outputJobSequence;
        this.gantries = gantries;
        this.slots = slots;
        this.safetyDistance = safetyDistance;
        this.pickupPlaceDuration = pickupPlaceDuration;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Job> getInputJobSequence() {
        return inputJobSequence;
    }

    public List<Job> getOutputJobSequence() {
        return outputJobSequence;
    }

    public List<Gantry> getGantries() {
        return gantries;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public int getSafetyDistance() {
        return safetyDistance;
    }

    public int getPickupPlaceDuration() {
        return pickupPlaceDuration;
    }

    public HashMap<Integer, Slot> getMap() {
        return Map;
    }

    public void setMap(HashMap<Integer, Slot> map) {
        this.Map = map;
    }

    public void writeJsonFile(File file) throws IOException {
        JSONObject root = new JSONObject();

        JSONObject parameters = new JSONObject();
        root.put("parameters",parameters);

        parameters.put("gantrySafetyDistance",safetyDistance);
        parameters.put("maxLevels",maxLevels);
        parameters.put("pickupPlaceDuration",pickupPlaceDuration);

        JSONArray items = new JSONArray();
        root.put("items",items);

        for(Item item : this.items) {
            JSONObject jo = new JSONObject();
            jo.put("id",item.getId());

            items.add(jo);
        }


        JSONArray slots = new JSONArray();
        root.put("slots",slots);
        for(Slot slot : this.slots) {
            JSONObject jo = new JSONObject();
            jo.put("id",slot.getId());
            jo.put("cx",slot.getCenterX());
            jo.put("cy",slot.getCenterY());
            jo.put("minX",slot.getXMin());
            jo.put("maxX",slot.getXMax());
            jo.put("minY",slot.getYMin());
            jo.put("maxY",slot.getYMax());
            jo.put("z",slot.getZ());
            jo.put("type",slot.getType().name());
            jo.put("itemId",slot.getItem() == null ? null : slot.getItem().getId());

            slots.add(jo);
        }

        JSONArray gantries = new JSONArray();
        root.put("gantries",gantries);
        for(Gantry gantry : this.gantries) {
            JSONObject jo = new JSONObject();

            jo.put("id",gantry.getId());
            jo.put("xMin",gantry.getXMin());
            jo.put("xMax",gantry.getXMax());
            jo.put("startX",gantry.getStartX());
            jo.put("startY",gantry.getStartY());
            jo.put("xSpeed",gantry.getXSpeed());
            jo.put("ySpeed",gantry.getYSpeed());

            gantries.add(jo);
        }

        JSONArray inputSequence = new JSONArray();
        root.put("inputSequence",inputSequence);

        for(Job inputJ : this.inputJobSequence) {
            JSONObject jo = new JSONObject();
            jo.put("itemId",inputJ.getItem().getId());
            jo.put("fromId",inputJ.getPickup().getSlot().getId());

            inputSequence.add(jo);
        }

        JSONArray outputSequence = new JSONArray();
        root.put("outputSequence",outputSequence);

        for(Job outputJ : this.outputJobSequence) {
            JSONObject jo = new JSONObject();
            jo.put("itemId",outputJ.getItem().getId());
            jo.put("toId",outputJ.getPlace().getSlot().getId());

            outputSequence.add(jo);
        }

        try(FileWriter fw = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            fw.write(gson.toJson(root));
        }

    }

    public static Problem fromJson(File file) throws IOException, ParseException {


        JSONParser parser = new JSONParser();

        try(FileReader reader = new FileReader(file)) {
            JSONObject root = (JSONObject) parser.parse(reader);

            List<Item> itemList = new ArrayList<>();
            List<Slot> slotList = new ArrayList<>();
            List<Gantry> gantryList = new ArrayList<>();
            List<Job> inputJobList = new ArrayList<>();
            List<Job> outputJobList = new ArrayList<>();

            JSONObject parameters = (JSONObject) root.get("parameters");
            int safetyDist = ((Long)parameters.get("gantrySafetyDistance")).intValue();
            int maxLevels = ((Long)parameters.get("maxLevels")).intValue();
            int pickupPlaceDuration = ((Long)parameters.get("pickupPlaceDuration")).intValue();

            JSONArray items = (JSONArray) root.get("items");
            for(Object o : items) {
                int id = ((Long)((JSONObject)o).get("id")).intValue();

                Item c = new Item(id);
                itemList.add(c);
            }

            int overallMinX = Integer.MAX_VALUE, overallMaxX = Integer.MIN_VALUE;
            int overallMinY = Integer.MAX_VALUE, overallMaxY = Integer.MIN_VALUE;

            JSONArray slots = (JSONArray) root.get("slots");

            for(Object o : slots) {
                JSONObject slot = (JSONObject) o;

                int id = ((Long)slot.get("id")).intValue();
                int cx = ((Long)slot.get("cx")).intValue();
                int cy = ((Long)slot.get("cy")).intValue();
                int minX = ((Long)slot.get("minX")).intValue();
                int minY = ((Long)slot.get("minY")).intValue();
                int maxX = ((Long)slot.get("maxX")).intValue();
                int maxY = ((Long)slot.get("maxY")).intValue();
                int z = ((Long)slot.get("z")).intValue();

                overallMinX = Math.min(overallMinX,minX);
                overallMaxX = Math.max(overallMaxX,maxX);
                overallMinY = Math.min(overallMinY,minY);
                overallMaxY = Math.max(overallMaxY,maxY);

                Slot.SlotType type = Slot.SlotType.valueOf((String)slot.get("type"));
                Integer itemId = slot.get("itemId") == null ? null : ((Long)slot.get("itemId")).intValue();
                Item c = itemId == null ? null : itemList.get(itemId);

                Slot s = new Slot(id,cx,cy,minX,maxX,minY,maxY,z,type,c);

                slotList.add(s);
            }


            JSONArray gantries = (JSONArray) root.get("gantries");
            for(Object o : gantries) {
                JSONObject gantry = (JSONObject) o;

                int id = ((Long)gantry.get("id")).intValue();
                int xMin = ((Long)gantry.get("xMin")).intValue();
                int xMax = ((Long)gantry.get("xMax")).intValue();
                int startX = ((Long)gantry.get("startX")).intValue();
                int startY = ((Long)gantry.get("startY")).intValue();
                double xSpeed = ((Double)gantry.get("xSpeed")).doubleValue();
                double ySpeed = ((Double)gantry.get("ySpeed")).doubleValue();

                Gantry g = new Gantry(id, xMin, xMax, startX, startY, xSpeed, ySpeed);
                gantryList.add(g);
            }

            JSONArray inputJobs = (JSONArray) root.get("inputSequence");
            int jid = 0;
            for(Object o : inputJobs) {
                JSONObject inputJob = (JSONObject) o;

                int iid = ((Long) inputJob.get("itemId")).intValue();
                int sid = ((Long) inputJob.get("fromId")).intValue();

                Job job = new Job(jid++,itemList.get(iid),slotList.get(sid),null);
                inputJobList.add(job);
            }

            JSONArray outputJobs = (JSONArray) root.get("outputSequence");
            for(Object o : outputJobs) {
                JSONObject outputJob = (JSONObject) o;

                int iid = ((Long) outputJob.get("itemId")).intValue();
                int sid = ((Long) outputJob.get("toId")).intValue();

                Job job = new Job(jid++,itemList.get(iid),null, slotList.get(sid));
                outputJobList.add(job);
            }


            return new Problem(
                    overallMinX,
                    overallMaxX,
                    overallMinY,
                    overallMaxY,
                    maxLevels,
                    itemList,
                    inputJobList,
                    outputJobList,
                    gantryList,
                    slotList,
                    safetyDist,
                    pickupPlaceDuration
                    );

        }

    }

    // hier wordt de parent child link gemaakt dus alle grondsloten met hun ouders dus setparent en setchild van ieder slot
    public void MakeParentChildLink() {

        //Hier wordt de hoogte van elke rij opgeslagen
        heightList = new ArrayList<>();

        for (Slot slot : slots) {

            int slotCenterY = slot.getCenterY() / 10;
            int slotCenterX = slot.getCenterX() / 10;

            // Als de Z gelijk is aan nul weten we dat het slot zich op de grond bevindt
            if (slot.getZ() == 0) {
                //initialisatie (if grondslot get(slotcenterY) geen value heeft maak een value new Hashmap)
                grondSlots.computeIfAbsent(slotCenterY, s -> new HashMap<>());

                //Er zijn ook input en output slots dus enkel bij storage
                if (slot.getType().equals(Slot.SlotType.STORAGE)) {
                    grondSlots.get(slotCenterY).put(slotCenterX, slot);

                }
            } else {

                Slot child = grondSlots.get(slotCenterY).get(slotCenterX);
                // we stijgen telkens tot op de hoogste z
                for (int i = 1; i < slot.getZ(); i++) {
                    child = child.getParent();
                }
                //Als we de child gevonden hebben zetten we de link
                slot.setChild(child);
                child.setParent(slot);
            }

            //Wanneer het slot gevuld is in een hasmap steken
            if (slot.getItem() != null) Map.put(slot.getItem().getId(), slot);
        }

        for(HashMap<Integer, Slot> row : grondSlots.values()){
            heightList.add(GeneralMeasures.hoogteBezetting(new HashSet<>(row.values())));
        }
    }

    // Eerst proberen we outputjobs uit te voeren tot deze bepaalde items nodig heeft die nog niet in het veld staan,
    // dan schakelen we over op inputjobs tot dat item voor de outputjob gevonden is
    public List<ItemMovement> werkUit()
    {
        MakeParentChildLink();

        List<ItemMovement> itemMovements = new ArrayList<>();
        int inputJobCounter=0,outputJobCounter=0;

        //We beginnen met het uitvoeren van de outputjobs
        while(outputJobCounter<outputJobSequence.size()) {
            Job outputJob = outputJobSequence.get(outputJobCounter);

            Item item = outputJob.getItem();
            Slot slot = Map.get(item.getId());

            //kijken of het in field zit.
            if(slot != null) {

                //Als het item dat we nodig heeft containers op hem heeft staan eerste deze uitgraven en nieuwe plaats geven
                if(slot.getParent() != null && slot.getParent().getItem() != null){
                    itemMovements.addAll(uitGraven(slot.getParent(), gantries.get(0)));
                }

                //De verplaatsingen nodig om de outputjob te vervolledigen en alle sloten updaten met hun huidige items
                itemMovements.addAll(GeneralMeasures.createMoves(pickupPlaceDuration,gantries.get(0), slot, outputJob.getPlace().getSlot()));
                update( Operatie.VerplaatsNaarOutput, outputJob.getPlace().getSlot(),slot);

                outputJobCounter++;

            }else {
                //een nieuw inputjob doen tot het item gevonden is
                while(slot == null){

                    arrangeInputJob(inputJobCounter,itemMovements);
                    inputJobCounter++;

                    //Opnieuw zoeken voor het Slot van de outputjob
                    slot = Map.get(outputJob.getItem().getId());
                }
            }
        }

        //eventueele overblijvende inputjobs uitvoeren
        while( inputJobCounter < inputJobSequence.size())
        {
            arrangeInputJob(inputJobCounter,itemMovements);
            inputJobCounter++;
        }
        return itemMovements;
    }

    //inputjob afwerken
    public void arrangeInputJob(int inputJobCounter, List<ItemMovement> itemMovements)
    {
        Job inputJob = inputJobSequence.get(inputJobCounter);

        //overige inputjobs afwerken
        int row = heightList.indexOf(Collections.max(heightList));
        Slot destination = GeneralMeasures.ZoekLeegSlot(new HashSet<>(grondSlots.get(row).values()));

        //De verplaatsingen nodig om de outputjob te vervolledigen en alle sloten updaten met hun huidige items
        inputJob.getPickup().getSlot().setItem(inputJob.getItem());
        itemMovements.addAll(GeneralMeasures.createMoves(pickupPlaceDuration,gantries.get(0),inputJob.getPickup().getSlot(), destination));
        update(Operatie.VerplaatsNaarBinnen, destination,inputJob.getPickup().getSlot());
    }

    public void update(Operatie operatie , Slot destination, Slot startSlot){
        //Items in slots worden aangepast en de hoogte van de rij(en) worden aangepast;

        int fromCenter = startSlot.getCenterY()/10;
        int toCenter = destination.getCenterY()/10;

        switch (operatie) {
            case VerplaatsNaarOutput:
                Map.remove(startSlot.getItem().getId());
                startSlot.setItem(null);
                heightList.set(fromCenter, GeneralMeasures.hoogteBezetting(new HashSet<>(grondSlots.get(fromCenter).values())));
                break;
            case VerplaatsIntern:
                destination.setItem(startSlot.getItem());
                startSlot.setItem(null);
                Map.put(destination.getItem().getId(), destination);

                heightList.set(fromCenter, GeneralMeasures.hoogteBezetting(new HashSet<>(grondSlots.get(fromCenter).values())));
                heightList.set(toCenter, GeneralMeasures.hoogteBezetting(new HashSet<>(grondSlots.get(toCenter).values())));
                break;
            case VerplaatsNaarBinnen:
                destination.setItem(startSlot.getItem());
                startSlot.setItem(null);
                Map.put(destination.getItem().getId(), destination);
                heightList.set(toCenter, GeneralMeasures.hoogteBezetting(new HashSet<>(grondSlots.get(toCenter).values())));
                break;

        }
    }

    //Deze functie graaft een bepaald slot dat we nodig hebben uit en verplaatst al de bovenliggende sloten.
    public List<ItemMovement> uitGraven(Slot slot, Gantry gantry){

        List<ItemMovement> itemMovements = new ArrayList<>();
        Richting richting = Richting.NaarVoor;

        //Recursief naar boven gaan, doordat we namelijk eerste de gevulde parents van een bepaald slot moeten uithalen
        if(slot.getParent() != null && slot.getParent().getItem() != null){
            itemMovements.addAll(uitGraven(slot.getParent(), gantry));
        }

        //Slot in een zo dicht mogelijke rij zoeken
        Boolean newSlotFound = false;
        Slot newSlot = null;
        int index = 1;
        do {

            // bij het NaarAchter lopen uw index telkens het negatieve deel nemen, dus deze wordt telkens groter negatief.
            if (richting == Richting.NaarAchter) {
                index = -index;
            }
            //we overlopen eerst alle richtingen NaarVoor wanneer deze op zen einde komt en er geen plaats meer is van richting veranderen naar achter
            // index terug op 1 zetten omdat de indexen ervoor al gecontroleerd waren ervoor:
            if (grondSlots.get((slot.getCenterY() / 10) + index) == null) {
                //Grootte resetten en richting omdraaien
                index = 1;
                richting = Richting.NaarAchter;
                continue;
            }

            int GetNewSlot = (slot.getCenterY() / 10) + index;
            Set<Slot> ondersteRij = new HashSet<>(grondSlots.get(GetNewSlot).values());
            newSlot = GeneralMeasures.ZoekLeegSlot(ondersteRij);

            if(newSlot != null)
            {
                newSlotFound = true;
            }
            //telkens één slot verder gaan
            index += 1;
        }while(newSlotFound == false);
        // vanaf er een nieuw vrij slot gevonden is deze functie verlaten

        //verplaatsen
        itemMovements.addAll(GeneralMeasures.createMoves(pickupPlaceDuration,gantry, slot, newSlot));
        update(Operatie.VerplaatsIntern, newSlot, slot);

        return itemMovements;
    }
}
