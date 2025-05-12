package script;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import sections.*;
import util.Sleep;

@ScriptManifest(author = "Explv", name = "Explv's Tutorial Island + Combat Training", info = "Completes Tutorial Island and trains combat", version=0, logo = "")
public final class TutorialIsland extends Script {
    public static final String VERSION = "v6.3";

    private final TutorialSection rsGuideSection = new RuneScapeGuideSection();
    private final TutorialSection survivalSection = new SurvivalSection();
    private final TutorialSection cookingSection = new CookingSection();
    private final TutorialSection questSection = new QuestSection();
    private final TutorialSection miningSection = new MiningSection();
    private final TutorialSection fightingSection = new FightingSection();
    private final TutorialSection bankSection = new BankSection();
    private final TutorialSection priestSection = new PriestSection();
    private final TutorialSection wizardSection = new WizardSection();
    private final TutorialSection chickenTrainingSection = new ChickenTrainingSection();

    private boolean hasStartedChickenTraining = false;

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
        chickenTrainingSection.exchangeContext(getBot());

        Sleep.sleepUntil(() -> getClient().isLoggedIn() && myPlayer().isVisible(), 6000, 500);
    }

    @Override
    public final int onLoop() throws InterruptedException {
        if (isTutorialIslandCompleted()) {
            // Si ya iniciamos el entrenamiento con chickens, continuar con eso
            if (hasStartedChickenTraining) {
                chickenTrainingSection.onLoop();
                return 200;
            }

            // TODA LA LÓGICA ORIGINAL DE BANKING SE MANTIENE EXACTAMENTE IGUAL
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

            // AQUÍ ES DONDE MODIFICAMOS: en lugar de cerrar y detener, retirar items
            if (getBank().isOpen() && getEquipment().isEmpty() && getInventory().isEmpty()) {
                // Retirar wooden shield
                if (!getInventory().contains("Wooden shield")) {
                    log("Withdrawing Wooden shield...");
                    if (getBank().withdraw("Wooden shield", 1)) {
                        Sleep.sleepUntil(() -> getInventory().contains("Wooden shield"), 2000);
                    }
                    return 200;
                }

                // Retirar bronze sword
                if (!getInventory().contains("Bronze sword")) {
                    log("Withdrawing Bronze sword...");
                    if (getBank().withdraw("Bronze sword", 1)) {
                        Sleep.sleepUntil(() -> getInventory().contains("Bronze sword"), 2000);
                    }
                    return 200;
                }

                // Una vez tenemos ambos items, cerrar banco e iniciar chicken training
                if (getInventory().contains("Wooden shield") && getInventory().contains("Bronze sword")) {
                    if (getBank().close()) {
                        Sleep.sleepUntil(() -> !getBank().isOpen(), 3000, 500);
                        log("Banking complete. Starting chicken training...");
                        hasStartedChickenTraining = true;
                    }
                }
            }

            return 200;
        }

        // TODO EL CÓDIGO ORIGINAL DE TUTORIAL ISLAND SE MANTIENE EXACTAMENTE IGUAL
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