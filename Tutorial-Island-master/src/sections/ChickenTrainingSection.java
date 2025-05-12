package sections;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import util.Sleep;

import java.util.Arrays;
import java.util.List;

public final class ChickenTrainingSection extends TutorialSection {

    // Área donde están las chickens al este de Lumbridge
    private static final Area CHICKEN_AREA = new Area(3171, 3290, 3183, 3301);
    private static final Area LUMBRIDGE_CASTLE = new Area(3207, 3209, 3210, 3228);

    // Path desde el banco de Lumbridge hasta las chickens
    private static final List<Position> PATH_TO_CHICKENS = Arrays.asList(
            new Position(3207, 3220, 2), // Banco
            new Position(3205, 3209, 2), // Cerca de escaleras
            new Position(3205, 3209, 0), // Planta baja
            new Position(3206, 3218, 0),
            new Position(3212, 3221, 0),
            new Position(3217, 3219, 0),
            new Position(3223, 3219, 0),
            new Position(3232, 3227, 0),
            new Position(3235, 3237, 0),
            new Position(3238, 3248, 0),
            new Position(3237, 3258, 0),
            new Position(3232, 3267, 0),
            new Position(3225, 3273, 0),
            new Position(3216, 3277, 0),
            new Position(3207, 3283, 0),
            new Position(3197, 3287, 0),
            new Position(3187, 3291, 0),
            new Position(3177, 3295, 0)
    );

    private final int DESIRED_LEVEL = 20;
    private boolean equipmentReady = false;

    public ChickenTrainingSection() {
        super("Chicken Trainer");
    }

    @Override
    public final void onLoop() throws InterruptedException {
        // Verificar si hemos alcanzado los niveles deseados
        if (hasReachedDesiredLevels()) {
            log("Training complete! Reached level 20 in all combat stats.");
            getBot().getScriptExecutor().stop();
            return;
        }

        // Asegurar que tenemos el equipo puesto
        if (!equipmentReady) {
            equipItems();
            return;
        }

        // Si estamos en el castillo de Lumbridge y necesitamos bajar
        if (LUMBRIDGE_CASTLE.contains(myPosition()) && myPosition().getZ() > 0) {
            descendStairs();
            return;
        }

        // Si no estamos en el área de chickens, caminar hacia allá
        if (!CHICKEN_AREA.contains(myPosition())) {
            walkToChickens();
            return;
        }

        // Cambiar estilo de combate según necesidad
        handleCombatStyle();

        // Atacar chickens
        if (!myPlayer().isAnimating() && !myPlayer().isMoving() && !getCombat().isFighting()) {
            attackChicken();
        }
    }

    private void equipItems() throws InterruptedException {
        boolean shieldEquipped = getEquipment().isWearingItem(EquipmentSlot.SHIELD, "Wooden shield");
        boolean swordEquipped = getEquipment().isWearingItem(EquipmentSlot.WEAPON, "Bronze sword");

        if (!shieldEquipped && getInventory().contains("Wooden shield")) {
            if (getInventory().getItem("Wooden shield").interact("Wield")) {
                Sleep.sleepUntil(() -> getEquipment().isWearingItem(EquipmentSlot.SHIELD, "Wooden shield"), 2000);
            }
        }

        if (!swordEquipped && getInventory().contains("Bronze sword")) {
            if (getInventory().getItem("Bronze sword").interact("Wield")) {
                Sleep.sleepUntil(() -> getEquipment().isWearingItem(EquipmentSlot.WEAPON, "Bronze sword"), 2000);
            }
        }

        // Verificar que ambos estén equipados
        if (getEquipment().isWearingItem(EquipmentSlot.SHIELD, "Wooden shield") &&
                getEquipment().isWearingItem(EquipmentSlot.WEAPON, "Bronze sword")) {
            equipmentReady = true;
            log("Equipment ready! Heading to chickens...");
        }
    }

    private void descendStairs() throws InterruptedException {
        RS2Object staircase = getObjects().closest(obj ->
                obj != null &&
                        obj.getName() != null &&
                        obj.getName().equals("Staircase") &&
                        obj.hasAction("Climb-down")
        );

        if (staircase != null) {
            log("Descending stairs...");
            if (staircase.interact("Climb-down")) {
                Sleep.sleepUntil(() -> myPosition().getZ() < 2, 5000, 600);
            }
        }
    }

    private void walkToChickens() throws InterruptedException {
        log("Walking to chicken area...");
        getWalking().walkPath(PATH_TO_CHICKENS);
    }

    private void attackChicken() throws InterruptedException {
        NPC chicken = getNpcs().closest(npc ->
                npc != null &&
                        npc.getName().equals("Chicken") &&
                        !npc.isUnderAttack() &&
                        npc.getHealthPercent() > 0 &&
                        CHICKEN_AREA.contains(npc.getPosition())
        );

        if (chicken != null) {
            if (chicken.interact("Attack")) {
                Sleep.sleepUntil(() -> myPlayer().isAnimating() || getCombat().isFighting(), 3000, 600);
            }
        }
    }

    private void handleCombatStyle() throws InterruptedException {
        int attackLevel = getSkills().getStatic(Skill.ATTACK);
        int strengthLevel = getSkills().getStatic(Skill.STRENGTH);
        int defenceLevel = getSkills().getStatic(Skill.DEFENCE);

        int targetStyle = -1;

        // Lógica simple: entrenar el skill más bajo
        if (attackLevel < DESIRED_LEVEL && attackLevel <= strengthLevel && attackLevel <= defenceLevel) {
            targetStyle = 0; // Accurate (Attack)
        } else if (strengthLevel < DESIRED_LEVEL && strengthLevel <= defenceLevel) {
            targetStyle = 1; // Aggressive (Strength)
        } else if (defenceLevel < DESIRED_LEVEL) {
            targetStyle = 3; // Defensive (Defence)
        }

        setCombatStyle(targetStyle);
    }

    private void setCombatStyle(int style) throws InterruptedException {
        if (style == -1) return;

        if (getConfigs().get(43) != style) {
            log("Switching combat style to: " + (style == 0 ? "Attack" : style == 1 ? "Strength" : "Defence"));

            getTabs().open(Tab.ATTACK);
            Sleep.sleepUntil(() -> getTabs().getOpen() == Tab.ATTACK, 2000);

            // Los widgets de estilo de combate en OSBot
            // Accurate: widget 593, child 7
            // Aggressive: widget 593, child 11
            // Defensive: widget 593, child 15
            int childId = 7 + (style * 4);

            if (getWidgets().get(593, childId) != null) {
                getWidgets().get(593, childId).interact();
                Sleep.sleepUntil(() -> getConfigs().get(43) == style, 2000);
            }
        }
    }

    private boolean hasReachedDesiredLevels() {
        return getSkills().getStatic(Skill.ATTACK) >= DESIRED_LEVEL &&
                getSkills().getStatic(Skill.STRENGTH) >= DESIRED_LEVEL &&
                getSkills().getStatic(Skill.DEFENCE) >= DESIRED_LEVEL;
    }
}