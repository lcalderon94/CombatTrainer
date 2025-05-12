package script;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import sections.*;
import util.Sleep;

@ScriptManifest(author = "Explv", name = "Explv's Tutorial Island " + TutorialIsland.VERSION, info = "Completes Tutorial Island", version=0, logo = "")
public final class TutorialIsland extends Script {
    public static final String VERSION = "v6.2";

    private final TutorialSection rsGuideSection = new RuneScapeGuideSection();
    private final TutorialSection survivalSection = new SurvivalSection();
    private final TutorialSection cookingSection = new CookingSection();
    private final TutorialSection questSection = new QuestSection();
    private final TutorialSection miningSection = new MiningSection();
    private final TutorialSection fightingSection = new FightingSection();
    private final TutorialSection bankSection = new BankSection();
    private final TutorialSection priestSection = new PriestSection();
    private final TutorialSection wizardSection = new WizardSection();

    @Override
    public void onStart() {
        rsGuideSection.exchangeContext(getBot());
        survivalSection.exchangeContext(getBot());
        cookingSection.exchangeContext(getBot());
        questSection.exchangeContext(getBot());
        miningSection.exchangeContext(getBot());
        fightingSection.exchangeContext(getBot());
        bankSection.exchangeContext(getBot());
        priestSection.exchangeContext(getBot());
        wizardSection.exchangeContext(getBot());

        Sleep.sleepUntil(() -> getClient().isLoggedIn() && myPlayer().isVisible(), 6000, 500);
    }

    @Override
    public final int onLoop() throws InterruptedException {
        if (isTutorialIslandCompleted()) {
            // Verificar el piso actual
            int currentFloor = myPlayer().getPosition().getZ();

            // Si estamos debajo del segundo piso, necesitamos subir
            if (currentFloor < 2) {
                log("On floor " + currentFloor + ", need to go up to bank...");

                RS2Object staircase = getObjects().closest(obj ->
                        obj != null &&
                                obj.getName() != null &&
                                obj.getName().equals("Staircase") &&
                                obj.hasAction("Climb-up")
                );

                if (staircase != null) {
                    log("Found staircase, climbing up...");
                    if (staircase.interact("Climb-up")) {
                        Sleep.sleepUntil(() -> myPlayer().getPosition().getZ() > currentFloor, 5000, 1000);
                    }
                }
                return 200;
            }

            // Ya en el segundo piso, abrir banco directamente
            if (!getBank().isOpen()) {
                log("Opening bank...");
                RS2Object bankBooth = getObjects().closest(obj ->
                        obj != null &&
                                obj.getName() != null &&
                                obj.getName().equals("Bank booth") &&
                                obj.hasAction("Bank")
                );

                if (bankBooth != null) {
                    getCamera().toEntity(bankBooth);
                    if (bankBooth.interact("Bank")) {
                        Sleep.sleepUntil(() -> getBank().isOpen(), 5000, 1000);
                    }
                }
                return 200;
            }

            // Depositar items equipados
            if (getBank().isOpen() && !getEquipment().isEmpty()) {
                log("Depositing equipped items...");
                if (getBank().depositWornItems()) {
                    Sleep.sleepUntil(() -> getEquipment().isEmpty(), 3000, 500);
                }
                return 200;
            }

            // Depositar inventario
            if (getBank().isOpen() && !getInventory().isEmpty()) {
                log("Depositing inventory...");
                if (getBank().depositAll()) {
                    Sleep.sleepUntil(() -> getInventory().isEmpty(), 3000, 500);
                }
                return 200;
            }

            // Cerrar banco y detener script
            if (getBank().isOpen() && getEquipment().isEmpty() && getInventory().isEmpty()) {
                if (getBank().close()) {
                    Sleep.sleepUntil(() -> !getBank().isOpen(), 3000, 500);
                    log("Tutorial completed and items banked. Stopping script...");
                    stop(true);
                    return 0;
                }
            }

            return 200;
        }

        switch (getTutorialSection()) {
            case 0:
            case 1:
                rsGuideSection.onLoop();
                break;
            case 2:
            case 3:
                survivalSection.onLoop();
                break;
            case 4:
            case 5:
                cookingSection.onLoop();
                break;
            case 6:
            case 7:
                questSection.onLoop();
                break;
            case 8:
            case 9:
                miningSection.onLoop();
                break;
            case 10:
            case 11:
            case 12:
                fightingSection.onLoop();
                break;
            case 14:
            case 15:
                bankSection.onLoop();
                break;
            case 16:
            case 17:
                priestSection.onLoop();
                break;
            case 18:
            case 19:
            case 20:
                wizardSection.onLoop();
                break;
        }
        return 200;
    }

    private int getTutorialSection() {
        return getConfigs().get(406);
    }

    private boolean isTutorialIslandCompleted() {
        return getConfigs().get(281) == 1000 && myPlayer().isVisible();
    }
}
